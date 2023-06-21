package com.wojtech.calendar;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private LinearLayout calendar_body;
    private float x1, x2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Date current_date = new Date();
        CalendarUtils.initiateCalendar(current_date);

        ActionBar bar = this.getSupportActionBar();
        calendar_body = findViewById(R.id.calendar_body);
        View calendar_control = findViewById(R.id.calendar_control);
        ImageButton settings_button = findViewById(R.id.settings_button);
        TextView month_label = findViewById(R.id.month_label);
        try {
            assert bar != null;
            bar.hide();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            month_label.setText(CalendarUtils.getMonthName()+" "+CalendarUtils.getYear());
            CalendarUtils.renderMonth(getApplicationContext(), calendar_body);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.render_failed), Toast.LENGTH_SHORT).show();
            System.out.println(e.getMessage());
        }

        // calendar month control via swipe gesture
        calendar_control.setOnTouchListener((view, event) -> {
            int min_distance = 100;
            float delta;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    return true;
                case MotionEvent.ACTION_UP:
                    x2 = event.getX();
                    delta = x2-x1;
                    if (Math.abs(delta) > min_distance) {
                        // left or right
                        if (delta < 0) {
                            try {
                                CalendarUtils.addMonthsToCalendar(1);
                                CalendarUtils.renderMonth(getApplicationContext(), calendar_body);
                                month_label.setText(CalendarUtils.getMonthName()+" "+CalendarUtils.getYear());
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), getResources().getText(R.string.render_failed), Toast.LENGTH_SHORT).show();
                                System.out.println(e.getMessage());
                            }
                            return true;
                        }
                        if (delta > 0) {
                            try {
                                CalendarUtils.addMonthsToCalendar(-1);
                                CalendarUtils.renderMonth(getApplicationContext(), calendar_body);
                                month_label.setText(CalendarUtils.getMonthName()+" "+CalendarUtils.getYear());
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), getResources().getText(R.string.render_failed), Toast.LENGTH_SHORT).show();
                                System.out.println(e.getMessage());
                            }
                            return true;
                        }
                    } else {
                        return false;
                    }
            }
            return calendar_control.performClick();
        });
    }
}