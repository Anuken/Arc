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
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.scene.ui.layout.Unit;

import static io.anuke.arc.Core.graphics;

/**
 * Drawable for a {@link TextureRegion}.
 * @author Nathan Sweet
 */
public class TextureRegionDrawable extends BaseDrawable implements TransformDrawable{
    private TextureRegion region;
    private Color tint = new Color(1f, 1f, 1f);

    /** Creates an uninitialized TextureRegionDrawable. The texture region must be set before use. */
    public TextureRegionDrawable(){
    }

    public TextureRegionDrawable(TextureRegion region){
        setRegion(region);
    }

    public TextureRegionDrawable(TextureRegionDrawable drawable){
        super(drawable);
        setRegion(drawable.region);
    }

    @Override
    public void draw(float x, float y, float width, float height){
        graphics.batch().draw().tex(region).pos(x, y).size(width, height).color(tint);
    }

    @Override
    public void draw(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
        graphics.batch().draw().tex(region).pos(x, y).size(width, height)
        .origin(originX, originY).scl(scaleX, scaleY).rot(rotation).color(tint);
    }

    public TextureRegion getRegion(){
        return region;
    }

    public void setRegion(TextureRegion region){
        this.region = region;
        setMinWidth(Unit.dp.scl(region.getWidth()));
        setMinHeight(Unit.dp.scl(region.getHeight()));
    }

    /** Creates a new drawable that renders the same as this drawable tinted the specified color. */
    public Drawable tint(Color tint){
        TextureRegionDrawable drawable = new TextureRegionDrawable(region);
        drawable.tint.set(tint);
        return drawable;
    }
}
