FROM docker.io/azul/zulu-openjdk:8-latest

# Install dependencies
RUN apt update && apt install -y libasound2-dev libdrm-dev libsdl2-dev libgbm-dev ant binutils g++

# Create symlinks
# For some reason JNI looks for binaries prefixed with aarch64-linux-gnu- even when they're not set in build-linuxarm64.xml
#RUN ln -s /aarch64-linux-gnu-g++
#RUN ln -s /aarch64-linux-gnu-strip

# Copy the repository
COPY . /home/Arc
WORKDIR /home/Arc

# Run the SDL build
RUN ./gradlew backends:backend-sdl:jnigenBuildLinuxARM64
