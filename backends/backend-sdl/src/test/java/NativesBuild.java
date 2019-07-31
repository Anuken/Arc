import com.badlogic.gdx.jnigen.*;
import com.badlogic.gdx.jnigen.BuildTarget.*;
import io.anuke.arc.util.*;

import java.io.*;
import java.util.*;

//mac: install sdl2, glew, ant
//linux: install sdl2, glew, glew utils, libmpg123-dev
//windows on linux: mingw32, mingw32 64 bit, then compile sdl2 yourself with the right targets, also compile sdl2-mixer
class NativesBuild{
    static final String win32crossCompilePath = "/usr/local/cross-tools/i686-w64-mingw32/bin/";
    static final String win64crossCompilePath = "/usr/local/cross-tools/x86_64-w64-mingw32/bin/";
    static final String minSDLversion = "2.0.9";
    static final String libsLinux = " -lGLEW -lGLU -lGL -lSDL2_mixer";
    static final String libsMac = " -lGLEW -lSDL2_mixer";
    static final String libsWin = " -lglew32s -lglu32 -lopengl32 -lSDL2_mixer";
    static final String macLibPath = "/usr/local/lib/libSDL2.a";
    static final boolean compileMac = OS.isMac;

    public static void main(String[] args) throws Exception{
        BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);
        BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
        BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);
        BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);

        checkSDLVersion("sdl2-config", minSDLversion);
        checkSDLVersion(win32crossCompilePath + "sdl2-config", minSDLversion);
        checkSDLVersion(win64crossCompilePath + "sdl2-config", minSDLversion);

        lin64.cIncludes = new String[]{};
        lin64.cFlags = lin64.cFlags + " " + execCmd("sdl2-config --cflags");
        lin64.cppFlags = lin64.cFlags;
        lin64.linkerFlags = "-shared -m64";
        lin64.libraries = execCmd("sdl2-config --static-libs").replace("-lSDL2", "-l:libSDL2.a") + libsLinux;

        mac64.cIncludes = new String[]{};
        mac64.cFlags = execCmd("sdl2-config --cflags") + " -c -Wall -O2 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.6";
        mac64.cppFlags = mac64.cFlags;
        mac64.linkerFlags = "-shared -arch x86_64 -mmacosx-version-min=10.6";
        mac64.libraries = macLibPath + " -lm -liconv -Wl,-framework,CoreAudio -Wl,-framework,OpenGL,-framework,AudioToolbox -Wl,-framework,ForceFeedback -lobjc -Wl,-framework,CoreVideo -Wl,-framework,Cocoa -Wl,-framework,Carbon -Wl,-framework,IOKit -Wl,-weak_framework,QuartzCore -Wl,-weak_framework,Metal"  + libsMac;

        win32.cFlags = win32.cFlags + " " + execCmd(win32crossCompilePath + "sdl2-config --cflags");
        win32.cppFlags = win32.cFlags;
        win32.libraries = execCmd(win32crossCompilePath + "sdl2-config --static-libs") + libsWin;

        win64.cFlags = win64.cFlags + " " + execCmd(win64crossCompilePath + "sdl2-config --cflags");
        win64.cppFlags = win64.cFlags;
        win64.libraries = execCmd(win64crossCompilePath + "sdl2-config --static-libs") + libsWin;

        new NativeCodeGenerator().generate("src/main/java", "build/classes/java/main", "jni");
        new AntScriptGenerator().generate(new BuildConfig("sdl-arc"), win32, win64, lin64, mac64);

        BuildExecutor.executeAnt("jni/build-windows32.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
        BuildExecutor.executeAnt("jni/build-windows64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
        BuildExecutor.executeAnt("jni/build-linux64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
        if(compileMac) BuildExecutor.executeAnt("jni/build-macosx64.xml", "-Dhas-compiler=true -Drelease=true clean postcompile");
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