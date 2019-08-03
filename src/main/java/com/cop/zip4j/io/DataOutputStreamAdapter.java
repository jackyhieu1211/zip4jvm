package com.cop.zip4j.io;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DataOutputStreamAdapter extends DataOutputStream {

    @NonNull
    protected DataOutputStream out;

    @Override
    public void seek(long bytes) throws IOException {
        out.seek(bytes);
    }

    @Override
    public long getFilePointer() throws IOException {
        return out.getFilePointer();
    }

    @Override
    public void writeDword(int val) throws IOException {
        out.writeDword(val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        out.writeDword(val);
    }

    @Override
    public void writeWord(int val) throws IOException {
        out.writeWord(val);
    }

    @Override
    public void writeQword(long val) throws IOException {
        out.writeQword(val);
    }

    @Override
    public void writeBytes(byte... buf) throws IOException {
        out.writeBytes(buf);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    @Override
    public int getCurrSplitFileCounter() {
        return out.getCurrSplitFileCounter();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
