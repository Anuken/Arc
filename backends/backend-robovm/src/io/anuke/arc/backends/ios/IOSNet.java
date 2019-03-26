package io.anuke.arc.backends.ios;

import io.anuke.arc.Net;
import io.anuke.arc.net.*;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationOpenURLOptions;

public class IOSNet implements Net{
    final UIApplication uiApp;
    NetJavaImpl netJavaImpl = new NetJavaImpl();

    public IOSNet(IOSApplication app){
        uiApp = app.uiApp;
    }

    @Override
    public void sendHttpRequest(HttpRequest httpRequest, HttpResponseListener httpResponseListener){
        netJavaImpl.sendHttpRequest(httpRequest, httpResponseListener);
    }

    @Override
    public void cancelHttpRequest(HttpRequest httpRequest){
        netJavaImpl.cancelHttpRequest(httpRequest);
    }

    @Override
    public ServerSocket newServerSocket(Protocol protocol, String hostname, int port, ServerSocketHints hints){
        return new NetJavaServerSocketImpl(protocol, hostname, port, hints);
    }

    @Override
    public ServerSocket newServerSocket(Protocol protocol, int port, ServerSocketHints hints){
        return new NetJavaServerSocketImpl(protocol, port, hints);
    }

    @Override
    public Socket newClientSocket(Protocol protocol, String host, int port, SocketHints hints){
        return new NetJavaSocketImpl(protocol, host, port, hints);
    }

    @Override
    public boolean openURI(String URI){
        NSURL url = new NSURL(URI);
        if(uiApp.canOpenURL(url)){
            uiApp.openURL(url, new UIApplicationOpenURLOptions(), null);
            return true;
        }
        return false;
    }
}
