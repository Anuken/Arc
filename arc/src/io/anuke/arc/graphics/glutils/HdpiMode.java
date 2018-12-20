package io.anuke.arc.graphics.glutils;

import io.anuke.arc.Graphics;
import io.anuke.arc.graphics.GL20;

public enum HdpiMode{
    /**
     * mouse coordinates, {@link Graphics#getWidth()} and
     * {@link Graphics#getHeight()} will return logical coordinates
     * according to the system defined HDPI scaling. Rendering will be
     * performed to a backbuffer at raw resolution. Use {@link HdpiUtils}
     * when calling {@link GL20#glScissor} or {@link GL20#glViewport} which
     * expect raw coordinates.
     */
    Logical,

    /**
     * Mouse coordinates, {@link Graphics#getWidth()} and
     * {@link Graphics#getHeight()} will return raw pixel coordinates
     * irrespective of the system defined HDPI scaling.
     */
    Pixels
}
