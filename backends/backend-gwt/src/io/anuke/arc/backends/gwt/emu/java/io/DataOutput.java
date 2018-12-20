/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.io;

public interface DataOutput{
    void write(byte[] data) throws IOException;

    void write(byte[] data, int ofs, int len) throws IOException;

    void write(int v) throws IOException;

    void writeBoolean(boolean v) throws IOException;

    void writeByte(int v) throws IOException;

    void writeBytes(String s) throws IOException;

    void writeChar(int v) throws IOException;

    void writeChars(String s) throws IOException;

    void writeDouble(double v) throws IOException;

    void writeFloat(float v) throws IOException;

    void writeInt(int v) throws IOException;

    void writeLong(long v) throws IOException;

    void writeShort(int v) throws IOException;

    void writeUTF(String s) throws IOException;
}
