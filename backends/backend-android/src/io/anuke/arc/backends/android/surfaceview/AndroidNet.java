package io.anuke.arc.backends.android.surfaceview;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import io.anuke.arc.Net;
import io.anuke.arc.func.Cons;
import io.anuke.arc.util.NetJavaImpl;

/**
 * Android implementation of the {@link Net} API.
 * @author acoppes
 */
public class AndroidNet implements Net{
    final AndroidApplicationBase app;
    NetJavaImpl impl = new NetJavaImpl();

    public AndroidNet(AndroidApplicationBase app){
        this.app = app;
    }

    @Override
    public void http(HttpRequest httpRequest, Cons<HttpResponse> success, Cons<Throwable> failure){
        impl.http(httpRequest, success, failure);
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
