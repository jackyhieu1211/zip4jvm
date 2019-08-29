package com.cop.zip4j.model.activity;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.IOException;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 23.08.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlainActivity implements Activity {

    public static final PlainActivity INSTANCE = new PlainActivity();

    // LocalFileHeader

    public long getCrc32LocalFileHeader(long crc32) {
        return crc32;
    }

    public long getCompressedSizeLocalFileHeader(long compressedSize) {
        return compressedSize;
    }

    public long getUncompressedSizeLocalFileHeader(long uncompressedSize) {
        return uncompressedSize;
    }

    public Supplier<Zip64.ExtendedInfo> getExtendedInfoLocalFileHeader(CentralDirectory.FileHeader fileHeader) {
        return () -> Zip64.ExtendedInfo.NULL;
    }

    // DataDescriptor

    public void writeValueDataDescriptor(long value, @NonNull DataOutput out) throws IOException {
        out.writeDword(value);
    }

    // FileHeader

    public LongSupplier getCompressedSizeFileHeader(LongSupplier originalCompressedSize) {
        return originalCompressedSize;
    }

    public LongSupplier getUncompressedSizeFileHeader(LongSupplier originalUncompressedSize) {
        return originalUncompressedSize;
    }

    public Supplier<Zip64.ExtendedInfo> getExtendedInfoFileHeader(CentralDirectory.FileHeader fileHeader) {
        return () -> Zip64.ExtendedInfo.NULL;
    }

    // ZipModel

//    public long getCentralDirectoryOffs(ZipModel zipModel) {
//        return zipModel.getEndCentralDirectory().getCentralDirectoryOffs();
//    }

//    public long getTotalEntries(ZipModel zipModel) {
//        return zipModel.getEndCentralDirectory().getTotalEntries();
//    }

//    public void incTotalEntries(ZipModel zipModel) {
//        zipModel.getEndCentralDirectory().incTotalEntries();
//    }

    // ENdCentralDirectory

    public int getTotalEntriesECD(ZipModel zipModel) {
        return zipModel.getEntries().size();
    }
}
