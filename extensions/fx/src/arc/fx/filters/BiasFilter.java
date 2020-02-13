/*******************************************************************************
 * Copyright 2012 tsagrista
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

package arc.fx.filters;

import arc.*;
import arc.fx.*;

/**
 * Bias filter.
 * @author Toni Sagrista
 */
public final class BiasFilter extends FxFilter{
    public float bias;

    public BiasFilter(){
        super(compileShader(
        Core.files.classpath("shaders/screenspace.vert"),
        Core.files.classpath("bias")));
        rebind();
    }

    @Override
    public void setParams(){
        shader.setUniformf("u_texture0", u_texture0);
        shader.setUniformf("u_bias", bias);
    }
}
