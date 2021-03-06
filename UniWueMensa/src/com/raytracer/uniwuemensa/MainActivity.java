package com.raytracer.uniwuemensa;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.uniwuemensa.R;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private String[][] techLists;
    private IntentFilter[] filters;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(mSectionsPagerAdapter);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(HelperUtilities.getCurrentIndex());
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeLayout.setEnabled(false);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        swipeLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });



        initCard();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            createTag(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createTag(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag == null) {
                return;
            }

            IsoDep desfire = IsoDep.get(tag);

            if (desfire == null) {
                return;
            }

            final byte[] READ_VALUE_COMMAND = new byte[]{(byte) 0x6C, (byte) 0x01};
            final byte[] NATIVE_SELECT_COMMAND = new byte[]{(byte) 0x5A, (byte) 0x5F, (byte) 0x84, (byte) 0x15};

            try {

                desfire.connect();

                desfire.transceive(NATIVE_SELECT_COMMAND);

                byte[] result = desfire.transceive(READ_VALUE_COMMAND);

                long value = 0;
                for (int i = 1; i < result.length; i++) {
                    value += ((long) result[i] & 0xffL) << (8 * (i - 1));
                }


                desfire.close();

                showMessageBox(new DecimalFormat("#0.00 €").format(value / 1000.0d), "Guthaben");

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void showMessageBox(String message, String title) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        builder.setPositiveButton("Ok", listener);
        builder.show();
    }

    private void initCard() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mAdapter == null) return;

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        IntentFilter detectedTag = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters = new IntentFilter[]{detectedTag};
        techLists = new String[][]{new String[]{
                MifareClassic.class.getName()
        }, new String[]{
                IsoDep.class.getName()
        }};
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSectionsPagerAdapter.notifyDataSetChanged();
        if (mAdapter == null) return;
        mAdapter.enableForegroundDispatch(this,
                mPendingIntent, filters, techLists);

        createTag(getIntent());
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    private List<Pair<String, String>> getMensaSettings() {
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_HUBLAND, false)) {
            result.add(new Pair<String, String> (getString(R.string.hubland),
                    getString(R.string.hubland_url)));
        }
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_FRANKENSTUBE, false)) {
            result.add(new Pair<String, String> (getString(R.string.frankenstube),
                    getString(R.string.frankenstube_url)));
        }
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_MENSATERIA, false)) {
            result.add(new Pair<String, String> (getString(R.string.mensateria),
                    getString(R.string.mensateria_url)));
        }

        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter == null) return;
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements  SwipeRefreshLayout.OnRefreshListener {
        private HashMap<Integer, MealListFragment> fragments = new HashMap<Integer, MainActivity.MealListFragment>();

        public void updateLists(List<Pair<String,List<List<MensaMeal>>>> allMeals) {
            SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
            swipeLayout.setRefreshing(false);

            List<List<Pair<String, List<MensaMeal>>>> mealsPerDay = new ArrayList<List<Pair<String, List<MensaMeal>>>>();

            for (Pair<String, List<List<MensaMeal>>> allMeal : allMeals) {
                String name = allMeal.first;
                List<List<MensaMeal>> mealsPerMensaPerTwoWeek = allMeal.second;

                for (int j = 0; j < mealsPerMensaPerTwoWeek.size(); j++) {
                    if (j >= mealsPerDay.size()) {
                        mealsPerDay.add(new ArrayList<Pair<String, List<MensaMeal>>>());
                    }

                    mealsPerDay.get(j).add(new Pair<String, List<MensaMeal>>(name, mealsPerMensaPerTwoWeek.get(j)));
                }
            }

            for (int i = 0; i < mealsPerDay.size(); i++) {
                if (fragments.containsKey(i)) {
                    fragments.get(i).updateLists(mealsPerDay.get(i));
                }
            }

            this.notifyDataSetChanged();
        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            for (int i = 0; i < getCount(); i++) {
                MealListFragment fragment = new MealListFragment();
                Bundle args = new Bundle();
                args.putInt(MealListFragment.ARG_SECTION_NUMBER, i + 1);
                fragment.setArguments(args);

                fragments.put(i, fragment);
            }

            new MensaTask(isOnline(), this, new MensaDbHelper(getApplicationContext()), getMensaSettings()).execute();
        }

        @Override
        public void onRefresh() {
            new MensaTask(isOnline(), this, new MensaDbHelper(getApplicationContext()), getMensaSettings()).execute();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            DateFormat df = new SimpleDateFormat("EEEE, dd.MM", Locale.getDefault());
            return df.format(HelperUtilities.getDateForIndex(position));

        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    public static class MealListFragment extends Fragment {
        public static final String ARG_SECTION_NUMBER = "section_number";

        private LayoutInflater inflater = null;
        private ViewGroup container = null;
        private List<Pair<String, List<MensaMeal>>> meals = null;
        private ListView listView = null;


        public MealListFragment() {
        }

        public boolean isFirstElementVisible() {
            if (listView != null) {
                View c = listView.getChildAt(0);
                if (c != null) {
                    int scrollY = -c.getTop() + listView.getFirstVisiblePosition() * c.getHeight();
                    return scrollY == 0;
                }
            }

            return true;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            this.inflater = inflater;
            this.container = container;

            if (meals == null) {
                ListView listView = (ListView) inflater.inflate(R.layout.menulist,
                        container, false);

                this.listView = listView;
                return listView;
            } else {
                return updateLists(meals);
            }
        }

        public View updateLists(List<Pair<String, List<MensaMeal>>> meals) {
            this.meals = meals;

            if (inflater == null || container == null) {
                return null;
            }

            ListView listView = (ListView) inflater.inflate(R.layout.menulist,
                    container, false);

            if (listView == null) {
                return null;
            }

            ArrayList<HashMap<String, String>> mylistData = new ArrayList<HashMap<String, String>>();

            String[] columnTags = new String[]{"col1", "col2"};

            int[] columnIds = new int[]{R.id.titleColumn, R.id.priceColumn};

            for (Pair<String, List<MensaMeal>> mealsPerMensa : meals) {
                for (MensaMeal meal : mealsPerMensa.second) {
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put("col1", meal.getTitle());

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String priceType = sharedPref.getString(SettingsActivity.KEY_PREF_PRICE_TYPE, MensaContract.MensaEntry.COLUMN_NAME_STUDENT_PRICE);

                    if (priceType.equals(MensaContract.MensaEntry.COLUMN_NAME_STUDENT_PRICE)) {
                        map.put("col2", HelperUtilities.centsToEuroString(meal.getStudentPrice()));
                    } else if (priceType.equals(MensaContract.MensaEntry.COLUMN_NAME_STAFF_PRICE)) {
                        map.put("col2", HelperUtilities.centsToEuroString(meal.getStaffPrice()));
                    } else if (priceType.equals(MensaContract.MensaEntry.COLUMN_NAME_GUEST_PRICE)) {
                        map.put("col2", HelperUtilities.centsToEuroString(meal.getGuestPrice()));
                    }

                    map.put("location", mealsPerMensa.first);

                    mylistData.add(map);
                }
            }

            SimpleAdapter arrayAdapter = new SimpleAdapter(getActivity(),
                    mylistData, R.layout.multicolumn, columnTags, columnIds);

            Sectionizer<HashMap<String, String>> mealSectionizer = new Sectionizer<HashMap<String, String>>() {

                @Override
                public String getSectionTitleForItem(HashMap<String, String> data) {
                    return data.get("location");
                }
            };

            SimpleSectionAdapter<HashMap<String, String>> simple = new SimpleSectionAdapter<HashMap<String, String>>(getActivity(), arrayAdapter, R.layout.locationname, R.id.locationtext, mealSectionizer);
            listView.setAdapter(simple);

            this.listView = listView;
            return listView;
        }
    }

}
