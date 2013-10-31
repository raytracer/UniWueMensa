package com.example.uniwuemensa;

import java.util.Calendar;
import java.util.Date;

public class HelperUtilities {
    public static int getCurrentIndex() {
        Date d = new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(d);

        //get last monday
        cal.add(Calendar.DAY_OF_MONTH, -cal.get(Calendar.DAY_OF_WEEK) + 2);

        for (int i = 0; i < 10; i++) {
            if (cal.getTime().equals(d)) {
                return i;
            } else if (cal.getTime().compareTo(d) > 0) {
                return i;
            }

            if (i == 4) {
                cal.add(Calendar.DAY_OF_MONTH, 3);
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        return 0;
    }

    public static Date getDateForIndex(int position) {
        Date d = new Date();
        Calendar cal = Calendar.getInstance();

        cal.setTime(d);

        //get last monday
        cal.add(Calendar.DAY_OF_MONTH, -cal.get(Calendar.DAY_OF_WEEK) + 2);

        for (int i = 0; i < 10; i++) {
            if (i == position) {
                return cal.getTime();
            }
            if (i == 4) {
                cal.add(Calendar.DAY_OF_MONTH, 3);
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        return null;
    }

    public static String centsToEuroString(int cents) {
        int euro = cents / 100;
        int remainder = cents % 100;

        return euro + "," + String.format("%02d", remainder) + " €";
    }
}
