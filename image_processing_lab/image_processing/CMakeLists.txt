cmake_minimum_required(VERSION 3.16)
project(image_processing LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(Threads REQUIRED)

add_executable(image_processor
    src/main.cpp
    src/Image.cpp
)

target_include_directories(image_processor PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${CMAKE_CURRENT_SOURCE_DIR}/third_party
)

target_link_libraries(image_processor PRIVATE Threads::Threads)

if(MSVC)
    target_compile_options(image_processor PRIVATE /W4)
else()
    target_compile_options(image_processor PRIVATE -Wall -Wextra -Wpedantic)
endif()

include(CTest)
if(BUILD_TESTING)
    find_package(Python3 COMPONENTS Interpreter QUIET)
    if(Python3_Interpreter_FOUND)
        add_test(NAME image_processor_integration
            COMMAND ${Python3_EXECUTABLE}
                ${CMAKE_CURRENT_SOURCE_DIR}/tests/test_integration.py
                $<TARGET_FILE:image_processor>)
    endif()
endif()
