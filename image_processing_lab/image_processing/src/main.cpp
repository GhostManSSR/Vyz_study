#include <algorithm>
#include <atomic>
#include <chrono>
#include <cctype>
#include <cstddef>
#include <cstdlib>
#include <filesystem>
#include <iomanip>
#include <iostream>
#include <mutex>
#include <queue>
#include <stdexcept>
#include <string>
#include <system_error>
#include <thread>
#include <utility>
#include <vector>

#include "Image.h"

namespace fs = std::filesystem;

namespace {

struct Options {
    fs::path inputDir;
    fs::path outputDir;
    std::string filter;
    unsigned int threads = 0; 
    bool verbose = true;
};

struct Task {
    fs::path input;
    fs::path output;
};

void printUsage(const char* program) {
    std::cout
        << "Batch image processing (C++17, std::thread)\n\n"
        << "Usage:\n  " << program
        << " INPUT_DIR OUTPUT_DIR grayscale|invert|both [THREADS]\n"
        << "  " << program
        << " --input DIR --output DIR --filter grayscale|invert|both"
        << " [--threads N] [--quiet]\n\n"
        << "Examples:\n  " << program
        << " examples/input examples/output grayscale 4\n  " << program
        << " --input examples/input --output examples/output --filter both --threads 2\n\n"
        << "Input: recursively scans PNG, JPG, JPEG, BMP, TGA, GIF, PSD, PIC, PNM, PPM, PGM, HDR.\n"
        << "Output: preserves subdirectories and writes PNG files named"
        << " <original_name>_<filter>.png.\n"
        << "The 'both' mode applies grayscale first, then invert.\n"
        << "The --threads option defaults to the number of hardware threads (max 32).\n";
}

unsigned int parseThreadCount(const std::string& value) {
    if (value.empty() ||
        !std::all_of(value.begin(), value.end(), [](unsigned char c) { return std::isdigit(c) != 0; })) {
        throw std::invalid_argument("Thread count must be an integer from 1 to 128");
    }
    const unsigned long count = std::stoul(value);
    if (count < 1 || count > 128) {
        throw std::invalid_argument("Thread count must be an integer from 1 to 128");
    }
    return static_cast<unsigned int>(count);
}

Options parseArgs(int argc, char* argv[]) {
    Options options;
    if (argc >= 2 && (std::string(argv[1]) == "-h" || std::string(argv[1]) == "--help")) {
        printUsage(argv[0]);
        std::exit(0);
    }
    if (argc >= 2 && argv[1][0] != '-') {
        if (argc != 4 && argc != 5) {
            throw std::invalid_argument("Expected INPUT_DIR OUTPUT_DIR FILTER [THREADS]. Use --help.");
        }
        options.inputDir = argv[1];
        options.outputDir = argv[2];
        options.filter = argv[3];
        if (argc == 5) options.threads = parseThreadCount(argv[4]);
    } else {
        for (int i = 1; i < argc; ++i) {
            const std::string arg = argv[i];
            if (arg == "--quiet") {
                options.verbose = false;
                continue;
            }
            if (arg == "--input" || arg == "--output" || arg == "--filter" || arg == "--threads") {
                if (i + 1 == argc) throw std::invalid_argument("Missing value for " + arg);
                const std::string value = argv[++i];
                if (arg == "--input") options.inputDir = value;
                else if (arg == "--output") options.outputDir = value;
                else if (arg == "--filter") options.filter = value;
                else options.threads = parseThreadCount(value);
            } else {
                throw std::invalid_argument("Unknown option: " + arg + ". Use --help.");
            }
        }
    }
    if (options.inputDir.empty() || options.outputDir.empty()) {
        throw std::invalid_argument("Input and output directories are required. Use --help.");
    }
    if (options.filter != "grayscale" && options.filter != "invert" && options.filter != "both") {
        throw std::invalid_argument("Filter must be grayscale, invert, or both.");
    }
    if (options.threads == 0) {
        const unsigned int available = std::thread::hardware_concurrency();
        options.threads = available == 0 ? 4 : std::min(available, 32u);
    }
    return options;
}

bool isInside(const fs::path& parent, const fs::path& candidate) {
    auto p = parent.begin();
    auto c = candidate.begin();
    for (; p != parent.end(); ++p, ++c) {
        if (c == candidate.end() || *p != *c) return false;
    }
    return true;
}

bool supportedImage(const fs::path& path) {
    std::string ext = path.extension().string();
    std::transform(ext.begin(), ext.end(), ext.begin(),
                   [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return ext == ".png" || ext == ".jpg" || ext == ".jpeg" || ext == ".bmp" ||
           ext == ".tga" || ext == ".gif" || ext == ".psd" || ext == ".pic" ||
           ext == ".pnm" || ext == ".ppm" || ext == ".pgm" || ext == ".hdr";
}

std::queue<Task> collectTasks(const fs::path& inputDir,
                              const fs::path& outputDir,
                              const std::string& filter) {
    std::queue<Task> tasks;
    std::error_code error;
    fs::recursive_directory_iterator it(inputDir, fs::directory_options::skip_permission_denied, error);
    if (error) throw fs::filesystem_error("Cannot scan input directory", inputDir, error);
    const fs::recursive_directory_iterator end;
    for (; it != end; it.increment(error)) {
        if (error) throw fs::filesystem_error("Cannot continue scanning input directory", inputDir, error);
        const fs::directory_entry& entry = *it;
        const fs::path current = entry.path();
        if (entry.is_directory(error)) {
            if (error) throw fs::filesystem_error("Cannot inspect directory", current, error);

            if (isInside(outputDir, fs::weakly_canonical(current))) it.disable_recursion_pending();
            continue;
        }
        if (error) throw fs::filesystem_error("Cannot inspect directory entry", current, error);
        if (!entry.is_regular_file(error)) {
            if (error) throw fs::filesystem_error("Cannot inspect file", current, error);
            continue;
        }
        if (error) throw fs::filesystem_error("Cannot inspect file", current, error);
        if (!supportedImage(current)) continue;
        const fs::path relativePath = current.lexically_relative(inputDir);
        if (relativePath.empty()) throw std::runtime_error("Cannot make a relative input path");
        fs::path outputPath = outputDir / relativePath;
        outputPath += "_" + filter + ".png";
        tasks.push(Task{current, outputPath});
    }
    return tasks;
}

void applyFilter(Image& image, const std::string& filter) {
    if (filter == "grayscale" || filter == "both") image.grayscale();
    if (filter == "invert" || filter == "both") image.invert();
}

class ImageProcessor {
public:
    ImageProcessor(std::queue<Task> tasks, std::string filter, bool verbose)
        : tasks_(std::move(tasks)), filter_(std::move(filter)), verbose_(verbose) {}

    void run(unsigned int threadCount) {
        std::queue<Task> pending = tasks_;
        while (!pending.empty()) {
            fs::create_directories(pending.front().output.parent_path());
            pending.pop();
        }

        std::vector<std::thread> workers;
        workers.reserve(threadCount);
        try {
            for (unsigned int i = 0; i < threadCount; ++i) {
                workers.emplace_back(&ImageProcessor::workerLoop, this, i + 1);
            }
        } catch (...) {
            for (std::thread& worker : workers) if (worker.joinable()) worker.join();
            throw;
        }
        for (std::thread& worker : workers) worker.join();
    }

    std::size_t succeeded() const { return succeeded_.load(); }
    std::size_t failed() const { return failed_.load(); }

private:
    void workerLoop(unsigned int workerNumber) {
        while (true) {
            Task task;
            {
                std::lock_guard<std::mutex> lock(queueMutex_);
                if (tasks_.empty()) return;
                task = std::move(tasks_.front());
                tasks_.pop();
            }
            try {
                Image image;
                if (!image.load(task.input.string())) {
                    throw std::runtime_error("cannot decode image");
                }
                applyFilter(image, filter_);
                if (!image.save(task.output.string())) {
                    throw std::runtime_error("cannot save PNG");
                }
                succeeded_.fetch_add(1);
                if (verbose_) {
                    std::lock_guard<std::mutex> lock(logMutex_);
                    std::cout << "[OK] worker " << workerNumber << ": "
                              << task.input.filename().string() << " -> "
                              << task.output.string() << '\n';
                }
            } catch (const std::exception& ex) {
                failed_.fetch_add(1);
                std::lock_guard<std::mutex> lock(logMutex_);
                std::cerr << "[ERROR] " << task.input.string() << ": " << ex.what() << '\n';
            } catch (...) {
                failed_.fetch_add(1);
                std::lock_guard<std::mutex> lock(logMutex_);
                std::cerr << "[ERROR] " << task.input.string() << ": unknown error\n";
            }
        }
    }

    std::queue<Task> tasks_;
    std::string filter_;
    bool verbose_;
    std::mutex queueMutex_;   
    std::mutex logMutex_;    
    std::atomic<std::size_t> succeeded_{0};
    std::atomic<std::size_t> failed_{0};
};

} // namespace

int main(int argc, char* argv[]) {
    try {
        const Options options = parseArgs(argc, argv);
        if (!fs::is_directory(options.inputDir)) {
            throw std::runtime_error("Input directory does not exist: " + options.inputDir.string());
        }
        const fs::path inputDir = fs::weakly_canonical(options.inputDir);
        const fs::path outputDir = fs::weakly_canonical(options.outputDir);
        if (inputDir == outputDir) {
            throw std::invalid_argument("Input and output directories must be different");
        }

        std::queue<Task> tasks = collectTasks(inputDir, outputDir, options.filter);
        const std::size_t taskCount = tasks.size();
        if (taskCount == 0) {
            std::cout << "No supported image files found in " << inputDir.string() << '\n';
            return 0;
        }
        const unsigned int workers = std::min(options.threads, static_cast<unsigned int>(taskCount));
        std::cout << "Input: " << inputDir.string() << '\n'
                  << "Output: " << outputDir.string() << '\n'
                  << "Filter: " << options.filter << '\n'
                  << "Images: " << taskCount << ", worker threads: " << workers << '\n';
        ImageProcessor processor(std::move(tasks), options.filter, options.verbose);
        const auto start = std::chrono::steady_clock::now();
        processor.run(workers);
        const auto elapsed = std::chrono::duration<double, std::milli>(
            std::chrono::steady_clock::now() - start).count();
        std::cout << std::fixed << std::setprecision(2)
                  << "Finished: " << processor.succeeded() << " succeeded, "
                  << processor.failed() << " failed; elapsed: " << elapsed << " ms\n";
        return processor.failed() == 0 ? 0 : 1;
    } catch (const std::exception& ex) {
        std::cerr << "Error: " << ex.what() << '\n';
        return 1;
    }
}
