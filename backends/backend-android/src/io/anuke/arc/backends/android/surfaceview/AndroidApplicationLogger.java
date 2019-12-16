package io.anuke.arc.backends.android.surfaceview;

import android.util.Log;
import io.anuke.arc.Core;
import io.anuke.arc.util.Log.*;
import io.anuke.arc.util.Strings;

public class AndroidApplicationLogger implements LogHandler{

    @Override
    public void log(LogLevel level, String text, Object... args){
        if(level == LogLevel.info){
            Log.i(Core.settings == null || Core.settings.getAppName() == null ? "Arc" : Core.settings.getAppName(), Strings.format(text, args));
        }else if(level == LogLevel.warn){
            Log.w(Core.settings == null || Core.settings.getAppName() == null ? "Arc" : Core.settings.getAppName(), Strings.format(text, args));
        }else if(level == LogLevel.err){
            Log.e(Core.settings == null || Core.settings.getAppName() == null ? "Arc" : Core.settings.getAppName(), Strings.format(text, args));
        }else if(level == LogLevel.debug){
            Log.d(Core.settings == null || Core.settings.getAppName() == null ? "Arc" : Core.settings.getAppName(), Strings.format(text, args));
        }
    }
}
