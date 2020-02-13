/*******************************************************************************
 * Copyright 2012 tsagrista
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
precision mediump float;
precision mediump int;
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture0;
uniform float u_bias;

void main() {
	vec4 tex = texture2D(u_texture0, v_texCoords);
	float avg = (tex.r + tex.g + tex.b) / 3.0;
	gl_FragColor = vec4(max(0.0, avg + u_bias)) * 50.0;
}