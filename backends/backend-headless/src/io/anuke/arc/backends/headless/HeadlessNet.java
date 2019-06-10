/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.backends.headless;

import io.anuke.arc.Net;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.util.NetJavaImpl;

/**
 * Headless implementation of the {@link Net} API, based on LWJGL implementation
 * @author acoppes
 * @author Jon Renner
 */
public class HeadlessNet implements Net{
    NetJavaImpl impl = new NetJavaImpl();

    @Override
    public void http(HttpRequest httpRequest, Consumer<HttpResponse> success, Consumer<Throwable> failure){
        impl.http(httpRequest, success, failure);
    }

    @Override
    public boolean openURI(String URI){
        return false; //unsupported
    }
}
