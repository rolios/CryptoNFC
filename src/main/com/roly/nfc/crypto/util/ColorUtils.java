package com.roly.nfc.crypto.util;

import android.graphics.Color;

public class ColorUtils {

    private ColorUtils(){}

    public static int darken(int color, float value) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= value;
        return Color.HSVToColor(hsv);
    }

}
