# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("serial_port")

# 设置源文件路径
set(SERIAL_PORT_SRC
    SerialPort.c
)

# 构建共享库
add_library(serial_port SHARED ${SERIAL_PORT_SRC})

# 添加库依赖
target_link_libraries(serial_port
    log
)

# 设置构建选项
set(CMAKE_BUILD_TYPE Release)
set(CMAKE_CXX_FLAGS "-std=c++11 -Wall -Wextra -pedantic")

# 添加 Android 平台的库依赖
target_link_libraries(serial_port
    android
)