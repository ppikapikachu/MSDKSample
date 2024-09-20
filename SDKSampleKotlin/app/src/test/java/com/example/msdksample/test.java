package com.example.msdksample;

import android.os.Environment;

import org.junit.Test;

import java.io.File;

public class test {
    @Test
    public void aaa(){
        File file = Environment.getExternalStorageDirectory();
        System.out.println(file);
    }
}
