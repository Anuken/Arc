package com.badlogic.gdx.backends.iosrobovm;

import io.anuke.arc.Net;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.util.NetJavaImpl;
import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationOpenURLOptions;

public class IOSNet implements Net{
    final UIApplication uiApp;
    NetJavaImpl impl = new NetJavaImpl();

    public IOSNet(IOSApplication app){
        uiApp = app.uiApp;
    }

    @Override
    public void http(HttpRequest httpRequest, Consumer<HttpResponse> success, Consumer<Throwable> failure){
        impl.http(httpRequest, success, failure);
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
