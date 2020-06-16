package arc.discord;

import arc.util.*;

/**
 * Core library binding for the official <a href="https://github.com/discordapp/discord-rpc" target="_blank">Discord RPC SDK</a>.
 * Supports Windows/Mac/Linux. No 32-bit support.
 * Has no support for callbacks or any interaction besides setting the displayed rich presence. Use the SDK for that.
 */
public class DiscordRPC{

    /*JNI

    #include <discord-rpc/linux-dynamic/include/discord_rpc.h>
    #include <string.h>

	 */

    static{
        new SharedLibraryLoader(){
            @Override public String mapLibraryName(String libraryName){ return OS.isWindows ? "discord-rpc.dll" : OS.isLinux ? "libdiscord-rpc.so" : "libdiscord-rpc.dylib"; }
        }.load("discord-rpc");

        new SharedLibraryLoader().load("arc-discord");
    }

    /**
     * Initializes the library, supply with application details.
     * <br><b>Before closing the application it is recommended to call {@link #shutdown()}</b>
     * @param applicationId The ID for this RPC application,
     * retrieved from the <a href="https://discordappc.com/developers/applications/me" target="_blank">developer dashboard</a>
     * @param autoRegister {@code true} to automatically register the game's steam and application ID
     * @param steamId Possible steam ID of the running game
     */
    public static native void initialize(String applicationId, boolean autoRegister, String steamId); /*
        DiscordEventHandlers handlers;
        memset(&handlers, 0, sizeof(handlers));
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
                                                    String joinSecret, String spectateSecret, byte instance); /*MANUAL

        char* state = obj_state ? (char*)env->GetStringUTFChars(obj_state, 0) : 0;
        char* details = obj_details ? (char*)env->GetStringUTFChars(obj_details, 0) : 0;
        char* largeImageKey = obj_largeImageKey ? (char*)env->GetStringUTFChars(obj_largeImageKey, 0) : 0;
        char* largeImageText = obj_largeImageText ? (char*)env->GetStringUTFChars(obj_largeImageText, 0) : 0;
        char* smallImageKey = obj_smallImageKey ? (char*)env->GetStringUTFChars(obj_smallImageKey, 0) : 0;
        char* smallImageText = obj_smallImageText ? (char*)env->GetStringUTFChars(obj_smallImageText, 0) : 0;
        char* partyId = obj_partyId ? (char*)env->GetStringUTFChars(obj_partyId, 0) : 0;
        char* matchSecret = obj_matchSecret ? (char*)env->GetStringUTFChars(obj_matchSecret, 0) : 0;
        char* joinSecret = obj_joinSecret ? (char*)env->GetStringUTFChars(obj_joinSecret, 0) : 0;
        char* spectateSecret = obj_spectateSecret ? (char*)env->GetStringUTFChars(obj_spectateSecret, 0) : 0;

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

        if(obj_state) env->ReleaseStringUTFChars(obj_state, state);
        if(obj_details) env->ReleaseStringUTFChars(obj_details, details);
        if(obj_largeImageKey) env->ReleaseStringUTFChars(obj_largeImageKey, largeImageKey);
        if(obj_largeImageText) env->ReleaseStringUTFChars(obj_largeImageText, largeImageText);
        if(obj_smallImageKey) env->ReleaseStringUTFChars(obj_smallImageKey, smallImageKey);
        if(obj_smallImageText) env->ReleaseStringUTFChars(obj_smallImageText, smallImageText);
        if(obj_partyId) env->ReleaseStringUTFChars(obj_partyId, partyId);
        if(obj_matchSecret) env->ReleaseStringUTFChars(obj_matchSecret, matchSecret);
        if(obj_joinSecret) env->ReleaseStringUTFChars(obj_joinSecret, joinSecret);
        if(obj_spectateSecret) env->ReleaseStringUTFChars(obj_spectateSecret, spectateSecret);
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
}
