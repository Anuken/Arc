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

package io.anuke.arc.utils;

import io.anuke.arc.graphics.Pixmap;

/**
 * A very simple clipboard interface for text and image content.
 * @author mzechner
 */
public interface Clipboard{
    /**
     * gets the current content of the clipboard if it contains text
     * @return the clipboard content or null
     */
    String getContents();

    /**
     * Sets the content of the system clipboard.
     * @param content the content as text
     */
    void setContents(String content);

    /** Sets the content of the clipboard as an image. */
    default void setContents(Pixmap pixmap){
        //not yet implemented
    }
}
