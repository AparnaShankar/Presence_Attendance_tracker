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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.CourseEntry;

/**
 * To display list of courses with details
 */
public class CourseActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the course data loader
     */
    private static final int COURSE_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    CourseCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        // Setup FAB to open CourseEditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.course_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CourseActivity.this, CourseEditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the course data
        ListView listView = (ListView) findViewById(R.id.course_list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.course_empty_view);
        listView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of course data in the Cursor.
        // There is no course data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new CourseCursorAdapter(this, null);
        listView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create new intent to go to {@link CourseEditorActivity}
                Intent intent = new Intent(CourseActivity.this, CourseEditorActivity.class);

                // Form the content URI that represents the specific course that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link CourseEntry#CONTENT_URI}.
                Uri currentCourseUri = ContentUris.withAppendedId(CourseEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentCourseUri);

                // Launch the {@link EditorActivity} to display the data for the current course.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(COURSE_LOADER, null, this);
    }

    /**
     * Prompt the user to confirm that they want to delete all the courses.
     */
    private void showDeleteConfirmationDialog() {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_course);

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all the courses.
                deleteAllCourse();
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
     * Helper method to delete all courses in the database.
     */
    private void deleteAllCourse() {

        //Delete operation
        int rowsDeleted = getContentResolver().delete(CourseEntry.CONTENT_URI, null, null);

        //Show toast message to tell delete was successful or unsuccessful.
        if (rowsDeleted == 0) {
            //unsuccessful
            Toast.makeText(this, getString(R.string.delete_unsuccessful_course),
                    Toast.LENGTH_SHORT).show();
        } else {
            //success
            Toast.makeText(this, getString(R.string.delete_success_course),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu options
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.course_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        //id of the menu item clicked on
        int id = item.getItemId();

        if (id == R.id.delete_all_course) {

            // Pop up confirmation dialog for deletion
            showDeleteConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                CourseEntry._ID,
                CourseEntry.COLUMN_COURSE_NAME,
                CourseEntry.COLUMN_TOTAL,
                CourseEntry.COLUMN_HOURS};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                CourseEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Update {@link CourseCursorAdapter} with this new cursor containing updated course data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
