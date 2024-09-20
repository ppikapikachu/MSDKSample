package com.example.msdksample.flight.takeoff;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dji.v5.ux.core.base.widget.IconButtonWidget;

public class MyGoHomeWidget extends androidx.appcompat.widget.AppCompatButton {

    public MyGoHomeWidget(@NonNull Context context) {
        super(context);
    }

    public MyGoHomeWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGoHomeWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ShapeButton);
//        normal_color = ta.getColor(R.styleable.ShapeButton_normal_color, Color.parseColor("#FF3333"));
//        pressed_color = ta.getColor(R.styleable.ShapeButton_pressed_color, Color.parseColor("#CC3333"));
//        enabled_color = ta.getColor(R.styleable.ShapeButton_enabled_color, Color.GRAY);
//        radius_size = (int) ta.getDimension(R.styleable.ShapeButton_radius_size, dip2px(4));
//        gravity = ta.getInt(R.styleable.ShapeButton_android_gravity, Gravity.CENTER);
////        int textColor = attrs.getAttributeIntValue(
////                "http://schemas.android.com/apk/res/android", "textColor", Color.WHITE);
////        setTextColor(textColor);
//        ta.recycle();
//        TypedArray tar = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.textColor, android.R.attr.paddingTop, android.R.attr.paddingBottom});
//        if (tar != null) {
//            setTextColor(tar.getColor(0, Color.WHITE));
//            setPadding(6, (int) tar.getDimension(1, 8), 6, (int) tar.getDimension(2, 8));
//        }
//        setGravity(gravity);
//        tar.recycle();
//        init();
    }


}
