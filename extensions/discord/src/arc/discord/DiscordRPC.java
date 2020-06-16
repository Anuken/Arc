package arc.discord;

import arc.util.*;

/**
 * Core library binding for the official <a href="https://github.com/discordapp/discord-rpc" target="_blank">Discord RPC SDK</a>.
 * Supports Windows/Mac/Linux. No 32-bit support.
 */
public class DiscordRPC{

    /*JNI

    #include <discord-rpc/linux-dynamic/include/discord_rpc.h>
    #include <string.h>

    static jobject callback = 0;
    static jmethodID mready;
    static jmethodID mdisconnected;
    static jmethodID merror;
    static jmethodID mjoin;
    static jmethodID mspectate;
    static jmethodID mjoinrequest;
    static JNIEnv* senv;

    static void handleDiscordReady(const DiscordUser* user){
        if(callback){
			senv->CallVoidMethod(callback, mready, senv->NewStringUTF(user->userId), senv->NewStringUTF(user->username), senv->NewStringUTF(user->discriminator), senv->NewStringUTF(user->avatar));
		}
    }

    static void handleDiscordDisconnected(int errcode, const char* message){
        if(callback){
			senv->CallVoidMethod(callback, mdisconnected, (jint)errcode, senv->NewStringUTF(message));
		}
    }

    static void handleDiscordError(int errcode, const char* message){
        if(callback){
			senv->CallVoidMethod(callback, merror, (jint)errcode, senv->NewStringUTF(message));
		}
    }

    static void handleDiscordJoin(const char* secret){
        if(callback){
			senv->CallVoidMethod(callback, mjoin, senv->NewStringUTF(secret));
		}
    }

    static void handleDiscordSpectate(const char* secret){
        if(callback){
			senv->CallVoidMethod(callback, mspectate, senv->NewStringUTF(secret));
		}
    }

    static void handleDiscordJoinRequest(const DiscordUser* user){
        if(callback){
			senv->CallVoidMethod(callback, mjoinrequest, senv->NewStringUTF(user->userId), senv->NewStringUTF(user->username), senv->NewStringUTF(user->discriminator), senv->NewStringUTF(user->avatar));
		}
    }

	 */

    static{
        new SharedLibraryLoader(){
            @Override public String mapLibraryName(String libraryName){ return OS.isWindows ? "discord-rpc.dll" : OS.isLinux ? "libdiscord-rpc.so" : "libdiscord-rpc.dylib"; }
        }.load("discord-rpc");

        new SharedLibraryLoader().load("arc-discord");

        init();
    }

    private static native void init(); /*
        jclass callbackClass = env->FindClass("arc/discord/DiscordRPC$DiscordEventHandler");
        jclass exception = env->FindClass("java/lang/Exception");

        mready = env->GetMethodID(callbackClass, "onReady", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		if(!mready) env->ThrowNew(exception, "Couldn't find onReady() method");

        mdisconnected = env->GetMethodID(callbackClass, "onDisconnected", "(ILjava/lang/String;)V");
		if(!mdisconnected) env->ThrowNew(exception, "Couldn't find onDisconnected() method");

        merror = env->GetMethodID(callbackClass, "onErrored", "(ILjava/lang/String;)V");
		if(!merror) env->ThrowNew(exception, "Couldn't find onErrored() method");

        mjoin = env->GetMethodID(callbackClass, "onJoinGame", "(Ljava/lang/String;)V");
		if(!mjoin)  env->ThrowNew(exception, "Couldn't find onJoinGame() method");

		mspectate = env->GetMethodID(callbackClass, "onSpectateGame", "(Ljava/lang/String;)V");
		if(!mspectate)  env->ThrowNew(exception, "Couldn't find onSpectateGame() method");

        mjoinrequest = env->GetMethodID(callbackClass, "onJoinRequest", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		if(!mjoinrequest) env->ThrowNew(exception, "Couldn't find onJoinRequest() method");
    */

    /** Used to decline a request via {@link #respond(String, int)} */
    public static final int replyNo = 0;
    /** Used to accept a request via {@link #respond(String, int)} */
    public static final int replyYes = 1;
    /** Currently unsused response, treated like NO. Used with {@link #respond(String, int)} */
    public static final int replyIgnore = 2;

    /**
     * Initializes the library, supply with application details and event handlers.
     * Handlers are only called when the {@link #runCallbacks()} method is invoked!
     * <br><b>Before closing the application it is recommended to call {@link #shutdown()}</b>
     * @param applicationId The ID for this RPC application,
     * retrieved from the <a href="https://discordappc.com/developers/applications/me" target="_blank">developer dashboard</a>
     * @param handle Nullable instance of {@link DiscordEventHandler}
     * @param autoRegister {@code true} to automatically register the game's steam and application ID
     * @param steamId Possible steam ID of the running game
     */
    public static native void initialize(String applicationId, DiscordEventHandler handle, boolean autoRegister, String steamId); /*
        DiscordEventHandlers handlers;
        memset(&handlers, 0, sizeof(handlers));

        handlers.ready = handleDiscordReady;
        handlers.disconnected = handleDiscordDisconnected;
        handlers.errored = handleDiscordError;
        handlers.joinGame = handleDiscordJoin;
        handlers.spectateGame = handleDiscordSpectate;
        handlers.joinRequest = handleDiscordJoinRequest;

        senv = env;
        callback = handle;

        Discord_Initialize(applicationId, &handlers, autoRegister, steamId);
    */

    /**
     * Shuts the RPC connection down.
     * If not currently connected, this does nothing.
     */
    public static native void shutdown(); /*
        Discord_Shutdown();
    */

    /**
     * Executes the registered handlers for currently queued events.
     * <br>If this is not called the handlers will not receive any events!
     *
     * <p>It is recommended to call this in a <u>2 second interval</u>
     */
    public static native void runCallbacks(); /*
        Discord_RunCallbacks();
    */

    /**
     * Updates the currently set presence of the logged in user.
     * <br>Note that the client only updates its presence every <b>15 seconds</b>
     * and queues all additional presence updates.
     * @param s The new presence to use
     * @see DiscordRichPresence
     */
    public static void updatePresence(DiscordRichPresence s){
        updatePresenceJni(s.state, s.details, s.startTimestamp, s.endTimestamp,
        s.largeImageKey, s.largeImageText, s.smallImageKey, s.smallImageText, s.partyId,
        s.partySize, s.partyMax, s.matchSecret, s.joinSecret, s.spectateSecret, s.instance);
    }

    /**
     * Clears the currently set presence.
     */
    public static native void clearPresence(); /*
        Discord_ClearPresence();
    */

    /**
     * Responds to the given user with the specified reply type.
     *
     * <h1>Possible Replies</h1>
     * <ul>
     *   <li>{@link #replyNo}</li>
     *   <li>{@link #replyYes}</li>
     *   <li>{@link #replyIgnore}</li>
     * </ul>
     * @param userid The id of the user to respond to
     * @param reply The reply type
     */
    public static native void respond(String userid, int reply); /*
        Discord_Respond(userid, reply);
    */

    /*
     * void Discord_Initialize(const char* applicationId, DiscordEventHandlers* handlers, int autoRegister, const char* optionalSteamId);
     * void Discord_Shutdown(void);
     * void Discord_RunCallbacks(void);
     * void Discord_UpdatePresence(const DiscordRichPresence* presence);
     * void Discord_ClearPresence(void);
     * void Discord_Respond(const char* userid, int reply);
     * void Discord_UpdateHandlers(DiscordEventHandlers* handlers);
     */

    private static native void updatePresenceJni(String state, String details, long startTimestamp, long endTimestamp, String largeImageKey, String largeImageText,
                                                    String smallImageKey, String smallImageText, String partyId, int partySize, int partyMax, String matchSecret,
                                                    String joinSecret, String spectateSecret, byte instance); /*
        DiscordRichPresence pres;
        memset(&pres, 0, sizeof(pres));

        pres.state = state;
        pres.details = details;
        pres.startTimestamp = startTimestamp;
        pres.endTimestamp = endTimestamp;
        pres.largeImageKey = largeImageKey;
        pres.largeImageText = largeImageText;
        pres.smallImageKey = smallImageKey;
        pres.smallImageText = smallImageText;
        pres.partyId = partyId;
        pres.partySize = partySize;
        pres.partyMax = partyMax;
        pres.matchSecret = matchSecret;
        pres.joinSecret = joinSecret;
        pres.spectateSecret = spectateSecret;
        pres.instance = instance;

        Discord_UpdatePresence(&pres);
    */

    /** Struct binding for a RichPresence */
    public static class DiscordRichPresence{

        /**
         * The user's current party status.
         * <br>Example: "Looking to Play", "Playing Solo", "In a Group"
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String state;

        /**
         * What the player is currently doing.
         * <br>Example: "Competitive - Captain's Mode", "In Queue", "Unranked PvP"
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String details;

        /**
         * Unix timestamp (seconds) for the start of the game.
         * <br>Example: 1507665886
         */
        public long startTimestamp;

        /**
         * Unix timestamp (seconds) for the start of the game.
         * <br>Example: 1507665886
         */
        public long endTimestamp;

        /**
         * Name of the uploaded image for the large profile artwork.
         * <br>Example: "default"
         *
         * <p><b>Maximum: 32 characters</b>
         */
        public String largeImageKey;

        /**
         * Tooltip for the largeImageKey.
         * <br>Example: "Blade's Edge Arena", "Numbani", "Danger Zone"
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String largeImageText;

        /**
         * Name of the uploaded image for the small profile artwork.
         * <br>Example: "rogue"
         *
         * <p><b>Maximum: 32 characters</b>
         */
        public String smallImageKey;

        /**
         * Tooltip for the smallImageKey.
         * <br>Example: "Rogue - Level 100"
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String smallImageText;

        /**
         * ID of the player's party, lobby, or group.
         * <br>Example: "ae488379-351d-4a4f-ad32-2b9b01c91657"
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String partyId;

        /**
         * Current size of the player's party, lobby, or group.
         * <br>Example: 1
         */
        public int partySize;

        /**
         * Maximum size of the player's party, lobby, or group.
         * <br>Example: 5
         */
        public int partyMax;

        /**
         * Unique hashed string for Spectate and Join.
         * Required to enable match interactive buttons in the user's presence.
         * <br>Example: "MmhuZToxMjMxMjM6cWl3amR3MWlqZA=="
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String matchSecret;

        /**
         * Unique hashed string for Spectate button.
         * This will enable the "Spectate" button on the user's presence if whitelisted.
         * <br>Example: "MTIzNDV8MTIzNDV8MTMyNDU0"
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String joinSecret;

        /**
         * Unique hashed string for chat invitations and Ask to Join.
         * This will enable the "Ask to Join" button on the user's presence if whitelisted.
         * <br>Example: "MTI4NzM0OjFpMmhuZToxMjMxMjM="
         *
         * <p><b>Maximum: 128 characters</b>
         */
        public String spectateSecret;

        /**
         * Marks the matchSecret as a game session with a specific beginning and end.
         * Boolean value of 0 or 1.
         * <br>Example: 1
         */
        public byte instance;
    }

    /**
     * Struct containing handlers for RPC events
     * <br>Provided handlers can be null.
     */
    public interface DiscordEventHandler{
        default void onReady(String id, String username, String discriminator, String avatar){}
        default void onDisconnected(int errorCode, String message){}
        default void onErrored(int errorCode, String message){}
        default void onJoinGame(String secret){}
        default void onSpectateGame(String secret){}
        default void onJoinRequest(String id, String username, String discriminator, String avatar){}
    }
}
