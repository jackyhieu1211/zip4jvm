package com.cop.zip4j.io;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface MarkDataOutput extends DataOutput {

    void mark(String id);

    long getWrittenBytesAmount(String id);

}
