/*******************************************************************************
 * Copyright 2012 bmanuel
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

#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
#else
	#define PRECISION
#endif

uniform sampler2D u_texture0;
varying vec2 v_texCoords;

uniform float distortion;   // Default is 0.3
uniform float zoom;         // Default is 1

vec2 radialDistortion(vec2 coord) {
    vec2 cc = coord - 0.5;
    float dist = dot(cc, cc) * distortion;
    return (coord + cc * (1.0 + dist) * dist);
}

void main() {
    vec2 uv = radialDistortion(v_texCoords);
    uv = 0.5 + (uv-0.5)*(zoom);

    if (uv.s<0.0 || uv.s>1.0 || uv.t<0.0 || uv.t >1.0) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    gl_FragColor = vec4(texture2D(u_texture0, uv).rgb, 1.0);
}