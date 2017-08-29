package aparna.appy.android.example.com.prsence;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry;
import fr.ganfra.materialspinner.MaterialSpinner;

import static aparna.appy.android.example.com.prsence.R.string.p;

/**
 * Predictor function implementation
 */

public class PredictorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the semester data loader
     */
    private static final int SEM_LOADER = 0;

    private MaterialSpinner spinner;
    private ArrayList<String> items = new ArrayList<>();
    private String name[] = new String[50];
    private int total[] = new int[50];
    private int attended[] = new int[50];
    private float percent[] = new float[50];
    private ArrayAdapter<String> adapter;
    private int n;
    private int selected = 0;
    private Button predict;
    private TextView p_total;
    private TextView p_attended;
    private TextView p_percent;
    private EditText f_total;
    private EditText f_attended;
    private int future_total;
    private int future_attended;
    private float future_percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predictor);

        spinner = (MaterialSpinner) findViewById(R.id.spinner);
        predict = (Button) findViewById(R.id.predictor);
        p_total = (TextView) findViewById(R.id.p_total);
        p_attended = (TextView) findViewById(R.id.p_attended);
        p_percent = (TextView) findViewById(R.id.p_percent);
        f_total = (EditText) findViewById(R.id.future_total);
        f_attended = (EditText) findViewById(R.id.future_attended);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != -1) {

                    selected = position;
                    p_total.setText("" + total[position]);
                    p_attended.setText("" + attended[position]);
                    p_percent.setText("" + percent[position] + getString(R.string.p));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tot = f_total.getText().toString();
                String att = f_attended.getText().toString();

                if (tot.isEmpty() || att.isEmpty()) {
                    Snackbar.make(view, R.string.predictor_no_input, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action, null).show();

                } else {
                    future_total = Integer.parseInt(tot);
                    future_attended = Integer.parseInt(att);
                    if (future_total < future_attended) {
                        Snackbar.make(view, R.string.predictor_wrong_input, Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.action), null).show();
                    } else {
                        future_total += total[selected];
                        future_attended += attended[selected];
                        future_percentage = percentage(future_total, future_attended);
                        Snackbar.make(view, getString(R.string.future_att) + " " + future_percentage + " " + getString(p), Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.action), null).show();
                    }
                }
            }
        });
        // Kick off the loader
        getLoaderManager().restartLoader(SEM_LOADER, null, this);
    }

    /**
     * Function to calculate percentage
     *
     * @param t total
     * @param a attended
     * @return
     */
    private float percentage(int t, int a) {
        float p = 0.0f;
        p = (float) a / (float) t * 100;
        int scale = (int) Math.pow(10, 1);
        p = (float) Math.round(p * scale) / scale;
        return p;
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        try {

            int overall_total = 0;
            int overall_attended = 0;

            //To check if data available
            n = cursor.getCount();
            if (n == 0) {
                setContentView(R.layout.no_data);
            }

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
            float overall_percent = 0.0f;

            if (overall_total > 0) {
                overall_percent = percentage(overall_total, overall_attended);
            }
            name[i] = getString(R.string.predictor_sem);
            total[i] = overall_total;
            attended[i] = overall_attended;
            percent[i] = overall_percent;

        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i <= n; i++)
            items.add(name[i]);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        p_total.setText("" + total[0]);
        p_attended.setText("" + attended[0]);
        p_percent.setText("" + percent[0] + getString(R.string.p));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
