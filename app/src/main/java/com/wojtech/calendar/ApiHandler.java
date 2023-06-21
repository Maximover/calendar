package com.wojtech.calendar;

import android.content.Context;
import android.net.ConnectivityManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class ApiHandler implements Runnable{
    private URL url;
    private final CountDownLatch latch;
    private JSONArray data;

    /**
     * Handles api call and notifies given latch when request is fulfilled
     * @param date date for which api call will be centered around
     * @param latch latch which will be notified once data is fetched
     */
    public ApiHandler(Date date, CountDownLatch latch) {
        DateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        try {
            c.add(Calendar.MONTH, -1);
            String date_from = YEAR_MONTH_FORMAT.format(c.getTime());
            c.add(Calendar.MONTH, 2);
            String date_to = YEAR_MONTH_FORMAT.format(c.getTime());
            this.url = new URL("https://openholidaysapi.org/PublicHolidays?countryIsoCode=PL&languageIsoCode=PL&validFrom="+date_from+"-01&validTo="+date_to+"-30");
        } catch (Exception e) {
            this.url = null;
        }
        this.latch = latch;
    }

    /**
     * Is responsible for fetching data from the url
     * @return json data array
     */
    private JSONArray getData() {
        try {
            Scanner scanner = new Scanner(this.url.openStream());
            JSONTokener json_encoder = new JSONTokener(scanner.useDelimiter("\\Z").next());
            JSONArray json = new JSONArray(json_encoder);
            scanner.close();
            return json;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets fetched data in json format
     * @return fetched data
     */
    public JSONArray getHolidayData(){
        return this.data;
    }

    /**
     * Parses fetched data into an ArrayList
     * @return parsed data
     */
    public ArrayList<HashMap<String,String>> getHolidayDataArray(){
        ArrayList<HashMap<String, String>> holidays = new ArrayList<>();
        try {
            for (int i=0; i<this.data.length(); i++) {
                HashMap<String, String> holiday = new HashMap<>();
                JSONObject current_fetched_holiday = this.data.getJSONObject(i);
                Date formatted_date = CalendarUtils.US_DATE_FORMAT.parse(current_fetched_holiday.getString("startDate"));
                assert formatted_date != null;
                String parsed_date = CalendarUtils.PL_DATE_FORMAT.format(formatted_date);
                holiday.put(Database.HOLIDAYS_NAME, current_fetched_holiday.getJSONArray("name").getJSONObject(0).getString("text"));
                holiday.put(Database.HOLIDAYS_DATE, parsed_date);
                holiday.put(Database.HOLIDAYS_TAG, Database.TAG_API);
                holidays.add(holiday);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return holidays;
    }

    @Override
    public void run() {
        try {
            this.data = getData();
            this.latch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether internet connection is available
     * @param context application context
     * @return True if internet connection is available
     */
    public static boolean checkForInternet(Context context){
        ConnectivityManager connectivity_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivity_manager.getActiveNetworkInfo() != null
                && connectivity_manager.getActiveNetworkInfo().isAvailable()
                && connectivity_manager.getActiveNetworkInfo().isConnected());
    }
}
