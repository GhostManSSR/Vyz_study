#include "Image.h"

#include <gtest/gtest.h>
#include <stb_image.h>
#include <stb_image_write.h>

#include <chrono>
#include <filesystem>
#include <string>
#include <system_error>

namespace fs = std::filesystem;

class ImageTest : public ::testing::Test {
protected:
    void SetUp() override {
        directory_ = fs::temp_directory_path() /
            ("image-unit-test-" + std::to_string(
                std::chrono::steady_clock::now().time_since_epoch().count()));
        fs::create_directories(directory_);
        input_ = directory_ / "input.png";
        output_ = directory_ / "output.png";

        const unsigned char pixels[] = {100, 150, 200, 77};
        ASSERT_NE(stbi_write_png(input_.string().c_str(), 1, 1, 4, pixels, 4), 0);
    }

    void TearDown() override {
        std::error_code error;
        fs::remove_all(directory_, error);
    }

    void expectPixel(const unsigned char expected[4]) {
        int width = 0;
        int height = 0;
        int channels = 0;
        unsigned char* pixels = stbi_load(output_.string().c_str(), &width, &height, &channels, 4);
        ASSERT_NE(pixels, nullptr);
        EXPECT_EQ(width, 1);
        EXPECT_EQ(height, 1);
        for (int i = 0; i < 4; ++i) EXPECT_EQ(pixels[i], expected[i]);
        stbi_image_free(pixels);
    }

    fs::path directory_;
    fs::path input_;
    fs::path output_;
};

TEST_F(ImageTest, GrayscalePreservesAlpha) {
    Image image;
    ASSERT_TRUE(image.load(input_.string()));
    ASSERT_EQ(image.getWidth(), 1);
    ASSERT_EQ(image.getHeight(), 1);

    image.grayscale();
    ASSERT_TRUE(image.save(output_.string()));

    const unsigned char expected[] = {140, 140, 140, 77};
    expectPixel(expected);
}

TEST_F(ImageTest, InvertPreservesAlpha) {
    Image image;
    ASSERT_TRUE(image.load(input_.string()));

    image.invert();
    ASSERT_TRUE(image.save(output_.string()));

    const unsigned char expected[] = {155, 105, 55, 77};
    expectPixel(expected);
}

TEST_F(ImageTest, MissingInputReturnsFalse) {
    Image image;
    EXPECT_FALSE(image.load((directory_ / "missing.png").string()));
    EXPECT_EQ(image.getWidth(), 0);
    EXPECT_EQ(image.getHeight(), 0);
}
