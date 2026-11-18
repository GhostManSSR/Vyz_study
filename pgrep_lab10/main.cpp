#include <algorithm>
#include <atomic>
#include <chrono>
#include <cstddef>
#include <filesystem>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <limits>
#include <mutex>
#include <queue>
#include <sstream>
#include <string>
#include <system_error>
#include <thread>
#include <utility>
#include <vector>

namespace fs = std::filesystem;

struct Options {
    bool recursive = false;
    bool ignore_case = false;
    bool invert = false;
    bool line_numbers = false;
    bool force_filename = false;
    bool hide_filename = false;
    bool count_only = false;
    bool filenames_only = false;
    bool whole_word = false;
    bool whole_line = false;
    bool quiet = false;
    std::size_t jobs = std::max(1u, std::thread::hardware_concurrency());
    std::string pattern;
    std::vector<fs::path> inputs;
};

struct Statistics {
    std::atomic<std::size_t> success{0};
    std::atomic<std::size_t> errors{0};
    std::atomic<std::size_t> matches{0};
};

std::mutex output_mutex;
std::mutex error_mutex;

void usage(std::ostream& out) {
    out << "Usage: pgrep [OPTIONS] PATTERN [PATH...]\n"
        << "  -r       search directories recursively (do not follow symlinks)\n"
        << "  -i       ignore ASCII letter case\n"
        << "  -v       select non-matching lines\n"
        << "  -n       print 1-based line numbers\n"
        << "  -H/-h    always/never print filenames\n"
        << "  -c       print number of selected lines per file\n"
        << "  -l       print names of files with selected lines\n"
        << "  -j N     use N workers (N >= 1; N=1 is sequential)\n"
        << "  -w       match whole ASCII words\n"
        << "  -x       require a complete-line match\n"
        << "  -q       suppress all output; use only the exit status\n"
        << "  --       end of options\n";
}

bool parse_jobs(const std::string& value, std::size_t& jobs) {
    if (value.empty() || value.find_first_not_of("0123456789") != std::string::npos) {
        return false;
    }
    try {
        const unsigned long long parsed = std::stoull(value);
        if (parsed == 0 || parsed > std::numeric_limits<std::size_t>::max()) {
            return false;
        }
        jobs = static_cast<std::size_t>(parsed);
        return true;
    } catch (const std::exception&) {
        return false;
    }
}

// An option group such as -rni is accepted, as are -j 4 and -j4.
bool parse_arguments(int argc, char* argv[], Options& opts) {
    bool end_of_options = false;
    bool pattern_seen = false;
    for (int arg = 1; arg < argc; ++arg) {
        const std::string current = argv[arg];
        if (!pattern_seen && !end_of_options && current == "--") {
            end_of_options = true;
            continue;
        }
        if (!pattern_seen && !end_of_options && current.size() > 1 && current[0] == '-') {
            for (std::size_t pos = 1; pos < current.size(); ++pos) {
                switch (current[pos]) {
                    case 'r': opts.recursive = true; break;
                    case 'i': opts.ignore_case = true; break;
                    case 'v': opts.invert = true; break;
                    case 'n': opts.line_numbers = true; break;
                    case 'H': opts.force_filename = true; opts.hide_filename = false; break;
                    case 'h': opts.hide_filename = true; opts.force_filename = false; break;
                    case 'c': opts.count_only = true; break;
                    case 'l': opts.filenames_only = true; break;
                    case 'w': opts.whole_word = true; break;
                    case 'x': opts.whole_line = true; break;
                    case 'q': opts.quiet = true; break;
                    case 'j': {
                        const std::string value = (pos + 1 < current.size())
                            ? current.substr(pos + 1)
                            : (arg + 1 < argc ? std::string(argv[++arg]) : "");
                        if (!parse_jobs(value, opts.jobs)) {
                            std::cerr << "pgrep: invalid number of threads: '" << value << "'\n";
                            return false;
                        }
                        pos = current.size();
                        break;
                    }
                    default:
                        std::cerr << "pgrep: unknown option: -" << current[pos] << '\n';
                        return false;
                }
            }
        } else if (!pattern_seen) {
            opts.pattern = current;
            pattern_seen = true;
        } else {
            opts.inputs.emplace_back(current);
        }
    }
    if (!pattern_seen) {
        std::cerr << "pgrep: PATTERN is required\n";
        return false;
    }
    if (opts.inputs.empty()) {
        opts.inputs.emplace_back(".");
    }
    return true;
}

// Case folding is limited to ASCII, as required by the assignment.
char ascii_lower(char c) {
    return (c >= 'A' && c <= 'Z') ? static_cast<char>(c - 'A' + 'a') : c;
}

std::string ascii_lower_copy(std::string value) {
    std::transform(value.begin(), value.end(), value.begin(), ascii_lower);
    return value;
}

bool is_word_char(char c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
           (c >= '0' && c <= '9') || c == '_';
}

// Checks a fixed substring, possibly with ASCII case folding and word boundaries.
bool matches_line(const std::string& original, const std::string& normalized_pattern,
                  const Options& opts) {
    const std::string folded = opts.ignore_case ? ascii_lower_copy(original) : original;
    if (opts.whole_line) {
        return folded == normalized_pattern;
    }
    if (normalized_pattern.empty()) {
        return true;
    }
    std::size_t position = folded.find(normalized_pattern);
    while (position != std::string::npos) {
        if (!opts.whole_word ||
            ((position == 0 || !is_word_char(original[position - 1])) &&
             (position + normalized_pattern.size() == original.size() ||
              !is_word_char(original[position + normalized_pattern.size()])))) {
            return true;
        }
        position = folded.find(normalized_pattern, position + 1);
    }
    return false;
}

void report_error(const fs::path& path, const std::string& message, const Options& opts) {
    if (!opts.quiet) {
        std::lock_guard<std::mutex> lock(error_mutex);
        std::cerr << "pgrep: " << path.string() << ": " << message << '\n';
    }
}

// File discovery is exclusively the main thread's responsibility.
// Files, not fragments of files, become units of work.
std::vector<fs::path> discover_files(const Options& opts, Statistics& stats) {
    std::vector<fs::path> files;
    for (const fs::path& input : opts.inputs) {
        std::error_code ec;
        const fs::file_status status = fs::symlink_status(input, ec);
        if (ec) {
            report_error(input, ec.message(), opts);
            ++stats.errors;
            continue;
        }
        if (fs::is_symlink(status)) {
            report_error(input, "symbolic links are not followed", opts);
            ++stats.errors;
            continue;
        }
        if (fs::is_regular_file(status)) {
            files.push_back(input);
            continue;
        }
        if (!fs::is_directory(status)) {
            report_error(input, "not a regular file", opts);
            ++stats.errors;
            continue;
        }
        if (!opts.recursive) {
            report_error(input, "is a directory (use -r)", opts);
            ++stats.errors;
            continue;
        }

        fs::recursive_directory_iterator it(input, fs::directory_options::none, ec);
        const fs::recursive_directory_iterator end;
        if (ec) {
            report_error(input, ec.message(), opts);
            ++stats.errors;
            continue;
        }
        while (it != end) {
            const fs::path path = it->path();
            const fs::file_status entry_status = it->symlink_status(ec);
            if (ec) {
                report_error(path, ec.message(), opts);
                ++stats.errors;
                ec.clear();
            } else if (fs::is_regular_file(entry_status)) {
                files.push_back(path);
            }
            // By default recursive_directory_iterator never descends into symlinks.
            it.increment(ec);
            if (ec) {
                report_error(input, "directory traversal failed: " + ec.message(), opts);
                ++stats.errors;
                break; // Continue with the next separately specified input path.
            }
        }
    }
    return files;
}

// One invocation handles exactly one file, regardless of the number of workers.
// Buffer per-file output so a failed or binary file doesn't produce partial results.
void process_file(const fs::path& path, const Options& opts,
                  const std::string& normalized_pattern, bool show_filename,
                  Statistics& stats) {
    std::ifstream input(path, std::ios::binary);
    if (!input.is_open()) {
        report_error(path, "cannot open file", opts);
        ++stats.errors;
        return;
    }

    const std::string filename = path.string();
    std::ostringstream buffered_output;
    std::size_t selected_count = 0;
    std::size_t line_number = 0;
    std::string line;
    while (std::getline(input, line)) {
        ++line_number;
        if (line.find('\0') != std::string::npos) {
            report_error(path, "binary/corrupted file (NUL byte)", opts);
            ++stats.errors;
            return;
        }
        const bool selected = matches_line(line, normalized_pattern, opts) != opts.invert;
        if (!selected) {
            continue;
        }
        ++selected_count;
        if (!opts.quiet && !opts.count_only && !opts.filenames_only) {
            if (show_filename) buffered_output << filename << ':';
            if (opts.line_numbers) buffered_output << line_number << ':';
            buffered_output << line << '\n';
        }
    }
    if (input.bad()) {
        report_error(path, "I/O error while reading", opts);
        ++stats.errors;
        return;
    }

    ++stats.success;
    stats.matches += selected_count;
    if (opts.quiet) {
        return;
    }
    if (opts.filenames_only) {
        if (selected_count > 0) buffered_output << filename << '\n';
    } else if (opts.count_only) {
        // The lab's -c examples explicitly show 'filename:number', even for one file.
        if (!opts.hide_filename || opts.force_filename) buffered_output << filename << ':';
        buffered_output << selected_count << '\n';
    }

    const std::string result = buffered_output.str();
    if (!result.empty()) {
        // A single lock also keeps the lines of each file adjacent in output.
        std::lock_guard<std::mutex> lock(output_mutex);
        std::cout << result;
    }
}

int main(int argc, char* argv[]) {
    Options opts;
    if (!parse_arguments(argc, argv, opts)) {
        usage(std::cerr);
        return 2;
    }

    const auto start = std::chrono::steady_clock::now();
    const std::string normalized_pattern = opts.ignore_case
        ? ascii_lower_copy(opts.pattern) : opts.pattern;
    Statistics stats;
    std::vector<fs::path> files = discover_files(opts, stats);
    const std::size_t total = files.size() + stats.errors.load();
    const bool show_filename = opts.force_filename ||
        (!opts.hide_filename && (opts.recursive || opts.inputs.size() > 1));

    // -j 1 must be genuinely sequential: no std::thread is constructed.
    if (opts.jobs == 1 || files.size() <= 1) {
        for (const fs::path& file : files) {
            process_file(file, opts, normalized_pattern, show_filename, stats);
        }
    } else {
        std::queue<fs::path> tasks;
        for (const fs::path& file : files) tasks.push(file);
        std::mutex queue_mutex;
        const std::size_t worker_count = std::min(opts.jobs, files.size());
        std::vector<std::thread> workers;
        workers.reserve(worker_count);

        auto worker = [&] {
            for (;;) {
                fs::path next;
                {
                    std::lock_guard<std::mutex> lock(queue_mutex);
                    if (tasks.empty()) return;
                    next = std::move(tasks.front());
                    tasks.pop();
                }
                process_file(next, opts, normalized_pattern, show_filename, stats);
            }
        };
        try {
            for (std::size_t n = 0; n < worker_count; ++n) {
                workers.emplace_back(worker);
            }
        } catch (const std::system_error& error) {
            // A partial pool is still safe: already-created workers finish every task.
            if (!opts.quiet) {
                std::lock_guard<std::mutex> lock(error_mutex);
                std::cerr << "pgrep: unable to create all threads: " << error.what() << '\n';
            }
            if (workers.empty()) worker();
        }
        for (std::thread& thread : workers) thread.join();
    }

    if (!opts.quiet) {
        const std::chrono::duration<double> elapsed =
            std::chrono::steady_clock::now() - start;
        std::cerr << "Processed: " << total << " files, " << stats.matches.load()
                  << " matches, " << stats.errors.load() << " errors, "
                  << std::fixed << std::setprecision(3) << elapsed.count() << " s\n";
    }
    // Invariant: successful files + errors equals the total attempt count.
    // Grep-style exit codes: 0=selected lines, 1=none, 2=error.
    // In quiet mode, a found match takes precedence over an unrelated error.
    if (opts.quiet && stats.matches.load() > 0) return 0;
    if (stats.errors.load() > 0) return 2;
    return stats.matches.load() > 0 ? 0 : 1;
}
