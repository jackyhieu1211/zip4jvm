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
package ru.olegcherednik.zip4jvm.snippets;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;

/**
 * @author Oleg Cherednik
 * @since 04.10.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class ZipItSnippet {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipItSnippet.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void createOrOpenExistedZipArchiveAndAddRegularFile() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        ZipIt.zip(zip).add(fileBentley);
    }

    public void createOrOpenExistedZipArchiveAndAddDirectory() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        ZipIt.zip(zip).add(dirCars);
    }

    public void createOrOpenExistedZipArchiveAndAddSomeRegularFilesAndDirectories() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        Collection<Path> paths = Arrays.asList(fileDucati, fileHonda, dirCars, fileSaintPetersburg);
        ZipIt.zip(zip).add(paths);
    }

    public void createOrOpenExistedZipArchiveAndAddSomeRegularFilesAndDirectoriesUsingStream() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.add(fileDucati);
            zipFile.add(fileHonda);
            zipFile.add(dirCars);
            zipFile.add(fileSaintPetersburg);
        }
    }

    public void createOrOpenExistedZipArchiveAndAddInputStreamsContentAsRegularFiles() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.add(ZipFile.Entry.builder()
                                     .inputStreamSupplier(() -> new FileInputStream(fileBentley.toFile()))
                                     .fileName("my_cars/bentley-continental.jpg")
                                     .uncompressedSize(Files.size(fileEmpty))
                                     .lastModifiedTime(System.currentTimeMillis()).build());

            zipFile.add(ZipFile.Entry.builder()
                                     .inputStreamSupplier(() -> new FileInputStream(fileKawasaki.toFile()))
                                     .fileName("my_bikes/kawasaki.jpg")
                                     .uncompressedSize(Files.size(fileKawasaki))
                                     .lastModifiedTime(System.currentTimeMillis()).build());
        }
    }

}
