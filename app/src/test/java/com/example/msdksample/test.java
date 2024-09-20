package com.example.msdksample;

import android.os.Environment;

import com.gosuncn.lib28181agent.bean.AngleEvent;

import org.junit.Test;

import java.io.File;

public class test {
    @Test
    public void aaa(){
        countCmos(4.8f,6.4f,24);
    }
    public void countCmos(float cmosH, float cmosW, int currentZoom) {
        AngleEvent angleEvent = new AngleEvent();
        float h = (float)(2.0D * Math.atan((double)(cmosW / (float)(2 * currentZoom))) * 360.0D / 2.0D / 3.141592653589793D);
        float v = (float)(2.0D * Math.atan((double)(cmosH / (float)(2 * currentZoom))) * 360.0D / 2.0D / 3.141592653589793D);
        System.out.println(h+"==="+v);
    }
}
