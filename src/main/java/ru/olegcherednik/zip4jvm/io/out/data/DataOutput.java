/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.io.out.data;

import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.Marker;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataOutput extends Marker, Closeable {

    long getRelativeOffs();

    void writeByte(int val) throws IOException;

    default void writeWordSignature(int sig) throws IOException {
        writeWord(sig);
    }

    default void writeDwordSignature(int sig) throws IOException {
        writeDword(sig);
    }

    void writeWord(int val) throws IOException;

    void writeDword(long val) throws IOException;

    void writeQword(long val) throws IOException;

    default void writeBytes(byte... buf) throws IOException {
        if (ArrayUtils.isNotEmpty(buf))
            write(buf, 0, buf.length);
    }

    void write(byte[] buf, int offs, int len) throws IOException;

    default int getDiskNo() {
        return 0;
    }

    void flush() throws IOException;

    /* this is technical method; update {@literal byte[]} from given {@literal long}  */
    @Deprecated
    void fromLong(long val, byte[] buf, int offs, int len);

}
