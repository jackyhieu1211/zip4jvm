/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.olegcherednik.zip4jvm.io.zstd;

import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_INT;
import static ru.olegcherednik.zip4jvm.io.zstd.Constants.SIZE_OF_LONG;

class DoubleFastBlockCompressor implements BlockCompressor {

    private static final int MIN_MATCH = 3;
    private static final int SEARCH_STRENGTH = 8;
    private static final int REP_MOVE = Constants.REPEATED_OFFSET_COUNT - 1;

    public int compressBlock(byte[] inputBase, final int inputAddress, int inputSize, SequenceStore output, BlockCompressionState state,
            RepeatedOffsets offsets, CompressionParameters parameters) {
        int matchSearchLength = Math.max(parameters.getSearchLength(), 4);

        // Offsets in hash tables are relative to baseAddress. Hash tables can be reused across calls to compressBlock as long as
        // baseAddress is kept constant.
        // We don't want to generate sequences that point before the current window limit, so we "filter" out all results from looking up in the hash tables
        // beyond that point.
        final int baseAddress = state.getBaseAddress();
        final long windowBaseAddress = baseAddress + state.getWindowBaseOffset();

        int[] longHashTable = state.hashTable;
        int longHashBits = parameters.getHashLog();

        int[] shortHashTable = state.chainTable;
        int shortHashBits = parameters.getChainLog();

        final long inputEnd = inputAddress + inputSize;
        final long inputLimit = inputEnd - SIZE_OF_LONG; // We read a long at a time for computing the hashes

        int input = inputAddress;
        int anchor = inputAddress;

        int offset1 = offsets.getOffset0();
        int offset2 = offsets.getOffset1();

        int savedOffset = 0;

        if (input - windowBaseAddress == 0) {
            input++;
        }
        int maxRep = (int)(input - windowBaseAddress);

        if (offset2 > maxRep) {
            savedOffset = offset2;
            offset2 = 0;
        }

        if (offset1 > maxRep) {
            savedOffset = offset1;
            offset1 = 0;
        }

        while (input < inputLimit) {   // < instead of <=, because repcode check at (input+1)
            int shortHash = hash(inputBase, input, shortHashBits, matchSearchLength);
            int shortMatchAddress = baseAddress + shortHashTable[shortHash];

            int longHash = hash8(UnsafeUtil.getLong(inputBase, input), longHashBits);
            int longMatchAddress = baseAddress + longHashTable[longHash];

            // update hash tables
            int current = input - baseAddress;
            longHashTable[longHash] = current;
            shortHashTable[shortHash] = current;

            int matchLength;
            int offset;

            if (offset1 > 0 && UnsafeUtil.getInt(inputBase, input + 1 - offset1) == UnsafeUtil.getInt(inputBase, input + 1)) {
                // found a repeated sequence of at least 4 bytes, separated by offset1
                matchLength = count(inputBase, input + 1 + SIZE_OF_INT, inputEnd, input + 1 + SIZE_OF_INT - offset1) + SIZE_OF_INT;
                input++;
                output.storeSequence(inputBase, anchor, input - anchor, 0, matchLength - MIN_MATCH);
            } else {
                // check prefix long match
                if (longMatchAddress > windowBaseAddress && UnsafeUtil.getLong(inputBase, longMatchAddress) == UnsafeUtil.getLong(inputBase, input)) {
                    matchLength = count(inputBase, input + SIZE_OF_LONG, inputEnd, longMatchAddress + SIZE_OF_LONG) + SIZE_OF_LONG;
                    offset = input - longMatchAddress;
                    while (input > anchor && longMatchAddress > windowBaseAddress && UnsafeUtil.getByte(inputBase, input - 1) == UnsafeUtil.getByte(
                            inputBase, longMatchAddress - 1)) {
                        input--;
                        longMatchAddress--;
                        matchLength++;
                    }
                } else {
                    // check prefix short match
                    if (shortMatchAddress > windowBaseAddress && UnsafeUtil.getInt(inputBase, shortMatchAddress) == UnsafeUtil.getInt(inputBase,
                            input)) {
                        int nextOffsetHash = hash8(UnsafeUtil.getLong(inputBase, input + 1), longHashBits);
                        int nextOffsetMatchAddress = baseAddress + longHashTable[nextOffsetHash];
                        longHashTable[nextOffsetHash] = current + 1;

                        // check prefix long +1 match
                        if (nextOffsetMatchAddress > windowBaseAddress && UnsafeUtil.getLong(inputBase, nextOffsetMatchAddress) == UnsafeUtil.getLong(
                                inputBase, input + 1)) {
                            matchLength = count(inputBase, input + 1 + SIZE_OF_LONG, inputEnd, nextOffsetMatchAddress + SIZE_OF_LONG) + SIZE_OF_LONG;
                            input++;
                            offset = (int)(input - nextOffsetMatchAddress);
                            while (input > anchor && nextOffsetMatchAddress > windowBaseAddress && UnsafeUtil.getByte(inputBase, input - 1) ==
                                    UnsafeUtil.getByte(inputBase, nextOffsetMatchAddress - 1)) {
                                input--;
                                nextOffsetMatchAddress--;
                                matchLength++;
                            }
                        } else {
                            // if no long +1 match, explore the short match we found
                            matchLength = count(inputBase, input + SIZE_OF_INT, inputEnd, shortMatchAddress + SIZE_OF_INT) + SIZE_OF_INT;
                            offset = (int)(input - shortMatchAddress);
                            while (input > anchor && shortMatchAddress > windowBaseAddress && UnsafeUtil.getByte(inputBase, input - 1) ==
                                    UnsafeUtil.getByte(inputBase, shortMatchAddress - 1)) {
                                input--;
                                shortMatchAddress--;
                                matchLength++;
                            }
                        }
                    } else {
                        input += ((input - anchor) >> SEARCH_STRENGTH) + 1;
                        continue;
                    }
                }

                offset2 = offset1;
                offset1 = offset;

                output.storeSequence(inputBase, anchor, input - anchor, offset + REP_MOVE, matchLength - MIN_MATCH);
            }

            input += matchLength;
            anchor = input;

            if (input <= inputLimit) {
                // Fill Table
                longHashTable[hash8(UnsafeUtil.getLong(inputBase, baseAddress + current + 2), longHashBits)] = current + 2;
                shortHashTable[hash(inputBase, baseAddress + current + 2, shortHashBits, matchSearchLength)] = current + 2;

                longHashTable[hash8(UnsafeUtil.getLong(inputBase, input - 2), longHashBits)] = input - 2 - baseAddress;
                shortHashTable[hash(inputBase, input - 2, shortHashBits, matchSearchLength)] = input - 2 - baseAddress;

                while (input <= inputLimit && offset2 > 0 && UnsafeUtil.getInt(inputBase, input) == UnsafeUtil.getInt(inputBase, input - offset2)) {
                    int repetitionLength = count(inputBase, input + SIZE_OF_INT, inputEnd, input + SIZE_OF_INT - offset2) + SIZE_OF_INT;

                    // swap offset2 <=> offset1
                    int temp = offset2;
                    offset2 = offset1;
                    offset1 = temp;

                    shortHashTable[hash(inputBase, input, shortHashBits, matchSearchLength)] = input - baseAddress;
                    longHashTable[hash8(UnsafeUtil.getLong(inputBase, input), longHashBits)] = input - baseAddress;

                    output.storeSequence(inputBase, anchor, 0, 0, repetitionLength - MIN_MATCH);

                    input += repetitionLength;
                    anchor = input;
                }
            }
        }

        // save reps for next block
        offsets.saveOffset0(offset1 != 0 ? offset1 : savedOffset);
        offsets.saveOffset1(offset2 != 0 ? offset2 : savedOffset);

        // return the last literals size
        return (int)(inputEnd - anchor);
    }

    // TODO: same as LZ4RawCompressor.count

    /**
     * matchAddress must be < inputAddress
     */
    public static int count(byte[] inputBase, final int inputAddress, final long inputLimit, final int matchAddress) {
        int input = inputAddress;
        int match = matchAddress;

        int remaining = (int)(inputLimit - inputAddress);

        // first, compare long at a time
        int count = 0;
        while (count < remaining - (SIZE_OF_LONG - 1)) {
            long diff = UnsafeUtil.getLong(inputBase, match) ^ UnsafeUtil.getLong(inputBase, input);
            if (diff != 0) {
                return count + (Long.numberOfTrailingZeros(diff) >> 3);
            }

            count += SIZE_OF_LONG;
            input += SIZE_OF_LONG;
            match += SIZE_OF_LONG;
        }

        while (count < remaining && UnsafeUtil.getByte(inputBase, match) == UnsafeUtil.getByte(inputBase, input)) {
            count++;
            input++;
            match++;
        }

        return count;
    }

    private static int hash(byte[] inputBase, int inputAddress, int bits, int matchSearchLength) {
        switch (matchSearchLength) {
            case 8:
                return hash8(UnsafeUtil.getLong(inputBase, inputAddress), bits);
            case 7:
                return hash7(UnsafeUtil.getLong(inputBase, inputAddress), bits);
            case 6:
                return hash6(UnsafeUtil.getLong(inputBase, inputAddress), bits);
            case 5:
                return hash5(UnsafeUtil.getLong(inputBase, inputAddress), bits);
            default:
                return hash4(UnsafeUtil.getInt(inputBase, inputAddress), bits);
        }
    }

    private static final int PRIME_4_BYTES = 0x9E3779B1;
    private static final long PRIME_5_BYTES = 0xCF1BBCDCBBL;
    private static final long PRIME_6_BYTES = 0xCF1BBCDCBF9BL;
    private static final long PRIME_7_BYTES = 0xCF1BBCDCBFA563L;
    private static final long PRIME_8_BYTES = 0xCF1BBCDCB7A56463L;

    private static int hash4(int value, int bits) {
        return (value * PRIME_4_BYTES) >>> (Integer.SIZE - bits);
    }

    private static int hash5(long value, int bits) {
        return (int)(((value << (Long.SIZE - 40)) * PRIME_5_BYTES) >>> (Long.SIZE - bits));
    }

    private static int hash6(long value, int bits) {
        return (int)(((value << (Long.SIZE - 48)) * PRIME_6_BYTES) >>> (Long.SIZE - bits));
    }

    private static int hash7(long value, int bits) {
        return (int)(((value << (Long.SIZE - 56)) * PRIME_7_BYTES) >>> (Long.SIZE - bits));
    }

    private static int hash8(long value, int bits) {
        return (int)((value * PRIME_8_BYTES) >>> (Long.SIZE - bits));
    }

}
