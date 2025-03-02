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
import ru.olegcherednik.zip4jvm.io.in.data.ZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
public final class BlockModelReader extends BaseZipModelReader {

    private final Block endCentralDirectoryBlock = new Block();
    private final Zip64Block zip64Block = new Zip64Block();
    private final CentralDirectoryBlock centralDirectoryBlock = new CentralDirectoryBlock();

    public BlockModelReader(SrcZip srcZip, Function<Charset, Charset> customizeCharset) {
        super(srcZip, customizeCharset);
    }

    public BlockModel read() throws IOException {
        readCentralData();

        ZipModel zipModel = new ZipModelBuilder(srcZip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();

        return BlockModel.builder()
                         .zipModel(zipModel)
                         .endCentralDirectory(endCentralDirectory, endCentralDirectoryBlock)
                         .zip64(zip64, zip64Block)
                         .centralDirectory(centralDirectory, centralDirectoryBlock).build();
    }

    public BlockModel readWithEntries() throws IOException {
        readCentralData();

        ZipModel zipModel = new ZipModelBuilder(srcZip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();
        Map<String, ZipEntryBlock> zipEntries = new BlockZipEntryReader(zipModel, customizeCharset).read();

        return BlockModel.builder()
                         .zipModel(zipModel)
                         .zipEntries(zipEntries)
                         .endCentralDirectory(endCentralDirectory, endCentralDirectoryBlock)
                         .zip64(zip64, zip64Block)
                         .centralDirectory(centralDirectory, centralDirectoryBlock).build();
    }

    @Override
    protected DataInput createDataInput() throws IOException {
        return new ZipInputStream(srcZip);
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new BlockEndCentralDirectoryReader(customizeCharset, endCentralDirectoryBlock);
    }

    @Override
    protected Zip64Reader getZip64Reader() {
        return new BlockZip64Reader(zip64Block);
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        return new BlockCentralDirectoryReader(totalEntries, customizeCharset, centralDirectoryBlock);
    }

}

