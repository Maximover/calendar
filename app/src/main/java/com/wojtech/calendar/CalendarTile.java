package com.wojtech.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;

@SuppressLint("ViewConstructor")
public class CalendarTile extends androidx.appcompat.widget.AppCompatTextView {
    public static final int STYLE_ACTIVE_DAY = 1;
    public static final int STYLE_ACTIVE_MONTH = 2;
    public static final int STYLE_NONACTIVE_MONTH = 3;
    public static final int STYLE_HOLIDAY = 4;
    public static final int STYLE_ACTIVE_HOLIDAY = 5;
    public static final int STYLE_NONACTIVE_HOLIDAY = 6;

    private final Context context;
    private boolean is_holiday, is_active, is_today;


    public CalendarTile(Context context, boolean is_holiday) {
        super(context);
        this.context = context;
        this.is_holiday = is_holiday;
        this.is_active = true;
        this.is_today = false;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(5, 5, 5, 5);
        this.setLayoutParams(params);
        this.setPadding(10,10,10,10);
        this.setTextSize(24f);
        this.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        this.setOnClickListener((v) -> {
            System.out.println(this.getTag().toString());
        });
        if (is_holiday) this.setTileStyle(STYLE_HOLIDAY);
        else this.setTileStyle(STYLE_ACTIVE_MONTH);
    }

    /**
     * Sets tile style according to given STYLE constant
     * @param tile_style STYLE constant
     */
    public void setTileStyle(int tile_style){
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
            case STYLE_HOLIDAY:
                this.setTextColor(context.getColor(R.color.red));
                this.setBackground(AppCompatResources.getDrawable(context, R.drawable.red_border));
                break;
            case STYLE_ACTIVE_HOLIDAY:
                this.setTextColor(context.getColor(R.color.light_red));
                this.setBackground(AppCompatResources.getDrawable(context, R.drawable.light_red_border));
                break;
            case STYLE_NONACTIVE_HOLIDAY:
                this.setTextColor(context.getColor(R.color.dark_red));
                this.setBackground(AppCompatResources.getDrawable(context, R.drawable.dark_red_border));
                break;
        }
    }

    /**
     * Sets whether this tile should be seen as 'active'
     * @param is_active bool value
     */
    public void setActive(boolean is_active){
        this.is_active = is_active;
        if (this.is_holiday && !is_active) this.setTileStyle(CalendarTile.STYLE_NONACTIVE_HOLIDAY);
        else if (this.is_holiday) this.setTileStyle(STYLE_HOLIDAY);
        else this.setTileStyle(STYLE_NONACTIVE_MONTH);
    }

    /**
     * Sets whether this tile should be seen as a holiday
     * @param is_holiday bool value
     */
    public void setHoliday(boolean is_holiday){
        this.is_holiday = is_holiday;
        if (this.is_active) this.setTileStyle(STYLE_HOLIDAY);
        else this.setTileStyle(STYLE_NONACTIVE_HOLIDAY);
        if (this.is_today) this.setTileStyle(STYLE_ACTIVE_HOLIDAY);
    }

    /**
     * Marks this tile as current day
     */
    public void setToday(){
        this.is_today = true;
        if (this.is_holiday) this.setTileStyle(CalendarTile.STYLE_ACTIVE_HOLIDAY);
        else this.setTileStyle(STYLE_ACTIVE_DAY);
    }
}
