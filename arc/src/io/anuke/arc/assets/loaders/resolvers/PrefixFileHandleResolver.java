/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

package io.anuke.arc.assets.loaders.resolvers;

import io.anuke.arc.assets.loaders.FileHandleResolver;
import io.anuke.arc.files.FileHandle;

/**
 * {@link FileHandleResolver} that adds a prefix to the filename before passing it to the base resolver. Can be used e.g. to use a
 * given subfolder from the base resolver. The prefix is added as is, you have to include any trailing '/' character if needed.
 * @author Xoppa
 */
public class PrefixFileHandleResolver implements FileHandleResolver{
    private String prefix;
    private FileHandleResolver baseResolver;

    public PrefixFileHandleResolver(FileHandleResolver baseResolver, String prefix){
        this.baseResolver = baseResolver;
        this.prefix = prefix;
    }

    public FileHandleResolver getBaseResolver(){
        return baseResolver;
    }

    public void setBaseResolver(FileHandleResolver baseResolver){
        this.baseResolver = baseResolver;
    }

    public String getPrefix(){
        return prefix;
    }

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    @Override
    public FileHandle resolve(String fileName){
        return baseResolver.resolve(prefix + fileName);
    }
}
