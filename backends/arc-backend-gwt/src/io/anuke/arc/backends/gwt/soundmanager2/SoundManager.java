/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.backends.gwt.soundmanager2;

public class SoundManager{

    public static final native SoundManager getInstance() /*-{
		return $wnd.soundManager;
	}-*/;

    public static native String getVersion() /*-{
		return $wnd.soundManager.version;
	}-*/;

    public static native String getUrl() /*-{
		return $wnd.soundManager.url;
	}-*/;

    public static native void setUrl(String url) /*-{
		$wnd.soundManager.url = url;
	}-*/;

    public static native boolean getDebugMode() /*-{
		return $wnd.soundManager.debugMode;
	}-*/;

    public static native void setDebugMode(boolean debug) /*-{
		$wnd.soundManager.debugMode = debug;
	}-*/;

    public static native int getFlashVersion() /*-{
		return $wnd.soundManager.flashVersion;
	}-*/;

    public static native void setFlashVersion(int version) /*-{
		$wnd.soundManager.flashVersion = version;
	}-*/;

    /**
     * Creates a new sound object from the supplied url.
     * @param url the location of the sound file.
     * @return the created sound object.
     */
    public static native SMSound createSound(String url) /*-{
		var jsSound = $wnd.soundManager.createSound({url: url});
		return @io.anuke.arc.backends.gwt.soundmanager2.SMSound::new(Lcom/google/gwt/core/client/JavaScriptObject;)(jsSound);
	}-*/;

    public static native void reboot() /*-{
		$wnd.soundManager.reboot();
	}-*/;

    public static native boolean ok() /*-{
		return $wnd.soundManager.ok();
	}-*/;

    public static native void init(String moduleBaseURL, int flashVersion, boolean preferFlash, SoundManagerCallback callback) /*-{
		$wnd.soundManager = new $wnd.SoundManager();
		$wnd.soundManager.setup({
			url: moduleBaseURL,
			flashVersion: flashVersion,
			preferFlash: preferFlash,
			onready: function() {
				callback.@io.anuke.arc.backends.gwt.soundmanager2.SoundManager.SoundManagerCallback::onready()();
			},
			ontimeout: function(status) {
				callback.@io.anuke.arc.backends.gwt.soundmanager2.SoundManager.SoundManagerCallback::ontimeout(Ljava/lang/String;Ljava/lang/String;)(status.success, (typeof status.error === 'undefined') ? '' : status.error.type);
			}

		});
		$wnd.soundManager.beginDelayedInit();
	}-*/;

    public interface SoundManagerCallback{
        void onready();

        void ontimeout(String status, String errorType);
    }

}