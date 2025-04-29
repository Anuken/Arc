FROM docker.io/azul/zulu-openjdk:8-latest

# Install dependencies
RUN apt update && apt install -y libasound2-dev libdrm-dev libsdl2-dev libgbm-dev libglew-dev ant binutils g++

# Copy the repository
COPY . /home/Arc
WORKDIR /home/Arc

# Run the SDL build
RUN ./gradlew backends:backend-sdl:jnigenBuildLinuxARM64
