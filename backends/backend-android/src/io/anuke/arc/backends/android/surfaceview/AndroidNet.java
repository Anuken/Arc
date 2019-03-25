package io.anuke.arc.backends.android.surfaceview;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import io.anuke.arc.Net;
import io.anuke.arc.net.*;

/**
 * Android implementation of the {@link Net} API.
 * @author acoppes
 */
public class AndroidNet implements Net{

    // IMPORTANT: The Gdx.net classes are a currently duplicated for JGLFW/LWJGL + Android!
    // If you make changes here, make changes in the other backend as well.
    final AndroidApplicationBase app;
    NetJavaImpl netJavaImpl;

    public AndroidNet(AndroidApplicationBase app){
        this.app = app;
        netJavaImpl = new NetJavaImpl();
    }

    @Override
    public void sendHttpRequest(HttpRequest httpRequest, final HttpResponseListener httpResponseListener){
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
        boolean result = false;
        final Uri uri = Uri.parse(URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PackageManager pm = app.getContext().getPackageManager();
        if(pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null){
            app.runOnUiThread(() -> {
                Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                // LiveWallpaper and Daydream applications need this flag
                if(!(app.getContext() instanceof Activity))
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                app.startActivity(intent1);
            });
            result = true;
        }
        return result;
    }

}
