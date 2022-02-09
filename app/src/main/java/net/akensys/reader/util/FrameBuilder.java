   /*
    * To change this license header, choose License Headers in Project Properties.
    * To change this template file, choose Tools | Templates
    * and open the template in the editor.
    */
   package net.akensys.reader.util;

   import java.io.UnsupportedEncodingException;
   import java.util.Calendar;
   import java.util.Date;

   /**
    * @class FrameBuilder
    * @author mathieu
    */
   public class FrameBuilder {

       /** \brief constant definition for the target datetime */
       private final int EXPECTED_DATETIME_LENGTH = 17;

       private final int[] Elason_header_definition = new int[] {
               0x00, 0x10, 0xFF, 0x01, 0x1c
       };


       private final int[] Elason_frame_ble_password  = new int[] {
               0x10, 0xef, 0x09, 0x01, 0x07, 0x45, 0x4c, 0x41, 0x31, 0x32
               , 0x33, 0x34
       };

       private final int[] Elason_frame_ble_frame_type  = new int[] {
               0x10, 0x39, 0x03, 0x01, 0x01, 0x01
       };

       private final int[] Elason_frame_to_update = new int[] {
               0x1f, 0x00, 0xff, 0x01, 0x05, 0x01, 0xff, 0x01, 0x01, 0x00      //< 10
               , 0x00, 0x01, 0x00, 0x01, 0x01, 0x00, 0x02, 0x06, 0x01, 0x00    //< 20
               , 0x01, 0x01, 0x03, 0x16, 0x01, 0x04, 0x00, 0x6e, 0x01, 0x04    //< 30
               , 0x01, 0x2a, 0x01, 0x04, 0x02, 0x02, 0x01, 0x04, 0x20, 0x92    //< 40
               , 0x01, 0x05, 0x01, 0x04, 0x21, 0x92, 0x01, 0x06, 0x02, 0x00    //< 50
               , 0x01, 0x02, 0x03, 0x09, 0x02, 0x04, 0x02, 0x0f, 0x02, 0x04    //< 60
               , 0x20, 0x94, 0xe1, 0x80, 0x02, 0x04, 0x21, 0x94, 0xe1, 0x81    //< 70
               , 0x02, 0x04, 0x22, 0x94, 0xe1, 0x82, 0x02, 0x04, 0x23, 0x94    //< 80
               , 0xe1, 0x83, 0x02, 0x04, 0x24, 0x94, 0xe1, 0x84, 0x02, 0x04    //< 90
               , 0x25, 0x94, 0xe1, 0x85, 0x02, 0x04, 0x26, 0x94, 0xe1, 0x86    //< 100
               , 0x02, 0x04, 0x27, 0x94, 0xe1, 0x87, 0x02, 0x04, 0x28, 0x94    //< 110
               , 0xe1, 0x88, 0x02, 0x04, 0x29, 0x94, 0xe1, 0x89, 0x02, 0x04    //< 120
               , 0x2a, 0x94, 0xe1, 0x8a, 0x02, 0x04, 0x2b, 0x94, 0xe1, 0x8b    //< 130
               , 0x02, 0x04, 0x2c, 0x94, 0xe1, 0x8c, 0x02, 0x04, 0x2d, 0x94    //< 140
               , 0xe1, 0x8d, 0x02, 0x04, 0x2e, 0x94, 0xe1, 0x8e, 0x10, 0x00    //< 150
               , 0x01, 0x11, 0x00, 0x01, 0x11, 0x03, 0xff, 0x11, 0x04, 0x00    //< 160
               , 0x57, 0x11, 0x04, 0x01, 0x07, 0x11, 0x04, 0x02, 0x13, 0x11    //< 170
               , 0x04, 0x20, 0x91, 0x0c, 0x03, 0x11, 0x04, 0x21, 0x91, 0x0c    //< 180
               , 0x04, 0x11, 0x04, 0x22, 0x10, 0x31, 0x11, 0x04, 0x23, 0x10    //< 190
               , 0x39, 0x11, 0x04, 0x24, 0x10, 0x38, 0x11, 0x04, 0x25, 0x10    //< 200
               , 0x32, 0x11, 0x04, 0x26, 0x10, 0x30, 0x11, 0x04, 0x27, 0x10    //< 210
               , 0x36, 0x11, 0x04, 0x28, 0x10, 0x32, 0x11, 0x04, 0x29, 0x10    //< 220
               , 0x35, 0x11, 0x04, 0x2a, 0x10, 0x31, 0x11, 0x04, 0x2b, 0x10    //< 230
               , 0x31, 0x11, 0x04, 0x2c, 0x10, 0x30, 0x11, 0x04, 0x2d, 0x10    //< 240
               , 0x31, 0x11, 0x04, 0x2e, 0x10, 0x31, 0x11, 0x04, 0x2f, 0x10    //< 250
               , 0x32, 0x11, 0x04, 0x30, 0x10, 0x2b, 0x11, 0x04, 0x31, 0x10    //< 260
               , 0x30, 0x11, 0x04, 0x32, 0x10, 0x31
       };

       private final int INDEX_CRC = 5;
       private final int INDEX_YEAR_01 = 185;
       private final int INDEX_YEAR_02 = 190;
       private final int INDEX_YEAR_03 = 195;
       private final int INDEX_YEAR_04 = 200;
       private final int INDEX_MONTH_01 = 205;
       private final int INDEX_MONTH_02 = 210;
       private final int INDEX_DAYS_01 = 215;
       private final int INDEX_DAYS_02 = 220;
       private final int INDEX_HOURS_01 = 225;
       private final int INDEX_HOURS_02 = 230;
       private final int INDEX_MINUTES_01 = 235;
       private final int INDEX_MINUTES_02 = 240;
       private final int INDEX_SECONDS_01 = 245;
       private final int INDEX_SECONDS_02 = 250;
       private final int INDEX_GMT_SIGN = 255;
       private final int INDEX_GMT_VALUE_01 = 260;
       private final int INDEX_GMT_VALUE_02 = 265;

       /**
        * @brief constructor
        */
       public FrameBuilder() { }

       /**
        * @fn prepareTimestampedFrame
        * @param datetime
        * @return
        */
       private int[] prepareTimestampedFrame(String datetime) {

           if(datetime.length() != EXPECTED_DATETIME_LENGTH) return new int[0];
           //
           byte[] bDateValues = Conversion.toAscii(datetime);
           //
           int[] updatedValues = new int[Elason_frame_to_update.length];
           int indexDate = 0;
           for(int index = 0; index < Elason_frame_to_update.length; index++){

               updatedValues[index] = Elason_frame_to_update[index];
               switch (index) {
                   case INDEX_YEAR_01:
                       updatedValues[INDEX_YEAR_01] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_YEAR_02:
                       updatedValues[INDEX_YEAR_02] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_YEAR_03:
                       updatedValues[INDEX_YEAR_03] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_YEAR_04:
                       updatedValues[INDEX_YEAR_04] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_MONTH_01:
                       updatedValues[INDEX_MONTH_01] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_MONTH_02:
                       updatedValues[INDEX_MONTH_02] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_DAYS_01:
                       updatedValues[INDEX_DAYS_01] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_DAYS_02:
                       updatedValues[INDEX_DAYS_02] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_HOURS_01:
                       updatedValues[INDEX_HOURS_01] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_HOURS_02:
                       updatedValues[INDEX_HOURS_02] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_MINUTES_01:
                       updatedValues[INDEX_MINUTES_01] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_MINUTES_02:
                       updatedValues[INDEX_MINUTES_02] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_SECONDS_01:
                       updatedValues[INDEX_SECONDS_01] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_SECONDS_02:
                       updatedValues[INDEX_SECONDS_02] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_GMT_SIGN:
                       updatedValues[INDEX_GMT_SIGN] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_GMT_VALUE_01:
                       updatedValues[INDEX_GMT_VALUE_01] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   case INDEX_GMT_VALUE_02:
                       updatedValues[INDEX_GMT_VALUE_02] = bDateValues[indexDate];
                       indexDate++;
                       break;
                   default:
                       break;
               }
           }
           //
           return updatedValues;
       }

       /**
        * @fn getCompleteFrame
        * @brief getter on the complete frame
        * @return
        */
       public String getCompleteFrame() {

           Date date = new Date();
           int year = Calendar.getInstance().get(Calendar.YEAR);
           int month = Calendar.getInstance().get(Calendar.MONTH);
           int day = Calendar.getInstance().get(Calendar.DATE);
           int hour = Calendar.getInstance().get(Calendar.HOUR);
           int minute = Calendar.getInstance().get(Calendar.MINUTE);
           int seconds = Calendar.getInstance().get(Calendar.SECOND);
           //
           String targetDate = String.format("%4d%2d%2d%2d%2d%2d+00", year, month, day, hour, minute, seconds);
           //
           return getCompleteFrame(targetDate);
       }

       /**
        * @fn getCompleteFrame
        * @brief getter on the complete frame
        * @return
        */
       public String getCompleteFrame(String formattedDatetime) {

           if(formattedDatetime.length() != EXPECTED_DATETIME_LENGTH) return "";
           //
           String completeCustomFrame = "";
           //
           int[] updatedValues = prepareTimestampedFrame(formattedDatetime);
           int[] finalValues = new int[Elason_header_definition.length + 1 + Elason_frame_ble_password.length + Elason_frame_ble_frame_type.length + updatedValues.length]; // size = header + crc + updatedframe
           //
           for(int i = 0; i< Elason_header_definition.length; i++)
           {
               finalValues[i] = Elason_header_definition[i];
           }
           //
           finalValues[INDEX_CRC] = Crc8.ComputeChecksum(Elason_header_definition, 0x00);
           //
           int currentIndex = 0;
           for(int i = 0; i< Elason_frame_ble_password.length; i++)
           {
               finalValues[i + INDEX_CRC + 1 + currentIndex] = Elason_frame_ble_password[i];
           }
           currentIndex+=Elason_frame_ble_password.length;
           for(int i = 0; i< Elason_frame_ble_frame_type.length; i++)
           {
               finalValues[i + INDEX_CRC + 1 + currentIndex] = Elason_frame_ble_frame_type[i];
           }
           currentIndex+=Elason_frame_ble_frame_type.length;
           for(int i = 0; i< updatedValues.length; i++)
           {
               finalValues[i + INDEX_CRC + 1 + currentIndex] = updatedValues[i];
           }
           finalValues[INDEX_CRC] = Crc8.ComputeChecksum(Elason_frame_ble_password, finalValues[INDEX_CRC]);
           finalValues[INDEX_CRC] = Crc8.ComputeChecksum(Elason_frame_ble_frame_type, finalValues[INDEX_CRC]);
           finalValues[INDEX_CRC] = Crc8.ComputeChecksum(updatedValues, finalValues[INDEX_CRC]);
           try {
               completeCustomFrame = Conversion.toHexadecimalString(finalValues);
           } catch (UnsupportedEncodingException ignored) {
           }
           //
           return completeCustomFrame;
       }
   }