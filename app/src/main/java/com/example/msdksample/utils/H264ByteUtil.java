package com.example.msdksample.utils;

public class H264ByteUtil {

    /**
     * 判断是否为开始码: 00 00 01 或 00 00 00 01
     * @param buffer    流字节数据
     * @return
     */
    public static boolean isHeaderStart(byte[] buffer) {
        int mask = buffer[0] | buffer[1] | buffer[2] | buffer[3];
        return (mask == 1);
    }

    /**
     * 判断是否为SPS：00 00 00 01 67
     * @param buffer
     * @return
     */
    public static boolean isSPS(byte[] buffer) {
        boolean isHeader = isHeaderStart(buffer);
        if (isHeader) {
            return (buffer[4] & 0x1F) == 7;
        }
        return false;
    }

    // 00 00 00 01 65
    public static boolean isKeyFrame(byte[] buffer) {
        boolean isHeader = isHeaderStart(buffer);
        if (isHeader) {
            return (buffer[4] & 0x1F) == 5;
        }
        return false;
    }

    /**
     * 判断是否为P帧：00 00 00 01 61
     * @param buffer
     * @return
     */
    public static boolean isPFrame(byte[] buffer) {
        boolean isHeader = isHeaderStart(buffer);
        if (isHeader) {
            return (buffer[4] & 0x1F) == 1;
        }
        return false;
    }


}
