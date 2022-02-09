/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.akensys.reader.util;

/**
 *
 * @author mathieu
 */
public class Crc8 {

    /**
     * @fn ComputeChecksum
     * @param p_data
     * @param p_crc
     * @return
     */
    public static int ComputeChecksum(int[] p_data, int p_crc)
    {
        int crc = 0;
        if(p_crc != 0) crc = p_crc;
        int extract;
        int sum;

        for (int crcID1 = 0; crcID1 < p_data.length; crcID1++)
        {
            extract = p_data[crcID1];
            for (byte crcID2 = 8; crcID2 > 0; crcID2--)
            {
                sum = ((crc ^ extract) & 0x01);
                sum &= 0xff;
                crc &= 0xff;
                crc >>>= 1;
                crc &= 0xff;
                if (sum > 0)
                    crc ^= 0x8C;
                extract &= 0xff;
                extract >>>= 1;
                extract &= 0xff;
                //
            }
        }
        return crc;
    }
}