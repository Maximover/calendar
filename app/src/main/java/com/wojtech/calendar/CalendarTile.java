package com.wojtech.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

@SuppressLint("ViewConstructor")
public class CalendarTile extends androidx.appcompat.widget.AppCompatTextView {
    public static final int STYLE_ACTIVE_DAY = 1;
    public static final int STYLE_ACTIVE_MONTH = 2;
    public static final int STYLE_NONACTIVE_MONTH = 3;

    public CalendarTile(Context context, int tile_style) {
        super(context);
        this.setPadding(10,10,10,10);
        this.setTextSize(24f);
        this.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        this.setTextAlignment(TEXT_ALIGNMENT_CENTER);

        switch (tile_style){
            case STYLE_ACTIVE_DAY:
                this.setTextColor(context.getColor(R.color.white));
                this.setBackground(AppCompatResources.getDrawable(context, R.drawable.light_border));
                break;
            case STYLE_ACTIVE_MONTH:
                this.setTextColor(context.getColor(R.color.gray));
                this.setBackground(AppCompatResources.getDrawable(context, R.drawable.gray_border));
                break;
            case STYLE_NONACTIVE_MONTH:
                this.setTextColor(context.getColor(R.color.charcoal));
                this.setBackground(AppCompatResources.getDrawable(context, R.drawable.dark_border));
                break;
        }
    }
}
