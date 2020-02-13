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

// Simple hdr implementation by Toni Sagrista
// see https://learnopengl.com/#!Advanced-Lighting/HDR
//#version 120
#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec2 v_texCoords;

 // Unprocessed image
uniform sampler2D u_texture0;

uniform float u_exposure;
uniform float u_gamma;

void main() {
    vec3 hdrColor = texture2D(u_texture0, v_texCoords).rgb;

     // Exposure tone mapping
    vec3 mapped = vec3(1.0) - exp(-hdrColor * u_exposure);
    // Gamma correction
    mapped = pow(mapped, vec3(1.0 / u_gamma));

    gl_FragColor = vec4(mapped, 1.0);
}