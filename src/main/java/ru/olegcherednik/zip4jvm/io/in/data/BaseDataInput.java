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
package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
abstract class BaseDataInput implements DataInput {

    private static final int OFFS_BYTE = 0;
    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    protected static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    private final Map<String, Long> map = new HashMap<>();

    @Override
    public int byteSize() {
        return 1;
    }

    @Override
    public int wordSize() {
        return 2;
    }

    @Override
    public int dwordSize() {
        return 4;
    }

    @Override
    public int qwordSize() {
        return 8;
    }

    @Override
    public int readByte() throws IOException {
        return (int)readAndToLong(OFFS_BYTE, byteSize());
    }

    @Override
    public int readWord() throws IOException {
        return (int)readAndToLong(OFFS_WORD, wordSize());
    }

    @Override
    public long readDword() throws IOException {
        return readAndToLong(OFFS_DWORD, dwordSize());
    }

    @Override
    public long readQword() throws IOException {
        return readAndToLong(OFFS_QWORD, qwordSize());
    }

    @Override
    public String readNumber(int bytes, int radix) throws IOException {
        if (bytes <= 0)
            return null;

        byte[] buf = readBytes(bytes);

        String hexStr = IntStream.rangeClosed(1, bytes)
                                 .map(i -> buf[buf.length - i] & 0xFF)
                                 .mapToObj(Integer::toHexString)
                                 .collect(Collectors.joining());

        return new BigInteger(hexStr, radix).toString();
    }

    private long readAndToLong(int offs, int len) throws IOException {
        byte[] buf = THREAD_LOCAL_BUF.get();
        read(buf, offs, len);
        return toLong(buf, offs, len);
    }

    @Override
    public String readString(int length, Charset charset) throws IOException {
        byte[] buf = readBytes(length);
        return buf.length == 0 ? null : new String(buf, charset);
    }

    @Override
    public byte[] readBytes(int total) throws IOException {
        if (total <= 0)
            return ArrayUtils.EMPTY_BYTE_ARRAY;

        byte[] buf = new byte[total];
        int n = read(buf, 0, buf.length);

        if (n == IOUtils.EOF)
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        if (n < total)
            return Arrays.copyOfRange(buf, 0, n);
        return buf;
    }

    @Override
    public void mark(String id) {
        map.put(id, getAbsoluteOffs());
    }

    @Override
    public long getMark(String id) {
        if (map.containsKey(id))
            return map.get(id);
        throw new Zip4jvmException("Cannot find mark: " + id);
    }

    @Override
    public void seek(String id) throws IOException {
        seek(getMark(id));
    }

}
