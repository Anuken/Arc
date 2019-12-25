package arc.backend.android;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import arc.*;

/**
 * Android implementation of the {@link Net} API.
 * @author acoppes
 */
public class AndroidNet extends Net{
    final AndroidApplicationBase app;

    public AndroidNet(AndroidApplicationBase app){
        this.app = app;
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
