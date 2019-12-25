import com.badlogic.gdx.jnigen.*;
import com.badlogic.gdx.jnigen.BuildTarget.*;
import arc.util.*;

import java.io.*;
import java.util.*;

//mac: install sdl2, glew, ant, openal dev packages
//linux: install sdl2, glew, glew utils, openal dev packages
//windows on linux: mingw32, mingw32 64 bit, then compile sdl2 yourself with the right targets, also get the files from the openal SDK and put them in the right places (include/lib directories)
class NativesBuild{
    static final String win32crossCompilePath = "/usr/local/cross-tools/i686-w64-mingw32/bin/";
    static final String win64crossCompilePath = "/usr/local/cross-tools/x86_64-w64-mingw32/bin/";
    static final String minSDLversion = "2.0.9";
    static final String libsLinux = " -Wl,-Bstatic -l:libGLEW.a -l:libGLU.a -Wl,-Bdynamic -lGL -lopenal ";
    static final String libsMac = " /usr/local/lib/libGLEW.a";
    static final String libsWin = " -lglew32s -lglu32 -lopengl32 -lOpenAL32";
    static final String macLibPath = "/usr/local/lib/libSDL2.a";
    static final boolean compileMac = OS.isMac;

    public static void main(String[] args) throws Exception{
        if(compileMac){
            BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);

            mac64.cIncludes = new String[]{};
            mac64.cFlags = mac64.cppFlags = execCmd("sdl2-config --cflags") + " -c -Wall -O2 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.9";
            mac64.linkerFlags = "-shared -arch x86_64 -mmacosx-version-min=10.9";
            mac64.libraries = macLibPath + " -lm -liconv -Wl,-framework,OpenAL -Wl,-framework,CoreAudio -Wl,-framework,OpenGL,-framework,AudioToolbox -Wl,-framework,ForceFeedback -lobjc -Wl,-framework,CoreVideo -Wl,-framework,Cocoa -Wl,-framework,Carbon -Wl,-framework,IOKit -Wl,-weak_framework,QuartzCore -Wl,-weak_framework,Metal" + libsMac;

            new NativeCodeGenerator().generate("src/main/java", "build/classes/java/main", "jni");
            new AntScriptGenerator().generate(new BuildConfig("sdl-arc"), mac64);

            BuildExecutor.executeAnt("jni/build-macosx64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
        }else{
            BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
            BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
            BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);

            checkSDLVersion("sdl2-config", minSDLversion);
            checkSDLVersion(win32crossCompilePath + "sdl2-config", minSDLversion);
            checkSDLVersion(win64crossCompilePath + "sdl2-config", minSDLversion);

            lin64.cIncludes = new String[]{};
            lin64.cFlags = lin64.cFlags + " " + execCmd("sdl2-config --cflags");
            lin64.cppFlags = lin64.cFlags;
            lin64.linkerFlags = "-shared -m64";
            lin64.libraries = execCmd("sdl2-config --static-libs").replace("-lSDL2", "-l:libSDL2.a") + libsLinux;

            win32.cFlags = win32.cFlags + " " + execCmd(win32crossCompilePath + "sdl2-config --cflags");
            win32.cppFlags = win32.cFlags;
            win32.libraries = execCmd(win32crossCompilePath + "sdl2-config --static-libs") + libsWin;

            win64.cFlags = win64.cFlags + " " + execCmd(win64crossCompilePath + "sdl2-config --cflags");
            win64.cppFlags = win64.cFlags;
            win64.libraries = execCmd(win64crossCompilePath + "sdl2-config --static-libs") + libsWin;

            new NativeCodeGenerator().generate("src/main/java", "build/classes/java/main", "jni");
            new AntScriptGenerator().generate(new BuildConfig("sdl-arc"), win32, win64, lin64);

            BuildExecutor.executeAnt("jni/build-windows32.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
            BuildExecutor.executeAnt("jni/build-windows64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
            BuildExecutor.executeAnt("jni/build-linux64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
            exec("strip", "libs/windows32/sdl-arc.dll");
            exec("strip", "libs/windows64/sdl-arc64.dll");
            exec("strip", "libs/linux64/sdl-arc.so");
        }
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

    public static String exec(String... cmd) throws IOException{
        Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next().trim() : "";
    }

    public static String execCmd(String cmd) throws IOException{
        Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
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