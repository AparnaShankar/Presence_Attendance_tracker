package aparna.appy.android.example.com.prsence;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry;

/**
 * Displaying statistics in form of bar chart and pie chart using view pager and fragments and MPChart library
 */
public class StatisticsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the semester data loader
     */
    private static final int SEM_LOADER = 0;

    //Semester data
    private String name[] = new String[50];
    private int attended[] = new int[50];
    private int total[] = new int[50];
    private float percent[] = new float[50];
    private float overall_percent = 0.0f;
    private int n = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Kick off the loader
        getLoaderManager().initLoader(SEM_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                SemesterEntry._ID,
                SemesterEntry.COLUMN_SUBJECT_NAME,
                SemesterEntry.COLUMN_TOTAL,
                SemesterEntry.COLUMN_ATTENDED,
                SemesterEntry.COLUMN_CRITERIA};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                SemesterEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    private float percentage(int t, int a) {
        float p = 0.0f;
        p = (float) a / (float) t * 100;
        int scale = (int) Math.pow(10, 1);
        p = (float) Math.round(p * scale) / scale;
        return p;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        try {
            int overall_total = 0;
            int overall_attended = 0;

            n = cursor.getCount();

            int nameColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_SUBJECT_NAME);
            int totalColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_TOTAL);
            int attendedColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_ATTENDED);

            int i = 0;

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the Int value of the word
                // at the current row the cursor is on.
                name[i] = cursor.getString(nameColumnIndex);
                total[i] = cursor.getInt(totalColumnIndex);
                attended[i] = cursor.getInt(attendedColumnIndex);
                percent[i] = 0.0f;
                overall_total += total[i];
                overall_attended += attended[i];

                //Check if denominator is not zero
                if (total[i] > 0) {
                    percent[i] = percentage(total[i], attended[i]);
                }

                i++;
            }

            if (overall_total > 0) {
                overall_percent = percentage(overall_total, overall_attended);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setOffscreenPageLimit(0);

        PageAdapter pg = new PageAdapter(getSupportFragmentManager());
        pager.setAdapter(pg);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private class PageAdapter extends FragmentPagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment f = null;

            switch (pos) {
                case 0:
                    f = barFragment.newInstance(name, percent, n, overall_percent);
                    break;
                case 1:
                    f = pieFragment.newInstance(name, percent, n);
                    break;
            }

            return f;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
