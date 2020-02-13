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
// https://github.com/spite/Wagner/blob/master/fragment-shaders/chromatic-aberration-fs.glsl

#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
#else
	#define PRECISION
#endif

#ifndef PASSES
#error Please define PASSES
#endif

const int NUM_ITER = PASSES;
const float RECI_NUM_ITER_F = 1.0 / float(NUM_ITER);

uniform sampler2D u_texture0;
uniform float u_maxDistortion;

varying vec2 v_texCoords;

vec2 barrelDistortion(vec2 coord, float amt) {
	vec2 cc = coord - 0.5;
	float dist = dot(cc, cc);
	return coord + cc * dist * amt;
}

float sat(float t) {
	return clamp(t, 0.0, 1.0);
}

float linterp(float t) {
	return sat(1.0 - abs(2.0 * t - 1.0));
}

float remap(float t, float a, float b) {
	return sat((t - a) / (b - a));
}

vec4 spectrumOffset(float t) {
	vec4 ret;
	float lo = step(t, 0.5);
	float hi = 1.0 - lo;
	float w = linterp(remap(t, 1.0 / 6.0, 5.0 / 6.0));
	ret = vec4(lo, 1.0, hi, 1.0) * vec4(1.0 - w, w, 1.0 - w, 1.0);

	return pow(ret, vec4(1.0 / u_maxDistortion));
}
void main() {
	vec2 uv = v_texCoords;

	vec4 sumcol = vec4(0.0);
	vec4 sumw = vec4(0.0);
	for (int i = 0; i < NUM_ITER; ++i) {
		float t = float(i) * RECI_NUM_ITER_F;
		vec4 w = spectrumOffset(t);
		sumw += w;
		sumcol += w * texture2D(u_texture0, barrelDistortion(uv, 0.6 * u_maxDistortion * t));
	}

	gl_FragColor = sumcol / sumw;
}