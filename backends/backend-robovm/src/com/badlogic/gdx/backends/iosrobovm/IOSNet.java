package com.badlogic.gdx.backends.iosrobovm;

import arc.*;
import org.robovm.apple.dispatch.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;

public class IOSNet extends Net{
    final UIApplication uiApp;

    public IOSNet(IOSApplication app){
        uiApp = app.uiApp;
    }

    @Override
    public boolean openURI(String URI){
        NSURL url = new NSURL(URI);
        if(uiApp.canOpenURL(url)){
            try{
                DispatchQueue.getMainQueue().async(() -> {
                    uiApp.openURL(url, new UIApplicationOpenURLOptions(), null);
                });
                return true;
            }catch(Throwable t){
                t.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
