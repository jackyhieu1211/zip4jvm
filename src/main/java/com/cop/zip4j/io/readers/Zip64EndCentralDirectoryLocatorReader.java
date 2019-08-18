package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryLocatorReader {

    private final long offs;

    @NonNull
    public Zip64.EndCentralDirectoryLocator read(@NonNull DataInput in) throws IOException {
        if (!findHead(in))
            return null;

        Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
        locator.setStartDiskNumber(in.readDword());
        locator.setOffs(in.readQword());
        locator.setTotalDisks(in.readDword());

        ZipUtils.requirePositive(locator.getOffs(), "Zip64.EndCentralDirectory");

        return locator;
    }

    private boolean findHead(DataInput in) throws IOException {
        if (offs < 0)
            throw new Zip4jException("EndCentralDirectory offs is unknown");

        long offs = this.offs - Zip64.EndCentralDirectoryLocator.SIZE;

        if (offs < 0)
            return false;

        in.seek(offs);
        return in.readSignature() == Zip64.EndCentralDirectoryLocator.SIGNATURE;
    }
}
