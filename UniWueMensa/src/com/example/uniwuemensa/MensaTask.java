package com.example.uniwuemensa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Pair;

public class MensaTask extends AsyncTask<Void, Void, List<Pair<String, List<List<MensaMeal>>>>> {
    // dont mess with the context, instead pass all relevant data to the Task
    private final boolean isOnline;
    private final MainActivity.SectionsPagerAdapter adapter;
    private SQLiteOpenHelper helper;

    private final static String[] urls = {"http://www.studentenwerk-wuerzburg.de/essen-trinken/speiseplaene/plan/show/mensa-am-hubland-wuerzburg.html",
            "http://www.studentenwerk-wuerzburg.de/essen-trinken/speiseplaene/plan/show/frankenstube-wuerzburg.html"};
    private final static String[] names = {"Hubland Mensa", "Frankenstube"};

    public MensaTask(boolean isOnline,
                     MainActivity.SectionsPagerAdapter adapter, SQLiteOpenHelper helper) {
        this.isOnline = isOnline;
        this.adapter = adapter;
        this.helper = helper;
    }

    @Override
    protected List<Pair<String, List<List<MensaMeal>>>> doInBackground(Void... params) {
        if (isOnline) {
            return fetchDataOnline();
        } else {
            return fetchDataLocally();
        }
    }

    private List<Pair<String, List<List<MensaMeal>>>> fetchDataLocally() {
        List<Pair<String, List<List<MensaMeal>>>> entries = new ArrayList<Pair<String, List<List<MensaMeal>>>>();

        final String[] names = {"Hubland Mensa", "Frankenstube"};



        for (String name : names) {
            Date d = new Date();
            Calendar cal = Calendar.getInstance();

            cal.setTime(d);

            // set the rest to 0 -> important for reliable database access
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            // get last monday
            cal.add(Calendar.DAY_OF_MONTH, -cal.get(Calendar.DAY_OF_WEEK) + 2);

            List<List<MensaMeal>> mealsPerDay = new ArrayList<List<MensaMeal>>();

            for (int i = 0; i < 10; i++) {
                mealsPerDay.add(MensaMeal.readFromDataBase(
                        helper.getReadableDatabase(), cal.getTime(), name));


                if (i == 4) {
                    cal.add(Calendar.DAY_OF_MONTH, 3);
                } else {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            entries.add(new Pair<String, List<List<MensaMeal>>>(name, mealsPerDay));
        }


        return entries;
    }

    private List<Pair<String, List<List<MensaMeal>>>> fetchDataOnline() {
        List<Pair<String, List<List<MensaMeal>>>> result = new ArrayList<Pair<String, List<List<MensaMeal>>>>();

        for (int i = 0; i < urls.length; i++) {
            Pair<String, List<List<MensaMeal>>> pair = new Pair<String, List<List<MensaMeal>>>(names[i], getListsForUrl(urls[i], names[i]));
            result.add(pair);
        }

        return result;
    }

    private List<List<MensaMeal>> getListsForUrl(String url, String name) {
        List<List<MensaMeal>> entries = new ArrayList<List<MensaMeal>>();

        try {


            Document doc = Jsoup.connect(url).get();
            Elements days = doc.select("div[data-day]");

            Date d = new Date();
            Calendar cal = Calendar.getInstance();

            cal.setTime(d);

            // set the rest to 0 -> important for reliable database access
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            // get last monday
            cal.add(Calendar.DAY_OF_MONTH, -cal.get(Calendar.DAY_OF_WEEK) + 2);

            for (int i = 0; i < 10; i++) {
                entries.add(getMealsOnDay(cal, days, name));

                if (i == 4) {
                    cal.add(Calendar.DAY_OF_MONTH, 3);
                } else {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            return entries;
        } catch (IOException e) {
        }

        return entries;
    }

    @Override
    protected void onPostExecute(List<Pair<String, List<List<MensaMeal>>>> result) {
        adapter.updateLists(result);
    }

    private List<MensaMeal> getMealsOnDay(Calendar cal, Elements days, String name) {
        List<MensaMeal> result = new ArrayList<MensaMeal>();

        for (Element e : days) {
            String date = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
                    + "." + String.format("%02d", cal.get(Calendar.MONTH) + 1)
                    + ".";

            if (e.attr("data-day").contains(date)
                    && !e.id().equals("thecurrentday")) {
                for (Element elem : e.select("article.menu")) {
                    int studentPrice = 0, staffPrice = 0, guestPrice = 0;
                    String title;

                    title = elem.select("div.left div.title").text();
                    String studentPriceString = elem.select("div.price").attr(
                            "data-default");
                    String staffPriceString = elem.select("div.price").attr(
                            "data-bed");
                    String guestPriceString = elem.select("div.price").attr(
                            "data-guest");

                    try {
                        studentPrice = Integer.parseInt(studentPriceString
                                .split(" ")[0].replace(",", ""));
                        staffPrice = Integer.parseInt(staffPriceString
                                .split(" ")[0].replace(",", ""));
                        guestPrice = Integer.parseInt(guestPriceString
                                .split(" ")[0].replace(",", ""));
                    } catch (NumberFormatException ex) {
                    }

                    MensaMeal meal = new MensaMeal(studentPrice, staffPrice,
                            guestPrice, title, cal.getTime());

                    meal.writeToDatabase(helper.getWritableDatabase(), name);
                    result.add(meal);
                }

                return result;
            }
        }

        return result;
    }
}
