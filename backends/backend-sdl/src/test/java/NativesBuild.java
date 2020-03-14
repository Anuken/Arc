import arc.struct.*;
import arc.util.*;
import com.badlogic.gdx.jnigen.*;
import com.badlogic.gdx.jnigen.BuildTarget.*;

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
    static final boolean compileMac = OS.isMac || defined("mac"), compileLinux = OS.isLinux || defined("linux"), compileWindows = OS.isWindows || defined("windows");

    public static void main(String[] args) throws Exception{
        Array<BuildTarget> targets = new Array<>();

        if(compileMac){
            BuildTarget mac64 = BuildTarget.newDefaultTarget(TargetOs.MacOsX, true);

            mac64.cIncludes = new String[]{};
            mac64.cFlags = mac64.cppFlags = execCmd("sdl2-config --cflags") + " -c -Wall -O2 -arch x86_64 -DFIXED_POINT -fmessage-length=0 -fPIC -mmacosx-version-min=10.9";
            mac64.linkerFlags = "-shared -arch x86_64 -mmacosx-version-min=10.9";
            mac64.libraries = macLibPath + " -lm -liconv -Wl,-framework,OpenAL -Wl,-framework,CoreAudio -Wl,-framework,OpenGL,-framework,AudioToolbox -Wl,-framework,ForceFeedback -lobjc -Wl,-framework,CoreVideo -Wl,-framework,Cocoa -Wl,-framework,Carbon -Wl,-framework,IOKit -Wl,-weak_framework,QuartzCore -Wl,-weak_framework,Metal" + libsMac;

            targets.add(mac64);
        }

        if(compileLinux){
            checkSDLVersion("sdl2-config", minSDLversion);

            BuildTarget lin64 = BuildTarget.newDefaultTarget(TargetOs.Linux, true);

            lin64.cIncludes = new String[]{};

            if(defined("dynamic")){
              lin64.cFlags = lin64.cFlags + " " + execCmd("pkg-config --cflags glew glu gl sdl2 openal");
              lin64.cppFlags = lin64.cFlags;
              lin64.linkerFlags = "-shared -m64" + " " + execCmd("pkg-config --libs glew glu gl sdl2 openal");
            }else{
              lin64.cppFlags =  lin64.cFlags = lin64.cFlags + " " + execCmd("sdl2-config --cflags");
              lin64.linkerFlags = "-shared -m64";
              lin64.libraries = execCmd("sdl2-config --static-libs").replace("-lSDL2", "-l:libSDL2.a") + libsLinux;
            }

            targets.add(lin64);
        }

        if(compileWindows){
            checkSDLVersion(win32crossCompilePath + "sdl2-config", minSDLversion);
            checkSDLVersion(win64crossCompilePath + "sdl2-config", minSDLversion);

            BuildTarget win32 = BuildTarget.newDefaultTarget(TargetOs.Windows, false);
            BuildTarget win64 = BuildTarget.newDefaultTarget(TargetOs.Windows, true);

            win32.cppFlags = win32.cFlags = win32.cFlags + " " + execCmd(win32crossCompilePath + "sdl2-config --cflags");
            win32.libraries = execCmd(win32crossCompilePath + "sdl2-config --static-libs") + libsWin;

            win64.cppFlags = win64.cFlags = win64.cFlags + " " + execCmd(win64crossCompilePath + "sdl2-config --cflags");
            win64.libraries = execCmd(win64crossCompilePath + "sdl2-config --static-libs") + libsWin;

            targets.add(win32, win64);
        }

        buildScripts(targets.toArray(BuildTarget.class));
    }

    private static void buildScripts(BuildTarget... targets) throws Exception{
        new NativeCodeGenerator().generate("src/main/java", "build/classes/java/main", "jni");

        new AntScriptGenerator().generate(new BuildConfig("sdl-arc"), targets);

        for(BuildTarget target : targets){
            String buildFileName = "build-" + target.os.toString().toLowerCase() + (target.is64Bit ? "64" : "32") + ".xml";
            BuildExecutor.executeAnt("jni/" + buildFileName, "-Dhas-compiler=true -Drelease=true clean postcompile");
        }

        for(BuildTarget target : targets){
            if(target.os != TargetOs.MacOsX){
                exec("strip", "libs/" + target.os.name().toLowerCase() + (target.is64Bit ? "64" : "32") + "/" + target.libName);
            }
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

    private static boolean defined(String prop){
        return System.getProperty(prop) != null;
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
