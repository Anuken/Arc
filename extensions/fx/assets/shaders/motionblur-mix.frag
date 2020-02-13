/*******************************************************************************
 * Copyright 2019 metaphore
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

// Simple motion blur implementation by Toni Sagrista
// Last frame is drawn with lower opacity

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Unprocessed image
uniform sampler2D u_texture0;
// Last frame
uniform sampler2D u_texture1;
// Last frame alpha
uniform float u_blurOpacity;

varying vec2 v_texCoords;

void main() {
    gl_FragColor = mix(texture2D(u_texture0, v_texCoords), texture2D(u_texture1, v_texCoords), u_blurOpacity);
}
