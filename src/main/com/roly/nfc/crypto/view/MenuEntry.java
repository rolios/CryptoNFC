package com.roly.nfc.crypto.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.roly.nfc.crypto.R;

public class MenuEntry extends FrameLayout{

    public MenuEntry(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.menu_entry, 0, 0);
        String text = a.getString(R.styleable.menu_entry_label);
        Drawable drawable = a.getDrawable(R.styleable.menu_entry_imageSrc);
        int background = a.getColor(R.styleable.menu_entry_backgroundColor, android.R.color.background_light);
        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.menu_entry, this, true);

        View frame = view.findViewById(R.id.menu_entry_frame);
        frame.setBackgroundColor(background);

        ImageView image = (ImageView) view.findViewById(R.id.menu_entry_image);
        image.setImageDrawable(drawable);

        TextView label = (TextView) view.findViewById(R.id.menu_entry_label);
        label.setText(text);

    }

}
