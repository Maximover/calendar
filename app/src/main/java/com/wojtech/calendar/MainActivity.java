package com.wojtech.calendar;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private LinearLayout calendar_body;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Date current_date = new Date();
        CalendarUtils.initiateCalendar(current_date);

        ActionBar bar = this.getSupportActionBar();
        calendar_body = findViewById(R.id.calendar_body);
        Button forward_button = findViewById(R.id.forward_button);
        Button backward_button = findViewById(R.id.backward_button);
        ImageButton settings_button = findViewById(R.id.settings_button);
        TextView month_label = findViewById(R.id.month_label);
        try {
            assert bar != null;
            bar.hide();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        try{
            month_label.setText(CalendarUtils.getMonthName()+" "+CalendarUtils.getYear());
            CalendarUtils.renderMonth(getApplicationContext(), calendar_body);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.render_failed), Toast.LENGTH_SHORT).show();
            System.out.println(e.getMessage());
        }

        backward_button.setOnClickListener((v)->{
            try {
                CalendarUtils.addMonthsToCalendar(-1);
                CalendarUtils.renderMonth(getApplicationContext(), calendar_body);
                month_label.setText(CalendarUtils.getMonthName()+" "+CalendarUtils.getYear());
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.render_failed), Toast.LENGTH_SHORT).show();
                System.out.println(e.getMessage());
            }
        });
        forward_button.setOnClickListener((v)->{
            try {
                CalendarUtils.addMonthsToCalendar(1);
                CalendarUtils.renderMonth(getApplicationContext(), calendar_body);
                month_label.setText(CalendarUtils.getMonthName()+" "+CalendarUtils.getYear());
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.render_failed), Toast.LENGTH_SHORT).show();
                System.out.println(e.getMessage());
            }
        });
    }
}