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
package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldRecordReader;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockExtraFieldReader extends ExtraFieldReader {

    private final ExtraFieldBlock extraFieldBlock;

    public BlockExtraFieldReader(int size, Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> readers,
            ExtraFieldBlock extraFieldBlock) {
        super(size, readers);
        this.extraFieldBlock = extraFieldBlock;
    }

    @Override
    protected ExtraField readExtraField(DataInput in) throws IOException {
        return extraFieldBlock.calcSize(in, () -> super.readExtraField(in));
    }

    @Override
    protected ExtraFieldRecordReader getExtraFieldRecordReader() {
        return new BlockExtraFieldRecordReader(readers, extraFieldBlock);
    }

}
