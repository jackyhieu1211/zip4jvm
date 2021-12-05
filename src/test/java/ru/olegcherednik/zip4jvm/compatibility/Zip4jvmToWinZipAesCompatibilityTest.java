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
package ru.olegcherednik.zip4jvm.compatibility;

import de.idyl.winzipaes.AesZipFileDecrypter;
import de.idyl.winzipaes.impl.AESDecrypterJCA;
import de.idyl.winzipaes.impl.ExtZipEntry;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Charsets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DataFormatException;

import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidAes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 15.08.2019
 */
@Test
@SuppressWarnings({ "NewClassNamingConvention", "FieldNamingConvention" })
public class Zip4jvmToWinZipAesCompatibilityTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(Zip4jvmToWinZipAesCompatibilityTest.class);

    public void checkCompatibilityWithWinZipAes() throws IOException, DataFormatException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        AesZipFileDecrypter decrypter = new AesZipFileDecrypter(zipDeflateSolidAes.toFile(), new AESDecrypterJCA());
        AesZipFileDecrypter.charset = Charsets.UTF_8.name();

        for (ExtZipEntry zipEntry : decrypter.getEntryList()) {
            Path path = dstDir.resolve(zipEntry.getName());

            if (zipEntry.isDirectory())
                Files.createDirectories(path);
            else {
                Files.createDirectories(path.getParent());

                if (zipEntry.getSize() == 0)
                    Files.createFile(path);
                else {
                    if (!Files.exists(path))
                        Files.createFile(path);

                    decrypter.extractEntry(zipEntry, path.toFile(), zipEntry.getName());
                }
            }
        }

        assertThatDirectory(dstDir).matches(rootAssert);
    }

}
