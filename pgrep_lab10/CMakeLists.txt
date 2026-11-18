cmake_minimum_required(VERSION 3.16)
project(pgrep LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(Threads REQUIRED)
add_executable(pgrep main.cpp)
target_link_libraries(pgrep PRIVATE Threads::Threads)

if(MSVC)
    target_compile_options(pgrep PRIVATE /W4)
else()
    target_compile_options(pgrep PRIVATE -Wall -Wextra -Wpedantic)
endif()
