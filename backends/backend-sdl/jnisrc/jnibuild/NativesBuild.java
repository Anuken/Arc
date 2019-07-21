package jnibuild;

import com.badlogic.gdx.jnigen.*;
import com.badlogic.gdx.jnigen.BuildTarget.*;

import java.io.*;

class NativesBuild{
    static final String win32crossCompilePath = "/usr/local/cross-tools/i686-w64-mingw32/bin/";
    static final String win64crossCompilePath = "/usr/local/cross-tools/x86_64-w64-mingw32/bin/";
    static final String minSDLversion = "2.0.9";
    static final String libs = " -lGLEW -lGLU -lGL";
    static final String macLibPath = "/usr/local/lib/libSDL2.a";

    public static void main(String[] args) throws Exception{
        //Deal with arguments
        boolean useSystemSDL = false;
        boolean buildWindows = false;
        boolean buildLinux = false;
        boolean buildOSX = false;

        for(String s : args){
            switch(s){
                case "system-SDL2":
                    useSystemSDL = true;
                    break;
                case "build-windows":
                    buildWindows = true;
                    break;
                case "build-linux":
                    buildLinux = true;
                    break;
                case "build-OSX":
                    buildOSX = true;
                    break;
            }
        }

        System.out.println("Using system SDL     (arg: system-SDL2)   " + (useSystemSDL ? "ON" : "OFF"));
        System.out.println("Building for Windows (arg: build-windows) " + (buildWindows ? "ON" : "OFF"));
        System.out.println("Building for Linux   (arg: build-linux)   " + (buildLinux ? "ON" : "OFF"));
        System.out.println("Building for OSX     (arg: build-OSX)     " + (buildOSX ? "ON" : "OFF"));
        System.out.println();

        BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
        BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
        BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
        BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);

        if(buildLinux){
            checkSDLVersion("sdl2-config", minSDLversion);

            lin64.cIncludes = new String[]{};
            String cflags = execCmd("sdl2-config --cflags"); // "-I/usr/local/include/SDL2"
            lin64.cFlags = lin64.cFlags + " " + cflags;
            lin64.cppFlags = lin64.cFlags;
            lin64.linkerFlags = "-shared -m64";
            //"-L/usr/local/lib -Wl,-rpath,/usr/local/lib -Wl,--enable-new-dtags -l:libSDL2.a -Wl,--no-undefined -lm -ldl -lsndio -lpthread -lrt";
            lin64.libraries = execCmd("sdl2-config --static-libs").replace("-lSDL2", "-l:libSDL2.a") + libs;
        }

        if(buildOSX){
            checkSDLVersion("sdl2-config", minSDLversion);

            mac64.cIncludes = new String[]{};
            //mac64.headerDirs = new String[] {"/usr/local/include/SDL2"};
            String cflags = execCmd("sdl2-config --cflags");
            mac64.cFlags = cflags + " -c -Wall -O2 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.6";
            mac64.cppFlags = mac64.cFlags;
            mac64.linkerFlags = "-shared -arch x86_64 -mmacosx-version-min=10.6";
            mac64.libraries = macLibPath + " -lm -liconv -Wl,-framework,CoreAudio -Wl,-framework,AudioToolbox -Wl,-framework,ForceFeedback -lobjc -Wl,-framework,CoreVideo -Wl,-framework,Cocoa -Wl,-framework,Carbon -Wl,-framework,IOKit -Wl,-weak_framework,QuartzCore -Wl,-weak_framework,Metal"  + libs;
            // we cant use:
            //   execCmd("sdl2-config --static-libs").replace("-lSDL2","-l:libSDL2.a" )
            // because OSX has Clang, not GCC.  See https://jonwillia.ms/2018/02/02/static-linking for the problem
        }

        if(buildWindows){
            checkSDLVersion(win32crossCompilePath + "sdl2-config", minSDLversion);
            checkSDLVersion(win64crossCompilePath + "sdl2-config", minSDLversion);

            win32.cFlags = win32.cFlags + " " + execCmd(win32crossCompilePath + "sdl2-config --cflags");
            win32.cppFlags = win32.cFlags;
            win32.libraries = execCmd(win32crossCompilePath + "sdl2-config --static-libs") + " -lglew32s -lglu32 -lopengl32";

            win64.cFlags = win64.cFlags + " " + execCmd(win64crossCompilePath + "sdl2-config --cflags");
            win64.cppFlags = win64.cFlags;
            win64.libraries = execCmd(win64crossCompilePath + "sdl2-config --static-libs") + " -lglew32s -lglu32 -lopengl32";
        }

        //Generate native code, build scripts
        System.out.println("##### GENERATING NATIVE CODE AND BUILD SCRIPTS #####");
        new NativeCodeGenerator().generate("src", "build/classes/java/main", "jni");
        new AntScriptGenerator().generate(new BuildConfig("sdl-arc", "build/tmp", "libs", "jni"), win32, win64, lin64, mac64);
        System.out.println();

        if(!useSystemSDL){
            throw new IllegalArgumentException("system SDL must be used!");
        }

        //Build library for all platforms and bitnesses
        if(buildWindows){
            System.out.println("##### COMPILING NATIVES FOR WINDOWS #####");
            BuildExecutorFixed.executeAnt("jni/build-windows32.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
            BuildExecutorFixed.executeAnt("jni/build-windows64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
            System.out.println();
        }
        if(buildLinux){
            System.out.println("##### COMPILING NATIVES FOR LINUX #####");
            BuildExecutorFixed.executeAnt("jni/build-linux64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
            System.out.println();
        }
        if(buildOSX){
            System.out.println("##### COMPILING NATIVES FOR OSX #####");
            BuildExecutorFixed.executeAnt("jni/build-macosx64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
            System.out.println();
        }

        System.out.println("##### PACKING NATIVES INTO .JAR #####");
        BuildExecutorFixed.executeAnt("jni/build.xml", "pack-natives");
    }

    private static void checkSDLVersion(String command, String version) throws FileNotFoundException{
        String sdl = "0";
        try{
            sdl = execCmd(command + " --version").trim();
        }catch(Exception e){
            System.out.println("SDL must be installed and " + command + " command must be on path.");
            e.printStackTrace();
        }
        System.out.println("SDL version found: " + sdl);
        if(compareVersions(sdl, version) < 0){
            throw new FileNotFoundException("\n!!! SDL version must be >= " + version + ". Current version: " + sdl);
        }
    }

    public static String execCmd(String cmd) throws java.io.IOException{
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next().trim() : "";
    }

    public static int compareVersions(String v1, String v2){
        String[] components1 = v1.split("\\.");
        String[] components2 = v2.split("\\.");
        int length = Math.min(components1.length, components2.length);
        for(int i = 0; i < length; i++){
            int result = new Integer(components1[i]).compareTo(Integer.parseInt(components2[i]));
            if(result != 0){
                return result;
            }
        }
        return Integer.compare(components1.length, components2.length);
    }
}