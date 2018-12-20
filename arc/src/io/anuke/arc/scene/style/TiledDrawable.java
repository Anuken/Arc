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

package io.anuke.arc.scene.style;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.Texture;
import io.anuke.arc.graphics.g2d.TextureRegion;

import static io.anuke.arc.Core.graphics;

/**
 * Draws a {@link TextureRegion} repeatedly to fill the area, instead of stretching it.
 * @author Nathan Sweet
 */
public class TiledDrawable extends TextureRegionDrawable{
    private final Color color = new Color(1, 1, 1, 1);
    private float tileWidth, tileHeight;

    public TiledDrawable(){
        super();
    }

    public TiledDrawable(TextureRegion region){
        super(region);
    }

    public TiledDrawable(TextureRegionDrawable drawable){
        super(drawable);
    }

    public void setRegion(TextureRegion region){
        super.setRegion(region);
        this.tileWidth = region.getWidth();
        this.tileHeight = region.getHeight();
    }

    public void setTileSize(float w, float h){
        tileWidth = w;
        tileHeight = h;
    }

    @Override
    public void draw(float x, float y, float width, float height){
        TextureRegion region = getRegion();
        float regionWidth = tileWidth, regionHeight = tileHeight;
        int fullX = (int)(width / regionWidth), fullY = (int)(height / regionHeight);
        float remainingX = width - regionWidth * fullX, remainingY = height - regionHeight * fullY;
        float startX = x, startY = y;
        for(int i = 0; i < fullX; i++){
            y = startY;
            for(int ii = 0; ii < fullY; ii++){
                graphics.batch().draw().tex(region).set(x, y, regionWidth, regionHeight).color(color);
                y += regionHeight;
            }
            x += regionWidth;
        }
        Texture texture = region.getTexture();
        float u = region.getU();
        float v2 = region.getV2();
        if(remainingX > 0){
            // Right edge.
            float u2 = u + remainingX / texture.getWidth();
            float v = region.getV();
            y = startY;
            for(int ii = 0; ii < fullY; ii++){
                graphics.batch().draw().tex(texture).set(x, y, remainingX, remainingY).uv(u, v2, u2, v).color(color);
                y += regionHeight;
            }
            // Upper right corner.
            if(remainingY > 0){
                v = v2 - remainingY / texture.getHeight();
                graphics.batch().draw().tex(texture).set(x, y, remainingX, remainingY).uv(u, v2, u2, v).color(color);
            }
        }
        if(remainingY > 0){
            // Top edge.
            float u2 = region.getU2();
            float v = v2 - remainingY / texture.getHeight();
            x = startX;
            for(int i = 0; i < fullX; i++){
                graphics.batch().draw().tex(texture).set(x, y, remainingX, remainingY).uv(u, v2, u2, v).color(color);
                x += regionWidth;
            }
        }
    }

    @Override
    public void draw(float x, float y, float originX, float originY, float width, float height, float scaleX,
                     float scaleY, float rotation){
        throw new UnsupportedOperationException();
    }

    public Color getColor(){
        return color;
    }

    public TiledDrawable tint(Color tint){
        TiledDrawable drawable = new TiledDrawable(this);
        drawable.color.set(tint);
        drawable.setLeftWidth(getLeftWidth());
        drawable.setRightWidth(getRightWidth());
        drawable.setTopHeight(getTopHeight());
        drawable.setBottomHeight(getBottomHeight());
        return drawable;
    }
}
