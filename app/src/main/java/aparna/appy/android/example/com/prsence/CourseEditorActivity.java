package aparna.appy.android.example.com.prsence;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.CourseEntry;

public class CourseEditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static int EXISTING_COURSE_LOADER = 1;
    /**
     * Content URI for the existing course (null if it's a new course)
     */
    private Uri mCurrentCoursetUri;

    /**
     * EditText field to enter the courses's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the total hours
     */
    private EditText mTotalEditText;

    /**
     * EditText field to enter the attended hours
     */
    private EditText mAttendedEditText;

    /**
     * Boolean flag that keeps track of whether the course has been edited (true) or not (false)
     */
    private boolean mCourseHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mCourseHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCourseHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new course or editing an existing one.
        Intent intent = getIntent();
        mCurrentCoursetUri = intent.getData();

        // If the intent DOES NOT contain a course content URI, then we know that we are
        // creating a new course.
        if (mCurrentCoursetUri == null) {
            // This is a new course, so change the app bar to say "Add a Course"
            setTitle(getString(R.string.editor_activity_title_new_course));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a course that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing course, so change app bar to say "Edit Course"
            setTitle(getString(R.string.editor_activity_title_edit_course));

            // Initialize a loader to read the course data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_COURSE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.course);
        mTotalEditText = (EditText) findViewById(R.id.total_hours);
        mAttendedEditText = (EditText) findViewById(R.id.hours_completed);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mTotalEditText.setOnTouchListener(mTouchListener);
        mAttendedEditText.setOnTouchListener(mTouchListener);
    }

    /**
     * Get user input from editor and save course into database.
     */
    private void saveCourse() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String totalString = mTotalEditText.getText().toString().trim();
        String attendedString = mAttendedEditText.getText().toString().trim();

        // Check if this is supposed to be a new course
        // and check if all the fields in the editor are blank
        if (mCurrentCoursetUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(totalString) &&
                TextUtils.isEmpty(attendedString)) {
            // Since no fields were modified, we can return early without creating a new course.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and course attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(CourseEntry.COLUMN_COURSE_NAME, nameString);

        // If the integer values are not provided by the user, don't try to parse the string into an integer value.
        // Use 0 by default.
        int total = 0;
        if (!TextUtils.isEmpty(totalString)) {
            total = Integer.parseInt(totalString);
        }
        values.put(CourseEntry.COLUMN_TOTAL, total);

        // If the integer values are not provided by the user, don't try to parse the string into an integer value.
        // Use 0 by default.
        int attended = 0;
        if (!TextUtils.isEmpty(attendedString)) {
            attended = Integer.parseInt(attendedString);
        }

        /**
         * Entering values in the database.
         */
        values.put(CourseEntry.COLUMN_HOURS, attended);
        if (nameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.editor_insert_course_failed_name), Toast.LENGTH_SHORT).show();
        } else if (total == 0) {
            Toast.makeText(this, getString(R.string.editor_insert_course_failed_total), Toast.LENGTH_SHORT).show();
        } else if (total < attended) {
            Toast.makeText(this, getString(R.string.editor_insert_course_failed_completed), Toast.LENGTH_SHORT).show();
        } else {

            // Determine if this is a new or existing course by checking if mCurrentCoursetUri is null or not
            if (mCurrentCoursetUri == null) {
                // This is a NEW course, so insert a new course into the provider,
                // returning the content URI for the new course.
                Uri newUri = getContentResolver().insert(CourseEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_course_failed), Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_course_successful), Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING course, so update the course with content URI: mCurrentCoursetUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentCoursetUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentCoursetUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0 || rowsAffected == -1) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_course_failed), Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_course_successful), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_ceditor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_ceditor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new course, hide the "Delete" menu item.
        if (mCurrentCoursetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save course to database
                saveCourse();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the course hasn't changed, continue with navigating up to parent activity
                // which is the {@link CourseActivity}.
                if (!mCourseHasChanged) {
                    NavUtils.navigateUpFromSameTask(CourseEditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(CourseEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the course hasn't changed, continue with handling back button press
        if (!mCourseHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Since the editor shows all course attributes, define a projection that contains
        // all columns from the course table
        String[] projection = {
                CourseEntry._ID,
                CourseEntry.COLUMN_COURSE_NAME,
                CourseEntry.COLUMN_TOTAL,
                CourseEntry.COLUMN_HOURS};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentCoursetUri,         // Query the content URI for the current course
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            // Find the columns of course attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(CourseEntry.COLUMN_COURSE_NAME);
            int totalColumnIndex = cursor.getColumnIndex(CourseEntry.COLUMN_TOTAL);
            int attendedColumnIndex = cursor.getColumnIndex(CourseEntry.COLUMN_HOURS);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int total = cursor.getInt(totalColumnIndex);
            int attended = cursor.getInt(attendedColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTotalEditText.setText(Integer.toString(total));
            mAttendedEditText.setText(Integer.toString(attended));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mTotalEditText.setText("");
        mAttendedEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the course.
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
     * Prompt the user to confirm that they want to delete this course.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg_course);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the course.
                deleteCourse();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the course.
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
     * Perform the deletion of the course in the database.
     */
    private void deleteCourse() {
        // Only perform the delete if this is an existing course.
        if (mCurrentCoursetUri != null) {
            // Call the ContentResolver to delete the course at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentCoursetUri
            // content URI already identifies the course that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentCoursetUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_course_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_course_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
