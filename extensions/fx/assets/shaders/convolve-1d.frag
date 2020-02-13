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

#ifndef LENGTH
#error Please define a LENGTH
#endif

varying vec2 v_texCoords;
uniform PRECISION sampler2D u_texture0;
uniform PRECISION vec2 SampleOffsets[LENGTH];
uniform PRECISION float SampleWeights[LENGTH];

void main()
{
	vec4 c = vec4(0);

	// Combine a number of weighted image filter taps.
	for (int i = 0; i < LENGTH; i++)
	{
		c += texture2D(u_texture0, v_texCoords + SampleOffsets[i]) * SampleWeights[i];
	}

	gl_FragColor = c;
}