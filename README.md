[![](https://github.com/Anuken/Arc/workflows/Java%20CI/badge.svg)](https://github.com/Anuken/Arc/actions)

# Where's the documentation?

There isn't any. This project is only used as the framework for Mindustry.

# How do I make a game with this framework?

Please don't. While there is nothing stopping you from making other games with this framework, there is no "project setup" tool of any kind, and no guide on how to do anything. In addition, I would discourage anyone from making games in Java at all.

# How does Arc differ from libGDX?

There are too many things to list, but here are some highlights:

- Soloud used as the audio engine across all platforms - faster, more consistent and more capable than libGDX's per-platform abstraction
- SDL used as the desktop backend library instead of LWJGL+GLFW bindings - comes with its own benefits and drawbacks
- Removal of GWT module and all workarounds associated with it
- Proper methods for drawing lines, polygons, etc in one sprite batch
- Global sprite batch, texture atlas, asset manager, etc
- Thin GL abstraction layer, state is cached to prevent unnecessary API calls
- All APIs deal with 2D coordinates instead of attempting to share 2D and 3D classes (cameras, matrices, etc)
- 3D package removed, some small parts moved to an extension
- Java 8 target, heavy usage of lambdas in Scene2D code
- Massive amount of refactored, merged, deleted classes (*especially* deleted)
