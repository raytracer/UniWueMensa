package com.raytracer.uniwuemensa;

import android.provider.BaseColumns;

public class MensaContract {

    public class MensaEntry implements BaseColumns {
        public static final String TABLE_NAME = "mensa";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_STUDENT_PRICE = "student_price";
        public static final String COLUMN_NAME_STAFF_PRICE = "staff_price";
        public static final String COLUMN_NAME_GUEST_PRICE = "guest_price";
        public static final String COLUMN_NAME_LOCATION = "location";

    }

    private MensaContract() {
    }
}
