package com.cop.zip4j.core.writers;

import com.cop.zip4j.io.DataOutput;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordWriter {

    @NonNull
    private final AesExtraDataRecord record;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        if (record == AesExtraDataRecord.NULL)
            return;

        out.writeWord(AesExtraDataRecord.SIGNATURE);
        out.writeWord(record.getDataSize());
        out.writeWord(record.getVersionNumber());
        out.writeBytes(record.getVendor(charset));
        out.writeBytes((byte)record.getStrength().getRawCode());
        out.writeWord(record.getCompressionMethod().getValue());
    }

}
