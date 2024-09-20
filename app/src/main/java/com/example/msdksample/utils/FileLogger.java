package com.example.msdksample.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

//用来打印数据到文件
public class FileLogger {

    private static final String LOG_FILE_NAME = "app_log.txt";

    public static void log(String message) {
        try {
            File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(logFile, true);
            PrintWriter pw = new PrintWriter(fos);
            pw.println(message);
            pw.flush();
            pw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
