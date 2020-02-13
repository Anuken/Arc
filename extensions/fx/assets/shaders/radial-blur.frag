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

#ifndef PASSES
#error Please define PASSES
#endif

const int passes = PASSES;

varying vec2 v_texCoord0;

uniform sampler2D u_texture0;
uniform float u_blurDiv;
uniform float u_offsetX;
uniform float u_offsetY;
uniform float u_zoom;

void main() {
	vec2 offset = vec2(u_offsetX, u_offsetY);
	vec4 color = vec4(0.0);
	float zoom = u_zoom;
	for( int i = 0; i < passes; ++i )	{
		color += texture2D(u_texture0, (v_texCoord0 * zoom) + offset);
		zoom += u_blurDiv;
	}
	gl_FragColor = color / vec4(float(passes));
}