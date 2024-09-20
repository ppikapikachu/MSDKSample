package com.example.msdksample.flight.takeoff;


import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

//import com.example.msdksample.MyLayoutActivity;
import com.example.msdksample.R;
import com.gosuncn.lib28181agent.GS28181SDKManager;

/**
 * 自定义组件
 * @author luminal
 * 基本写法：
 * 步骤1：重写2个构造方法。一个不带属性的构造方法，一个是带属性的构造方法。
 * 步骤2：在带属性的构造方法中，获取到属性，为属性设置值。
 * 步骤3：重写onDraw方法，进行绘制。
 * 步骤4：使用自定义组件，在Layout布局里面：R.layout.activity_main 进行调用
 *
 */
public class MyTakeOffWidget extends View implements View.OnClickListener {

    private Paint paint = null;//画笔
    private String text = null;
    private Rect mBounds = null;
    private static final String TAG="MyTakeOffWidget@@@";

    /**
     * 不带属性的构造方法
     * @param context
     */
    public MyTakeOffWidget(Context context) {
        super(context);

        Log.v(TAG, "***MyView不带属性的构造方法");

        paint = new Paint();//初始化画笔
        mBounds = new Rect();//返回在边界(分配)的最小矩形包含所有的字符,返回包含文本在内，所形成的边界。
        paint.setColor(Color.BLACK);//画笔颜色-黑色
        paint.setTextSize(20);//画笔尺寸

        Log.i("MyTakeOff","第一个构造方法");
        setOnClickListener(this);
    }


    /**
     * 带属性的构造方法
     * @param context
     * @param attrs
     */
    public MyTakeOffWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.v(TAG, "***MyView带属性的构造方法");

        paint = new Paint();//初始化画笔
        mBounds = new Rect();//返回在边界(分配)的最小矩形包含所有的字符,返回包含文本在内，所形成的边界。

        // 获取属性的方式
        // 这里取得 declare-styleable 集合
        TypedArray typedArray =  context.obtainStyledAttributes(attrs, R.styleable.MyTakeOffWidget);

        //参数1：直接内嵌在View中的属性集合。这里直接使用 MyView构造方法的参数2
        //参数2：想要获取的属性集合。
        //通常对应values文件下，自定义的attrs.xml资源文件里面，某个declare-styleable(风格样式)
        //obtainStyledAttributes(AttributeSet set, int[] attrs)

        // 这里从集合里取出相对应的属性值。 参数2：如果使用者没有配置该属性时，所用的默认值
        int color = typedArray.getColor(R.styleable.MyTakeOffWidget_textColor,  Color.GREEN);
        float size = typedArray.getDimension(R.styleable.MyTakeOffWidget_textSize, 26);
        text = typedArray.getString(R.styleable.MyTakeOffWidget_text);

        Log.i("size",Float.toString(size));
        paint.setTextSize(size);//设置自己的类成员变量
        paint.setColor(color);//画笔颜色-绿色
        typedArray.recycle();//关闭资源

//        button = new Button(context);
//        button.setTextColor(color);
//        button.setTextSize(size);
//        button.setText(text);

    }


    /**
     * 初始化组件的时候，被触发，进行组件的渲染
     * Canvas canva 画布
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.v(TAG, "***onDraw方法");

        paint.setStyle(Style.FILL);//设置画笔的风格-实心
        canvas.drawRect(10, 10, getWidth(), getHeight(), paint);//绘制矩形
        paint.getTextBounds(text,0,text.length(),mBounds);
        paint.setColor(Color.BLUE);//画笔颜色-蓝色
        float textWidth = mBounds.width();
        float textHeight = mBounds.height();
        canvas.drawText(text, getWidth()/2 - textWidth/2, getHeight()/2 + textHeight/2, paint);//绘制文本

    }

    @Override
    public void onClick(View view) {
        Log.i("MyTakeOff","初始化initSDK");
        int i = GS28181SDKManager.getInstance().initSDK("192.168.0.118");
        Log.i("MyTakeOff","initSDK"+i);
    }
}




