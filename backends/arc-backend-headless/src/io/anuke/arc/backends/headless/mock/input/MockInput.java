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

package io.anuke.arc.backends.headless.mock.input;

import io.anuke.arc.Input;

/**
 * The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockInput extends Input{
    @Override
    public int mouseX(){
        return 0;
    }

    @Override
    public int mouseX(int pointer){
        return 0;
    }

    @Override
    public int deltaX(){
        return 0;
    }

    @Override
    public int deltaX(int pointer){
        return 0;
    }

    @Override
    public int mouseY(){
        return 0;
    }

    @Override
    public int mouseY(int pointer){
        return 0;
    }

    @Override
    public int deltaY(){
        return 0;
    }

    @Override
    public int deltaY(int pointer){
        return 0;
    }

    @Override
    public boolean isTouched(){
        return false;
    }

    @Override
    public boolean justTouched(){
        return false;
    }

    @Override
    public boolean isTouched(int pointer){
        return false;
    }

    @Override
    public long getCurrentEventTime(){
        return 0;
    }
}
