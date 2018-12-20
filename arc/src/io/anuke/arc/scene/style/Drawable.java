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

/**
 * A drawable knows how to draw itself at a given rectangular size. It provides border sizes and a minimum size so that other code
 * can determine how to size and position content.
 * @author Nathan Sweet
 */
public interface Drawable{
    /** Draws this drawable at the specified bounds. */
    void draw(float x, float y, float width, float height);

    void draw(float x, float y, float originX, float originY, float width, float height, float scaleX,
              float scaleY, float rotation);

    float getLeftWidth();

    void setLeftWidth(float leftWidth);

    float getRightWidth();

    void setRightWidth(float rightWidth);

    float getTopHeight();

    void setTopHeight(float topHeight);

    float getBottomHeight();

    void setBottomHeight(float bottomHeight);

    float getMinWidth();

    void setMinWidth(float minWidth);

    float getMinHeight();

    void setMinHeight(float minHeight);
}
