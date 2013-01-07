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

public class MensaTask extends AsyncTask<Void, Void, List<List<MensaMeal>>> {
	// dont mess with the context, instead pass all relevant data to the Task
	private final boolean isOnline;
	private final MainActivity.SectionsPagerAdapter adapter;
	private SQLiteOpenHelper helper;

	public MensaTask(boolean isOnline,
			MainActivity.SectionsPagerAdapter adapter, SQLiteOpenHelper helper) {
		this.isOnline = isOnline;
		this.adapter = adapter;
		this.helper = helper;
	}

	@Override
	protected List<List<MensaMeal>> doInBackground(Void... params) {
		if (isOnline) {
			return fetchDataOnline();
		} else {
			return fetchDataLocally();
		}
	}

	private List<List<MensaMeal>> fetchDataLocally() {
		List<List<MensaMeal>> entries = new ArrayList<List<MensaMeal>>();

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
			entries.add(MensaMeal.readFromDataBase(
					helper.getReadableDatabase(), cal.getTime()));

			if (i == 4) {
				cal.add(Calendar.DAY_OF_MONTH, 3);
			} else {
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		return entries;
	}

	private List<List<MensaMeal>> fetchDataOnline() {
		List<List<MensaMeal>> entries = new ArrayList<List<MensaMeal>>();

		try {
			final String url = "http://www.studentenwerk-wuerzburg.de/essen-trinken/speiseplaene/plan/show/mensa-am-hubland-wuerzburg.html";
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
				entries.add(getMealsOnDay(cal, days));

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
	protected void onPostExecute(List<List<MensaMeal>> result) {
		adapter.updateLists(result);
	}

	private List<MensaMeal> getMealsOnDay(Calendar cal, Elements days) {
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

					result.add(meal);
				}

				return result;
			}
		}

		return result;
	}
}
