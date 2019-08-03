package com.cop.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.LittleEndianReadFile;
import com.cop.zip4j.model.Zip64;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryLocatorReader {

    private final long offs;

    @NonNull
    public Zip64.EndCentralDirectoryLocator read(@NonNull LittleEndianReadFile in) throws IOException {
        if (!findHead(in))
            return null;

        Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
        locator.setNoOfDiskStartOfZip64EndOfCentralDirRec(in.readDword());
        locator.setOffs(in.readQword());
        locator.setTotNumberOfDiscs(in.readDword());

        return locator;
    }

    private boolean findHead(LittleEndianReadFile in) throws IOException {
        if (offs < 0)
            throw new Zip4jException("EndCentralDirectory offs is unknown");

        long offs = this.offs - Zip64.EndCentralDirectoryLocator.SIZE;

        if (offs < 0)
            return false;

        in.seek(offs);
        return in.readDword() == Zip64.EndCentralDirectoryLocator.SIGNATURE;
    }
}
