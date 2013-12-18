package com.roly.nfc.crypto.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

public class LineEditText extends EditText {

    private static Paint linePaint;

    static {
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    public LineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int lineHeight = getLineHeight();
        int totalLines = Math.max(getLineCount(), getHeight() / lineHeight);

        int voffset = getExtendedPaddingTop();

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int width = canvas.getWidth() - paddingLeft - paddingRight;

        for (int i = 1; i < totalLines; i++) {
            int lineY = voffset + i * lineHeight;
            canvas.drawLine(paddingLeft, lineY, width, lineY, linePaint);
        }

        super.onDraw(canvas);
    }

}
