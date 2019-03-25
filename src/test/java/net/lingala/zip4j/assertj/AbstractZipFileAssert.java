package net.lingala.zip4j.assertj;

import org.assertj.core.api.AbstractAssert;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("NewClassNamingConvention")
public class AbstractZipFileAssert<SELF extends AbstractZipFileAssert<SELF>> extends AbstractAssert<SELF, ZipFile> {
    public AbstractZipFileAssert(ZipFile actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public AbstractZipEntryAssert<?> hasDirectory(String name) {
        if (!"/".equals(name)) {
            ZipEntry entry = actual.getEntry(name);

            Zip4jAssertions.assertThat(entry).isNotNull();
            Zip4jAssertions.assertThat(entry.isDirectory()).isTrue();
        }

        return Zip4jAssertionsForClassTypes.assertThat(new ZipEntry(name), actual);
    }
}
