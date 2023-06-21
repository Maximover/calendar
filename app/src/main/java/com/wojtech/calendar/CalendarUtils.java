package com.wojtech.calendar;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class CalendarUtils {
    public static final DateFormat PL_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
    public static final DateFormat US_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    public static final DateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MM.yyyy", Locale.ENGLISH);
    public static final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy", Locale.ENGLISH);
    public static CalendarTile[][] calendar_tiles = new CalendarTile[6][7];
    protected static Date base_date;
    protected static String base_date_str;
    protected static String current_date;
    protected static Calendar calendar;

    /**
     * Initiates necessary objects for CalendarUtils' utilities to work
     * @param date date to initiate the calendar with, can be changed with addMonthsToCalendar()
     */
    public static void initiateCalendar(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        calendar = c;
        base_date = date;
        base_date_str = PL_DATE_FORMAT.format(date);
        current_date = PL_DATE_FORMAT.format(date);
    }

    /**
     * Adds given number of month to class' Calendar object
     * @param months_to_add number of months to add or subtract when given a negative number
     */
    public static void addMonthsToCalendar(int months_to_add){
        calendar.add(Calendar.MONTH, months_to_add);
        base_date = calendar.getTime();
        base_date_str = PL_DATE_FORMAT.format(base_date);
    }

    /**
     * Renders a month in the calendar body
     * @param application_context application context
     * @param calendar_body layout in which to render day tiles
     * @throws AssertionError failed while processing given date
     * @throws ParseException failed while processing given date
     */
    public static void renderMonth(Context application_context, LinearLayout calendar_body) throws AssertionError, ParseException{
        calendar_body.removeAllViews();
        Date date_obj = CalendarUtils.PL_DATE_FORMAT.parse(base_date_str);
        // get which day of the week is 1st on given month to, and how many days are in given month
        int first_day_of_current_month_nr, days_in_current_month, days_in_previous_month;
        LocalDate previous_month_date;
        try {
            assert date_obj != null;
            previous_month_date = LocalDate.parse(CalendarUtils.US_DATE_FORMAT.format(date_obj)).minusMonths(1);
            String previous_date = CalendarUtils.PL_DATE_FORMAT.format(Objects.requireNonNull(CalendarUtils.US_DATE_FORMAT.parse(previous_month_date.toString())));
            first_day_of_current_month_nr = getMonthsFirstDay(base_date_str);
            days_in_current_month = getMonthsDays(base_date_str);
            days_in_previous_month = getMonthsDays(previous_date);
        }catch (Exception e){
            first_day_of_current_month_nr = 0;
            days_in_current_month = 0;
            days_in_previous_month = 0;
            e.printStackTrace();
        }
        assert first_day_of_current_month_nr > 0 && days_in_current_month > 0;
        int day_render = days_in_current_month + first_day_of_current_month_nr - 1;
        for (int i=1; i<=6; i++) {
            LinearLayout week_layout = new LinearLayout(application_context);
            week_layout.setOrientation(LinearLayout.HORIZONTAL);
            week_layout.setHorizontalGravity(1);
            for(int j=1; j<=7; j++){
                CalendarTile tile = new CalendarTile(application_context, j==7);
                Calendar c = Calendar.getInstance();
                c.setTime(base_date);
                c.add(Calendar.MONTH, -1);
                String previous_month_year = MONTH_YEAR_FORMAT.format(c.getTime());
                c.add(Calendar.MONTH, 2);
                String next_month_year = MONTH_YEAR_FORMAT.format(c.getTime());
                c.add(Calendar.MONTH, 1);
                if (day_render > days_in_current_month) {
                    // previous month' overlapping days
                    tile.setText(String.valueOf(days_in_previous_month - (day_render - days_in_current_month) + 1));
                    tile.setTag(tile.getText().toString() + "." + previous_month_year);
                    tile.setActive(false);

                } else if (day_render > 0) {
                    // this month' days
                    tile.setText(String.valueOf(days_in_current_month - day_render + 1));
                    tile.setTag(tile.getText().toString() + base_date_str.substring(2));
                    if(days_in_current_month - day_render + 1 == Integer.parseInt(current_date.substring(0,2)) && isCurrentMonth()) {
                        tile.setToday();
                    }
                } else {
                    // next month' overlapping days
                    tile.setText(String.valueOf(Math.abs(day_render) + 1));
                    tile.setTag(tile.getText().toString() + "." + next_month_year);
                    tile.setActive(false);

                }
                calendar_tiles[i-1][j-1] = tile;
                day_render -= 1 ;
                week_layout.addView(tile);
            }
            calendar_body.addView(week_layout);
        }
        fillHolidays(application_context);
    }

    public static void randomizeHolidays(int randomized_days){
        for(int i=0; i<randomized_days; i++)
            calendar_tiles[ThreadLocalRandom.current().nextInt(0, 6)][ThreadLocalRandom.current().nextInt(0, 7)].setHoliday(true);
    }

    /**
     * Connects to a holiday api and fills in days that appear to be holidays
     */
    private static void fillHolidays(Context context){
        ArrayList<HashMap<String, String>> holidays;
        Database db = new Database(context);
        holidays = db.getMonthsHolidays(base_date_str);
        // if there aren't holidays saved for this month fetch, then some from api and save them if there is internet connection
        if (holidays.size() == 0 && ApiHandler.checkForInternet(context)){
            CountDownLatch latch = new CountDownLatch(1);
            try {
                ExecutorService thread_service = Executors.newSingleThreadExecutor();
                ApiHandler api = new ApiHandler(base_date, latch);
                thread_service.execute(api);
                latch.await();
                holidays = api.getHolidayDataArray();
                for (HashMap<String, String> holiday : holidays) {
                    if (!db.isSavedHoliday(holiday.get(Database.HOLIDAYS_DATE)))
                        db.addHoliday(holiday.get(Database.HOLIDAYS_NAME), holiday.get(Database.HOLIDAYS_DATE), holiday.get(Database.HOLIDAYS_TAG));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.close();
        // no holidays saved and none were fetched via api
        if (holidays.size() == 0) {
            Toast.makeText(context, context.getResources().getText(R.string.holiday_fetch_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i=0;i<6;i++) {
            for (int j=0;j<7;j++){
                CalendarTile tile = calendar_tiles[i][j];
                String tile_date;
                try {
                    Date tile_date_obj = PL_DATE_FORMAT.parse(tile.getTag().toString());
                    assert tile_date_obj != null;
                    tile_date = PL_DATE_FORMAT.format(tile_date_obj);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                for (HashMap<String, String> holiday : holidays) {
                    if (tile_date.equals(holiday.get(Database.HOLIDAYS_DATE))) {
                        tile.setHoliday(true);
                        break;
                    }
                }
            }
        }

    }

    /**
     * Returns number for first day of month in given date.
     * @param date date
     * @return number for first day of given month - 1 for monday, 7 for Sunday
     * @throws ParseException thrown if given date is invalid
     */
    public static int getMonthsFirstDay(String date) throws ParseException {
        String formatted_date = "01" + date.substring(2);
        Date date_obj = CalendarUtils.PL_DATE_FORMAT.parse(formatted_date);
        assert date_obj != null;
        calendar.setTime(date_obj);
        int first_day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        // if first day is a Sunday, first_day will be equal to 0
        if (first_day == 0) first_day = 7;
        calendar.setTime(base_date);
        return first_day;
    }

    /**
     * Return number of days in a month of given date
     * @param date date
     * @return number of days in month
     * @throws ParseException thrown if given date is invalid
     */
    public static int getMonthsDays(String date) throws ParseException{
        Date date_obj = CalendarUtils.PL_DATE_FORMAT.parse(date);
        assert date_obj != null;
        calendar.setTime(date_obj);
        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.setTime(base_date);
        return days;
    }

    /**
     * Checks if class' Calendar object month matches current month
     * @return True if Calendar date's month and date is current
     */
    public static boolean isCurrentMonth(){
        return base_date_str.indexOf(current_date.substring(2)) > 0;
    }

    /**
     * Returns english month name
     * @return english name of current month
     */
    public static String getMonthName() {
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
    }

    /**
     * Returns current year
     * @return current year
     */
    public static String getYear() {
        return YEAR_FORMAT.format(calendar.getTime());
    }
}
