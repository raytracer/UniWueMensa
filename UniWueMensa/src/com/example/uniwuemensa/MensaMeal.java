package com.example.uniwuemensa;

import java.util.Date;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

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
	
	public boolean writeToDataBase(SQLiteDatabase db) {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(MensaContract.MensaEntry.COLUMN_NAME_STUDENT_PRICE, studentPrice);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_STAFF_PRICE, staffPrice);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_GUEST_PRICE, guestPrice);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_TITLE, title);
		values.put(MensaContract.MensaEntry.COLUMN_NAME_DATE, date.getTime());

		// Insert the new row, returning the primary key value of the new row
		long newRowId;
		newRowId = db.insert(
		         MensaContract.MensaEntry.TABLE_NAME,
		         null,
		         values);
		
		return newRowId != -1;
	}
	
	public static MensaMeal readFromDataBase(SQLiteDatabase db, Date day) {
		//db.query
		return null;
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
