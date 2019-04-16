package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class Zip64EndCentralDirectoryReader {

    private final long offs;

    @NonNull
    public Zip64EndCentralDirectory read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        findHead(in);

        Zip64EndCentralDirectory dir = new Zip64EndCentralDirectory();
        dir.setSizeOfZip64EndCentralDirRec(in.readLong());
        dir.setVersionMadeBy(in.readShort());
        dir.setVersionNeededToExtract(in.readShort());
        dir.setDiskNumber(in.readInt());
        dir.setStartDiskNumber(in.readInt());
        dir.setDiskEntries(in.readLong());
        dir.setTotalEntries(in.readLong());
        dir.setSize(in.readLong());
        dir.setOffs(in.readLong());
        dir.setExtensibleDataSector(in.readBytes((int)(dir.getSizeOfZip64EndCentralDirRec() - Zip64EndCentralDirectory.SIZE)));

        return dir;
    }

    private void findHead(LittleEndianRandomAccessFile in) throws IOException {
        in.seek(offs);

        if (in.readInt() == InternalZipConstants.ZIP64_ENDSIG)
            return;

        throw new IOException("invalid zip64 end of central directory");
    }
}
