package ru.olegcherednik.zip4jvm.compression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestDataAssert;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.storeSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.storeSplitZip;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionStoreTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(CompressionStoreTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateSingleZipWithFilesWhenStoreCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, filesDirCars, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).directory("/").matches(TestDataAssert.zipCarsDirAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenStoreCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .splitSize(1024 * 1024).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, filesDirCars, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(3);
        // TODO check split zip file
    }

    public void shouldCreateSingleZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.dirCars, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zip).directory("cars/").matches(TestDataAssert.zipCarsDirAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .splitSize(1024 * 1024).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.dirCars, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(3);
        // TODO check split zip file
    }

    public void shouldUnzipWhenStoreCompression() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(storeSolidZip, destDir);
        assertThatDirectory(destDir).matches(TestDataAssert.dirAssert);
    }

    public void shouldUnzipWhenSplitAndStoreCompression() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(storeSplitZip, destDir);
        assertThatDirectory(destDir).matches(TestDataAssert.dirAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndPkwareEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, filesDirCars, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).directory("/").matches(TestDataAssert.zipCarsDirAssert);

        Path dirUnzip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        UnzipIt.extract(zip, dirUnzip, fileName -> Zip4jvmSuite.password);
        assertThatDirectory(dirUnzip).matches(TestDataAssert.dirCarsAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndAesEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.AES_256, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, filesDirCars, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).directory("/").matches(TestDataAssert.zipCarsDirAssert);

        Path dirUnzip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        UnzipIt.extract(zip, dirUnzip, fileName -> Zip4jvmSuite.password);
        assertThatDirectory(dirUnzip).matches(TestDataAssert.dirCarsAssert);
    }

}
