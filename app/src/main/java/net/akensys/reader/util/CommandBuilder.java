/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.akensys.reader.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mathieu
 * @class Command build for Elason and date time
 */
public class CommandBuilder {

    /**
     * \brief constant for the minimum frame size
     */
    private final int HEADER_TRUNCATED_FRAME_SIZE = 5;

    /**
     * \brief frame truncated identifier
     */
    private final int TAG_FRAME_TRUNCATES = 0x8000;    // truncated frame

    /**
     * @brief constructor
     */
    public CommandBuilder() {
    }

    /**
     * @param value : input vaue to convert into byte array
     * @return associtaed byte value
     * @fn getBytes
     */
    private byte[] getBytes(int value) {
        byte[] result = new byte[2];
        result[1] = (byte) (value & 0xff);
        result[0] = (byte) ((value >> 8) & 0xff);
        return result;
    }

    /**
     * @param fullData
     * @param iTruncatedFrameSize
     * @return formatted byte array
     * @fn unwrap
     */
    public List<int[]> unwrap(int[] fullData, int iTruncatedFrameSize) {
        if (iTruncatedFrameSize <= HEADER_TRUNCATED_FRAME_SIZE) return new ArrayList<int[]>();
        if (iTruncatedFrameSize > 255) return new ArrayList<int[]>();
        //
        List<int[]> result = new ArrayList<int[]>();
        //
        double dNumberBlocks = (double) (fullData.length) / (double) (iTruncatedFrameSize - HEADER_TRUNCATED_FRAME_SIZE);
        int iNumberBlocks = (int) Math.ceil(dNumberBlocks);
        int iTotalLengthLeft = fullData.length;
        int sourceOffset = 0;
        //
        for (int i = 0; i < iNumberBlocks; i++) {
            int[] truncatedBuffer = new int[iTruncatedFrameSize];
            //
            int bufferLength = (int) Math.min(iTotalLengthLeft, iTruncatedFrameSize - HEADER_TRUNCATED_FRAME_SIZE);
            if (bufferLength < (iTruncatedFrameSize - HEADER_TRUNCATED_FRAME_SIZE)) {
                truncatedBuffer = new int[iTotalLengthLeft + HEADER_TRUNCATED_FRAME_SIZE];
            }
            //
            byte[] id0 = getBytes(TAG_FRAME_TRUNCATES);
            truncatedBuffer[0] = id0[0] & 0x0000ff;
            truncatedBuffer[1] = id0[1] & 0x0000ff;
            truncatedBuffer[2] = (byte) bufferLength;
            truncatedBuffer[3] = (byte) iNumberBlocks;
            truncatedBuffer[4] = (byte) (i + 1);
            //
            //Buffer.BlockCopy(fullData, sourceOffset, truncatedBuffer, 5, bufferLength);
            System.arraycopy(fullData, sourceOffset, truncatedBuffer, 5, bufferLength);
            sourceOffset += bufferLength;
            iTotalLengthLeft -= bufferLength;
            //
            result.add(truncatedBuffer);
        }
        //
        return result;
    }

    /**
     * @param completeFrame
     * @param iTruncatedFrameSize
     * @return list of formatted ascci string
     * @fn getFormattedDateCommand
     */
    public List<String> getFormattedDateCommand(String completeFrame, int iTruncatedFrameSize) {

        List<String> results = new ArrayList<>();
        //
        if (!"".equals(completeFrame)) {
            int[] bValues = Conversion.toHexadecimal(completeFrame);
            List<int[]> bResults = unwrap(bValues, iTruncatedFrameSize);
            //
            bResults.forEach(result -> {
                try {
                    results.add(Conversion.toHexadecimalString(result));
                } catch (UnsupportedEncodingException ignored) {
                }
            });
        }
        //
        return results;
    }
}
