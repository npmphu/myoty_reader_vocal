package net.akensys.reader.util;


import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author mathieu
 */
public class Conversion {

    /**
     * @fn toAscci
     * @brief convert the input data buffer into byte array
     * @param data : string to convert
     * @return
     */
    public static byte[] toAscii(String data)
    {
        byte[] b = new byte[data.length()];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) data.charAt(i);
        }
        return b;
    }

    /**
     *
     * @param data
     * @return
     */
    public static int[] toHexadecimal(String data) {

        int[] val = new int[data.length() / 2];
        for (int i = 0; i < val.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(data.substring(index, index + 2), 16);
            val[i] = j & 0xff;
        }
        return val;
    }

    /**
     * @fn toAsciiString
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toHexadecimalString(int[] data) throws UnsupportedEncodingException
    {
        StringBuilder values = new StringBuilder();
        for(int value : data) {
            values.append(String.format("%02X", value));
        }
        return values.toString();
    }

    /**
     * @fn toAsciiString
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toAsciiString(int[] data) throws UnsupportedEncodingException
    {
        byte[] bData = new byte[data.length];
        int index = 0;
        for(int value : data) {
            bData[index] = (byte)value;
            index++;
        }
        return toAsciiString(bData);
    }

    /**
     * @fn toAsciiString
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toAsciiString(byte[] data) throws UnsupportedEncodingException
    {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * Tool function to convert hex string to ascii char
     * @param hexStr hex string value to convert
     * @return Ascii string
     */
    public static String hexToAscii(String hexStr)
    {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    /**
     * Build string name to match wirepas tag
     * @param name Tag name
     * @return builded name
     */
    public static String buildWpName(String name)
    {
        name += hexToAscii("00");
        while(name.length() < 15)
            name += hexToAscii("20");
        while(name.length() <= 20)
            name += hexToAscii("00");
        return name;
    }

    /**
     * Build string name to match custom advertising tag
     * @param name Tag name
     * @return builded name
     */
    public static String buildCustomName(String name)
    {
        while(name.length() < 15)
            name += hexToAscii("00");
        return name;
    }
}

