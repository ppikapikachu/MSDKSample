package com.example.msdksample.utils
import kotlin.experimental.and

object  FrameUtil {

     fun getStartCodeSize(byteBuffer: ByteArray): Int {
        var startCodeSize = 0
        if (byteBuffer[0].toInt() == 0x00 && byteBuffer[1].toInt() == 0x00
            && byteBuffer[2].toInt() == 0x00 && byteBuffer[3].toInt() == 0x01
        ) {
            //match 00 00 00 01
            startCodeSize = 4
        } else if (byteBuffer[0].toInt() == 0x00 && byteBuffer[1].toInt() == 0x00
            && byteBuffer[2].toInt() == 0x01
        ) {
            //match 00 00 01
            startCodeSize = 3
        }
        return startCodeSize
    }


    fun checkFrameType(videoArray: ByteArray): Int {
        val startCodeSize = getStartCodeSize(videoArray)
        val naluType = (videoArray[startCodeSize] and 0x1f).toInt()
        if (naluType == 5) {
            return 1
        }
        if (naluType == 1 || naluType == 1 || naluType == 3 || naluType == 4 || naluType == 6) {
            return -1
        }
        if (naluType == 7 || naluType == 8) {
            return 2
        }
        return -1
    }

    fun getFrameType(data: ByteArray): Int {
        val offset = 4
        return if (data[0].toInt() or data[1].toInt() or data[2].toInt() or data[3].toInt() != 1) {
            -1
        } else {
            val type = data[offset].toInt() and 31
            when (type) {
                1 -> 2
                7 -> 1
                else -> -1
            }
        }
    }
}