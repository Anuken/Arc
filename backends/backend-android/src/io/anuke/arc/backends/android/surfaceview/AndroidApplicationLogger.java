package io.anuke.arc.backends.android.surfaceview;

import android.util.Log;
import io.anuke.arc.Core;
import io.anuke.arc.util.Log.LogHandler;
import io.anuke.arc.util.Strings;

public class AndroidApplicationLogger extends LogHandler{

    @Override
    public void info(String text, Object... args){
        Log.i(Core.settings == null || Core.settings.getAppName() == null ? "libGDX" : Core.settings.getAppName(), Strings.format(text, args));
    }

    @Override
    public void warn(String text, Object... args){
        Log.w(Core.settings == null || Core.settings.getAppName() == null ? "libGDX" : Core.settings.getAppName(), Strings.format(text, args));
    }

    @Override
    public void err(String text, Object... args){
        Log.e(Core.settings == null || Core.settings.getAppName() == null ? "libGDX" : Core.settings.getAppName(), Strings.format(text, args));
    }
}
