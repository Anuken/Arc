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

package io.anuke.arc.postprocessing.filters;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.glutils.FrameBuffer;
import io.anuke.arc.postprocessing.PostEffect;
import io.anuke.arc.postprocessing.PostProcessor;
import io.anuke.arc.postprocessing.filters.Blur.BlurType;
import io.anuke.arc.postprocessing.utils.PingPongBuffer;

/**
 * Pseudo lens flare implementation. This is a post-processing effect entirely, no need for light positions or anything. It
 * includes ghost generation, halos, chromatic distortion and blur.
 * @author Toni Sagrista
 */
public final class LensFlareEffect extends PostEffect{
    private PingPongBuffer pingPongBuffer;

    public Lens lens;
    public Blur blur;
    public Bias bias;
    public Combine combine;
    public Blending blending = Blending.disabled;

    public LensFlareEffect(int fboWidth, int fboHeight){
        pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight);

        lens = new Lens(fboWidth, fboHeight);
        blur = new Blur(fboWidth, fboHeight);
        bias = new Bias();
        combine = new Combine();

        blur.type = BlurType.Gaussian5x5b;
        blur.passes = 2;
        bias.bias = -0.9f;

        combine.baseIntensity = 1f;
        combine.baseSaturation = 1f;

        combine.effectIntensity = 0.7f;
        combine.effectSaturation = 1f;
        lens.ghosts = 8;
        lens.haloWidth = 0.5f;
    }

    @Override
    public void dispose(){
        combine.dispose();
        bias.dispose();
        blur.dispose();
        pingPongBuffer.dispose();
    }

    @Override
    public void render(final FrameBuffer src, final FrameBuffer dest){
        Texture texsrc = src.getTexture();
        Core.gl.glDisable(GL20.GL_BLEND);

        pingPongBuffer.begin();
        {
            // apply bias
            bias.setInput(texsrc).setOutput(pingPongBuffer.getSourceBuffer()).render();

            lens.setInput(pingPongBuffer.getSourceBuffer()).setOutput(pingPongBuffer.getResultBuffer()).render();

            pingPongBuffer.set(pingPongBuffer.getResultBuffer(), pingPongBuffer.getSourceBuffer());

            // blur pass
            blur.render(pingPongBuffer);
        }
        pingPongBuffer.end();

        if(blending != Blending.disabled){
            Core.gl.glEnable(GL20.GL_BLEND);
            Core.gl.glBlendFunc(blending.src, blending.dst);
        }

        // mix original scene and blurred threshold, modulate via
        combine.setOutput(dest);
        combine.setInput(texsrc, pingPongBuffer.getResultTexture()).render();
    }

    @Override
    public void rebind(){
        blur.rebind();
        bias.rebind();
        combine.rebind();
        pingPongBuffer.rebind();
    }
}
