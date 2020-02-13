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

// Originally based on https://www.shadertoy.com/view/4ljfRG

#ifdef GL_ES
	#define PRECISION highp
	precision PRECISION float;
#else
	#define PRECISION
#endif

const float NOISE_AMOUNT = 0.18;

varying vec2 v_texCoords;
uniform sampler2D u_texture0;
uniform float u_seed;

void main() {
	vec2 uv = v_texCoords;
    vec4 color = texture2D(u_texture0, v_texCoords);

    float n = fract(sin(dot(uv, vec2(u_seed + 12.9898, 78.233))) * 43758.5453);
    color *= (1.0 - NOISE_AMOUNT + n * NOISE_AMOUNT) * 1.1;
	gl_FragColor = color;
}