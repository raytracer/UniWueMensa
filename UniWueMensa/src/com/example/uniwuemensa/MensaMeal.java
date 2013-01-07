package com.example.uniwuemensa;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * MensaMeal represents a single meal on a specific day.
 */
public class MensaMeal {
	private final int studentPrice, staffPrice, guestPrice;
	private final String title;
	private final Date date;

	public MensaMeal(int studentPrice, int staffPrice, int guestPrice,
			String title, Date date) {
		super();
		this.studentPrice = studentPrice;
		this.staffPrice = staffPrice;
		this.guestPrice = guestPrice;
		this.title = title;
		this.date = date;
	}

	public boolean writeToDatabase(SQLiteDatabase db) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(MensaContract.MensaEntry.COLUMN_NAME_STUDENT_PRICE,
				studentPrice);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_STAFF_PRICE, staffPrice);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_GUEST_PRICE, guestPrice);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_TITLE, title);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_DATE, date.getTime());

		// Insert the new row, returning the primary key value of the new row
		long newRowId;
		newRowId = db.insert(MensaContract.MensaEntry.TABLE_NAME, null, values);
		
		return newRowId != -1;
	}

	public static ArrayList<MensaMeal> readFromDataBase(SQLiteDatabase db, Date day) {
		ArrayList<MensaMeal> result = new ArrayList<MensaMeal>();
		
		Cursor cur = db.query(true, MensaContract.MensaEntry.TABLE_NAME,
				new String[] { MensaContract.MensaEntry.COLUMN_NAME_TITLE,
						MensaContract.MensaEntry.COLUMN_NAME_STUDENT_PRICE,
						MensaContract.MensaEntry.COLUMN_NAME_STAFF_PRICE,
						MensaContract.MensaEntry.COLUMN_NAME_GUEST_PRICE },
				"date = " + day.getTime(), null, null, null, null, null);

		if (cur.getCount() == 0) {
			return null;
		}

		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			String title = cur.getString(cur
					.getColumnIndex(MensaContract.MensaEntry.COLUMN_NAME_TITLE));
			int studentPrice = cur
					.getInt(cur
							.getColumnIndex(MensaContract.MensaEntry.COLUMN_NAME_STUDENT_PRICE));
			int staffPrice = cur
					.getInt(cur
							.getColumnIndex(MensaContract.MensaEntry.COLUMN_NAME_STAFF_PRICE));
			int guestPrice = cur
					.getInt(cur
							.getColumnIndex(MensaContract.MensaEntry.COLUMN_NAME_GUEST_PRICE));
			
			result.add(new MensaMeal(studentPrice, staffPrice, guestPrice, title, day));
		}
		
		return result;
	}

	public int getStudentPrice() {
		return studentPrice;
	}

	public int getStaffPrice() {
		return staffPrice;
	}

	public int getGuestPrice() {
		return guestPrice;
	}

	public String getTitle() {
		return title;
	}

	public Date getDate() {
		return date;
	}

}
