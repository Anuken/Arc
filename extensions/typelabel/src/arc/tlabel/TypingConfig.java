package arc.tlabel;

import arc.struct.*;
import arc.func.*;
import arc.graphics.Color;
import arc.tlabel.effects.*;

/** Configuration class that easily allows the user to fine tune the library's functionality. */
public class TypingConfig{

    /**
     * Whether or not <a href="https://github.com/libgdx/libgdx/wiki/Color-Markup-Language">LibGDX's Color Markup
     * Language</a> should be enabled when parsing a {@link TypeLabel}. Note that this library doesn't truly handle
     * colors, but simply convert them to the markup format. If markup is disabled, color tokens will be ignored.
     */
    public static boolean FORCE_COLOR_MARKUP_BY_DEFAULT = true;

    /** Default time in seconds that an empty {@code WAIT} token should wait for. Default value is {@code 0.250}. */
    public static float DEFAULT_WAIT_VALUE = 0.250f;

    /** Time in seconds that takes for each char to appear in the default speed. Default value is {@code 0.035}. */
    public static float DEFAULT_SPEED_PER_CHAR = 0.035f;

    /**
     * Minimum value for the {@code SPEED} token. This value divides {@link #DEFAULT_SPEED_PER_CHAR} to calculate the
     * final speed. Keep it above zero. Default value is {@code 0.001}.
     */
    public static float MIN_SPEED_MODIFIER = 0.001f;

    /**
     * Maximum value for the {@code SPEED} token. This value divides {@link #DEFAULT_SPEED_PER_CHAR} to calculate the
     * final speed. Default value is {@code 100}.
     */
    public static float MAX_SPEED_MODIFIER = 100.0f;

    /**
     * Defines how many chars can appear per frame. Use a value less than {@code 1} to disable this limit. Default value
     * is {@code -1}.
     */
    public static int CHAR_LIMIT_PER_FRAME = -1;

    /** Default color for the {@code CLEARCOLOR} token. Can be overriden by {@link TypeLabel#getClearColor()}. */
    public static Color DEFAULT_CLEAR_COLOR = new Color(Color.white);

    /**
     * Returns a map of characters and their respective interval multipliers, of which the interval to the next char
     * should be multiplied for.
     */
    public static ObjectFloatMap<Character> INTERVAL_MULTIPLIERS_BY_CHAR = new ObjectFloatMap<>();

    /** Map of global variables that affect all {@link TypeLabel} instances at once. */
    public static final ObjectMap<String, String> GLOBAL_VARS = new ObjectMap<>();

    /** Map of start tokens and their effect classes. Internal use only. */
    static final ObjectMap<String, Func<TypeLabel, Effect>> EFFECTS = new ObjectMap<>();

    /** Whether or not effect tokens are dirty and need to be recalculated. */
    static boolean dirtyEffectMaps = true;

    /**
     * Registers a new effect to TypeLabel.
     *
     * @param tokenName Name of the token that starts the effect, such as WAVE.
     */
    public static void registerEffect(String tokenName, Func<TypeLabel, Effect> effect){
        EFFECTS.put(tokenName, effect);
        dirtyEffectMaps = true;
    }

    /**
     * Unregisters an effect from TypeLabel.
     *
     * @param tokenName Name of the token that starts the effect, such as WAVE.
     */
    public static void unregisterEffect(String tokenName){
        EFFECTS.remove(tokenName);
    }

    static{
        // Generate default char intervals
        INTERVAL_MULTIPLIERS_BY_CHAR.put(' ', 0.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put(':', 1.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put(',', 2.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('.', 2.5f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('!', 5.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('?', 5.0f);
        INTERVAL_MULTIPLIERS_BY_CHAR.put('\n', 20f);

        // Register default tokens
        registerEffect("ease", EaseEffect::new);
        registerEffect("jump", JumpEffect::new);
        registerEffect("shake", ShakeEffect::new);
        registerEffect("sick", SickEffect::new);
        registerEffect("wave", WaveEffect::new);
        registerEffect("wind", WindEffect::new);
        registerEffect("rainbow", RainbowEffect::new);
        registerEffect("gradient", GradientEffect::new);
        registerEffect("fade", FadeEffect::new);
        registerEffect("blink", BlinkEffect::new);
    }

}
