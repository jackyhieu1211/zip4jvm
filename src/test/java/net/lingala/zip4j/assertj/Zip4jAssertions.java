package net.lingala.zip4j.assertj;

import lombok.experimental.UtilityClass;
import org.assertj.core.api.Assertions;

import java.util.zip.ZipFile;

/**
 * @author Oleg Cherednik
 * @since 24.03.2019
 */
@UtilityClass
@SuppressWarnings("ExtendsUtilityClass")
public class Zip4jAssertions extends Assertions {

    public AbstractZipFileAssert<?> assertThat(ZipFile actual) {
        return Zip4jAssertionsForClassTypes.assertThat(actual);
    }
}
