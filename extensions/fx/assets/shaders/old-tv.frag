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

// Originally based on
// https://www.shadertoy.com/view/MtlyDX

#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
#else
	#define PRECISION
#endif

uniform sampler2D u_texture0;
uniform vec2 u_resolution;
uniform float u_time;
varying vec2 v_texCoords;

vec3 scanline(vec2 coord, vec3 screen) {
    const float scale = 0.66;
    const float amt = 0.02; // intensity of effect
    const float spd = 1.0; // speed of scrolling rows transposed per second

	screen.rgb += vec3(sin((coord.y / scale - (u_time * spd * 6.28))) * amt);
	return screen;
}

vec3 channelSplit(sampler2D tex, vec2 coord) {
    const float spread = 0.008;
	vec3 frag;
	frag.r = texture2D(tex, vec2(coord.x - spread * sin(u_time), coord.y)).r;
	frag.g = texture2D(tex, vec2(coord.x, 					     coord.y)).g;
	frag.b = texture2D(tex, vec2(coord.x + spread * sin(u_time), coord.y)).b;
	return frag;
}

void main() {
	vec2 uv = v_texCoords;
	vec3 channelSplit = channelSplit(u_texture0, uv);
	vec2 screenSpace = uv * u_resolution.xy;
	vec3 scanline = scanline(screenSpace, channelSplit);
	gl_FragColor = vec4(scanline, 1.0);
}