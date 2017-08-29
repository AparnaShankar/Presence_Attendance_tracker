package aparna.appy.android.example.com.prsence;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry;

/**
 * To display list of subjects with details
 */
public class SemesterActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the semester data loader
     */
    private static final int SEM_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    SemCursorAdapter mCursorAdapter;

    private float percent = 0.0f;
    private int overall_total = 0;
    private int overall_attended = 0;
    private ToolbarAdapter pg;
    private ViewPager viewPager;
    private ListView listView;
    private Toolbar toolbar;
    private String backup;
    private TextView empty;
    private boolean backup_enable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester);

        // Setup FAB to open SemEditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SemesterActivity.this, SemEditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the subject data
        listView = (ListView) findViewById(R.id.list);
        listView.setNestedScrollingEnabled(true);
        empty = (TextView) findViewById(R.id.empty_text);

        // Setup an Adapter to create a list item for each row of subject data in the Cursor.
        // There is no subject data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new SemCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link SemEditorActivity}
                Intent intent = new Intent(SemesterActivity.this, SemEditorActivity.class);

                // Form the content URI that represents the specific subject that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link SemesterEntry#CONTENT_URI}.
                Uri currentPetUri = ContentUris.withAppendedId(SemesterEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                // Launch the {@link EditorActivity} to display the data for the current subject.
                startActivity(intent);
            }
        });

        toolbar = (Toolbar) findViewById(R.id.MyToolbar);
        setSupportActionBar(toolbar);
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Kick off the loader
        getLoaderManager().restartLoader(SEM_LOADER, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (id == R.id.delete_all) {
            // Pop up confirmation dialog for deletion
            showDeleteConfirmationDialog();
            return true;
        } else if (id == R.id.backup) {
            if (backup.isEmpty())
                backup_enable = false;
            //Check if data available for backup
            if (backup_enable) {
                //Mail the available data
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.att_backup));
                intent.putExtra(Intent.EXTRA_TEXT, backup);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);

                }
            } else {
                //No data
                Toast.makeText(this, getString(R.string.no_backup),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Prompt the user to confirm that they want to delete all the subjects.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all the subjects.
                deleteAll();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper method to delete all subjects in the database.
     */
    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(SemesterEntry.CONTENT_URI, null, null);

        backup_enable = false;
        empty.setVisibility(View.VISIBLE);

        //Show toast message to tell delete was successful or unsuccessful.
        if (rowsDeleted == 0) {
            //unsuccessful
            Toast.makeText(this, getString(R.string.delete_unsuccessful),
                    Toast.LENGTH_SHORT).show();
        } else {
            //success
            Toast.makeText(this, getString(R.string.delete_success),
                    Toast.LENGTH_SHORT).show();
        }
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //set empty view
        empty.setVisibility(View.VISIBLE);

        //Calculate nd update overall attendance
        overall_total = 0;
        overall_attended = 0;
        try {
            int nameColumnIndex = data.getColumnIndex(SemesterEntry.COLUMN_SUBJECT_NAME);
            int totalColumnIndex = data.getColumnIndex(SemesterEntry.COLUMN_TOTAL);
            int attendedColumnIndex = data.getColumnIndex(SemesterEntry.COLUMN_ATTENDED);
            backup = "";

            // Iterate through all the returned rows in the cursor
            while (data.moveToNext()) {

                //remove empty view text
                empty.setVisibility(View.GONE);

                //enable backup
                backup_enable = true;

                // Use that index to extract the Int value of the word
                // at the current row the cursor is on.

                String name = data.getString(nameColumnIndex);
                int total = data.getInt(totalColumnIndex);
                int attended = data.getInt(attendedColumnIndex);
                //Creating string for backup
                backup += getString(R.string.subject) + " " + getString(R.string.colon) + " " + name + "\n";
                backup += getString(R.string.total) + " " + getString(R.string.colon) + " " + total + "\n";
                backup += getString(R.string.attended) + " " + getString(R.string.colon) + " " + attended + "\n\n";
                overall_total += total;
                overall_attended += attended;
            }

            percent = 0.0f;

            //Check if denominator is not zero
            if (overall_total > 0) {
                percent = (float) overall_attended / (float) overall_total * 100;
                int scale = (int) Math.pow(10, 1);
                percent = (float) Math.round(percent * scale) / scale;
            }

            viewPager = (ViewPager) findViewById(R.id.viewpager);

            mainToolbarFragment.newInstance(this, overall_total, overall_attended, percent);

            pg = new ToolbarAdapter(getSupportFragmentManager(), this, overall_total, overall_attended, percent);
            viewPager.setAdapter(pg);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.setupWithViewPager(viewPager, true);

            CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
            if (!collapsingToolbarLayout.isTitleEnabled()) {
                collapsingToolbarLayout.setTitleEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update {@link SemCursorAdapter} with this new cursor containing updated attendance data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
