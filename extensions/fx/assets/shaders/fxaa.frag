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

// FXAA shader, GLSL code adapted from following source with 4 extra sampling directions (NSEW):
// http://horde3d.org/wiki/index.php5?title=Shading_Technique_-_FXAA
// Whitepaper describing the technique:
// http://developer.download.nvidia.com/assets/gamedev/files/sdk/11/FXAA_WhitePaper.pdf

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D u_texture0;

// The inverse of the viewport dimensions along X and Y
uniform vec2 u_viewportInverse;
uniform float u_fxaaReduceMin;
uniform float u_fxaaReduceMul;
uniform float u_fxaaSpanMax;

varying vec2 v_texCoords;

vec4 fxaa(sampler2D texture, vec2 texCoords, vec2 viewportInv) {
	vec3 rgbNW = texture2D(texture, texCoords.xy + (vec2(-1.0, -1.0) * viewportInv)).xyz;
	vec3 rgbNE = texture2D(texture, texCoords.xy + (vec2(+1.0, -1.0) * viewportInv)).xyz;
	vec3 rgbSW = texture2D(texture, texCoords.xy + (vec2(-1.0, +1.0) * viewportInv)).xyz;
	vec3 rgbSE = texture2D(texture,	texCoords.xy + (vec2(+1.0, +1.0) * viewportInv)).xyz;
	vec3 rgbN = texture2D(texture, texCoords.xy + (vec2(0.0, -1.0) * viewportInv)).xyz;
	vec3 rgbS = texture2D(texture, texCoords.xy + (vec2(0.0, 1.0) * viewportInv)).xyz;
	vec3 rgbE= texture2D(texture, texCoords.xy + (vec2(1.0, 0.0) * viewportInv)).xyz;
	vec3 rgbW= texture2D(texture, texCoords.xy + (vec2(-1.0, 0.0) * viewportInv)).xyz;
	vec3 rgbM = texture2D(texture, texCoords.xy).xyz;

	vec3 luma = vec3(0.299, 0.587, 0.114);
	float lumaNW = dot(rgbNW, luma);
	float lumaNE = dot(rgbNE, luma);
	float lumaSW = dot(rgbSW, luma);
	float lumaSE = dot(rgbSE, luma);
	float lumaN = dot(rgbN, luma);
	float lumaS = dot(rgbS, luma);
	float lumaE = dot(rgbE, luma);
	float lumaW = dot(rgbW, luma);
	float lumaM = dot(rgbM, luma);

	float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, min(lumaSE, min(lumaN, min(lumaS, min(lumaE, lumaW)))))));
	float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, max(lumaSE, max(lumaN, max(lumaS, max(lumaE, lumaW)))))));

	vec2 dir;
	dir.x = abs(lumaN + lumaS - 2.0 * lumaM) * 2.0 +
	        abs(lumaNE + lumaSE - 2.0 * lumaE) +
	        abs(lumaNW + lumaSW - 2.0 * lumaW);
	dir.y = abs(lumaE + lumaW - 2.0 * lumaM) * 2.0 +
	        abs(lumaNE + lumaNW - 2.0 * lumaN) +
	        abs(lumaSE + lumaSW - 2.0 * lumaS);

	float dirReduce = max(
			(lumaNW + lumaNE + lumaSW + lumaSE + lumaN + lumaS + lumaE + lumaW) * (0.125 * u_fxaaReduceMul),
			u_fxaaReduceMin);

	float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);

	dir = min(vec2(u_fxaaSpanMax, u_fxaaSpanMax),
			max(vec2(-u_fxaaSpanMax, -u_fxaaSpanMax), dir * rcpDirMin))
			* viewportInv;

	vec3 rgbA =	0.5	* (texture2D(texture, texCoords.xy + dir * (1.0 / 3.0 - 0.5)).xyz +
					   texture2D(texture, texCoords.xy + dir * (2.0 / 3.0 - 0.5)).xyz);
	vec3 rgbB =	rgbA * 0.5 + 0.25 * (texture2D(texture, texCoords.xy + dir * (0.0 / 3.0 - 0.5)).xyz +
									 texture2D(texture, texCoords.xy + dir * (3.0 / 3.0 - 0.5)).xyz);
	float lumaB = dot(rgbB, luma);

	vec4 color = vec4(0.0);

	if ((lumaB < lumaMin) || (lumaB > lumaMax)) {
		color.xyz = rgbA;
	} else {
		color.xyz = rgbB;
	}

#ifdef SUPPORT_ALPHA
	color.a = texture2D(texture, texCoords.xy).a;
#else
	color.a = 1.0;
#endif

	return color;
}

void main() {
	gl_FragColor = fxaa(u_texture0, v_texCoords, u_viewportInverse);
}