package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.08.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StoreZipData {

    public static void createStoreZip() throws IOException {
        createStoreSolidZip();
        createStoreSplitZip();
        createStoreSolidPkwareZip();
        createStoreSolidAesZip();
    }

    private static void createStoreSolidZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL).build())
                                                  .build();

        ZipIt.add(Zip4jSuite.storeSolidZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.storeSolidZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSolidZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(Zip4jSuite.storeSolidZip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    private static void createStoreSplitZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL).build())
                                                  .splitSize(1024 * 1024).build();
        ZipIt.add(Zip4jSuite.storeSplitZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.storeSplitZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSplitZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(11);
    }

    private static void createStoreSolidPkwareZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password).build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();
        ZipIt.add(Zip4jSuite.storeSolidPkwareZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.storeSolidPkwareZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSolidPkwareZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSolidPkwareZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createStoreSolidAesZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, String::toCharArray).build())
                                                  .comment("password: fileName").build();
        ZipIt.add(Zip4jSuite.storeSolidAesZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.storeSolidAesZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSolidAesZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSolidAesZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

}
