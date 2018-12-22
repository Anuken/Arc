package io.anuke.arc.backends.android.surfaceview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import io.anuke.arc.util.Clipboard;

public class AndroidClipboard implements Clipboard{

    private ClipboardManager honeycombClipboard;

    public AndroidClipboard(Context context){
        honeycombClipboard = (android.content.ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public String getContents(){
        ClipData clip = honeycombClipboard.getPrimaryClip();
        if(clip == null) return null;
        CharSequence text = clip.getItemAt(0).getText();
        if(text == null) return null;
        return text.toString();
    }

    @Override
    public void setContents(final String contents){
        ClipData data = ClipData.newPlainText(contents, contents);
        honeycombClipboard.setPrimaryClip(data);
    }
}
