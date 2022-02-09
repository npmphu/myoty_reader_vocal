package net.akensys.reader.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Utils {
    public static byte[] getBytes(int[] data) {
        byte[] bytes = new byte[data.length];
        int index = 0;
        for(int value : data) {
            // bytes[index] = (byte)value;
            // Unsigned byte
            bytes[index] = (byte) ((byte)value & 0xff);
            index++;
        }
        return bytes;
    }

    public static String toHexadecimalString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static void writeToFile(Context context, String fileName, String fileData) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(fileData);
            outputStreamWriter.close();
        } catch (IOException ignored) { }
    }

    public static String readFromFile(Context context, String fileName) {
        String fileData = "";
        try {
            InputStream inputStream = context.openFileInput(fileName);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String receiveString;
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                fileData = stringBuilder.toString();
            }
        }
        catch (IOException ignored) { }
        return fileData;
    }

    public static Boolean deleteFile(Context context, String fileName) {
        return context.deleteFile(fileName);
    }
}
