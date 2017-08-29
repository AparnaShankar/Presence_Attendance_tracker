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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry;

import static aparna.appy.android.example.com.prsence.R.string.p;

public class SemEditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static int EXISTING_SEM_LOADER = 1;
    /**
     * Content URI for the existing subject (null if it's a new subject)
     */
    private Uri mCurrentSubjectUri;

    /**
     * EditText field to enter the subjects's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the total classes
     */
    private EditText mTotalEditText;

    /**
     * EditText field to enter the attended classes
     */
    private EditText mAttendedEditText;

    /**
     * EditText field to enter the criteria
     */
    private TextView text_seekBar;
    private SeekBar seekBar;
    private int Progress = 0;

    /**
     * Boolean flag that keeps track of whether the subject has been edited (true) or not (false)
     */
    private boolean mSubjectHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mSubjectHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSubjectHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sem_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new subject or editing an existing one.
        Intent intent = getIntent();
        mCurrentSubjectUri = intent.getData();

        seekBar = (SeekBar) findViewById(R.id.seekbar);
        text_seekBar = (TextView) findViewById(R.id.criteriaText);
        seekBar.setMax(100);
        seekBar.setProgress(Progress);
        text_seekBar.setText(Progress + getString(p));

        // If the intent DOES NOT contain a subject content URI, then we know that we are
        // creating a new subject.
        if (mCurrentSubjectUri == null) {
            // This is a new subject, so change the app bar to say "Add a Subject"
            setTitle(getString(R.string.editor_activity_title_new_subject));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a subject that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing subject, so change app bar to say "Edit Subject"
            setTitle(getString(R.string.editor_activity_title_edit_subject));

            // Initialize a loader to read the subject data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_SEM_LOADER, null, this);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Progress = progress;
                text_seekBar.setText("" + Progress + getString(R.string.p));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.subject);
        mTotalEditText = (EditText) findViewById(R.id.total);
        mAttendedEditText = (EditText) findViewById(R.id.attended);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        seekBar.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mTotalEditText.setOnTouchListener(mTouchListener);
        mAttendedEditText.setOnTouchListener(mTouchListener);

    }

    /**
     * Get user input from editor and save subject into database.
     */
    private void saveSubject() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String totalString = mTotalEditText.getText().toString().trim();
        String attendedString = mAttendedEditText.getText().toString().trim();

        // Check if this is supposed to be a new subject
        // and check if all the fields in the editor are blank
        if (mCurrentSubjectUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(totalString) &&
                TextUtils.isEmpty(attendedString) && seekBar.getProgress() == 0) {
            // Since no fields were modified, we can return early without creating a new subject.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and subject attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(SemesterEntry.COLUMN_SUBJECT_NAME, nameString);

        // If the integer values are not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int total = 0;
        if (!TextUtils.isEmpty(totalString)) {
            total = Integer.parseInt(totalString);
        }
        values.put(SemesterEntry.COLUMN_TOTAL, total);

        int attended = 0;
        if (!TextUtils.isEmpty(attendedString)) {
            attended = Integer.parseInt(attendedString);
        }
        values.put(SemesterEntry.COLUMN_ATTENDED, attended);

        values.put(SemesterEntry.COLUMN_CRITERIA, Progress);

        if (nameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.editor_insert_subject_failed_name), Toast.LENGTH_SHORT).show();

        } else if (total < attended) {
            Toast.makeText(this, getString(R.string.editor_insert_subject_failed_greater), Toast.LENGTH_SHORT).show();

        } else {
            // Determine if this is a new or existing subject by checking if mCurrentSubjectUri is null or not
            if (mCurrentSubjectUri == null) {
                // This is a NEW subject, so insert a new subject into the provider,
                // returning the content URI for the new subject.
                Uri newUri = getContentResolver().insert(SemesterEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_subject_failed), Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_subject_successful), Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING subject, so update the subject with content URI: mCurrentSubjectUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentSubjectUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentSubjectUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0 || rowsAffected == -1) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_subject_failed), Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_subject_successful), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_seditor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new subject, hide the "Delete" menu item.
        if (mCurrentSubjectUri == null) {
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
                // Save subject to database
                saveSubject();
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
                // If the subject hasn't changed, continue with navigating up to parent activity
                // which is the {@link SemesterActivity}.
                if (!mSubjectHasChanged) {
                    NavUtils.navigateUpFromSameTask(SemEditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(SemEditorActivity.this);
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
        // If the subject hasn't changed, continue with handling back button press
        if (!mSubjectHasChanged) {
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
        // Since the editor shows all subject attributes, define a projection that contains
        // all columns from the subject table
        String[] projection = {
                SemesterEntry._ID,
                SemesterEntry.COLUMN_SUBJECT_NAME,
                SemesterEntry.COLUMN_TOTAL,
                SemesterEntry.COLUMN_ATTENDED,
                SemesterEntry.COLUMN_CRITERIA};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentSubjectUri,         // Query the content URI for the current subject
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
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of subject attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_SUBJECT_NAME);
            int totalColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_TOTAL);
            int attendedColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_ATTENDED);
            int criteriaColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_CRITERIA);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int total = cursor.getInt(totalColumnIndex);
            int attended = cursor.getInt(attendedColumnIndex);
            int criteria = cursor.getInt(criteriaColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTotalEditText.setText(Integer.toString(total));
            mAttendedEditText.setText(Integer.toString(attended));
            seekBar.setProgress(criteria);
            text_seekBar.setText(criteria + getString(R.string.p));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mTotalEditText.setText("");
        mAttendedEditText.setText("");
        seekBar.setProgress(0);
        text_seekBar.setText("");
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
                // and continue editing the subject.
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
     * Prompt the user to confirm that they want to delete this subject.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positvie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the subject.
                deleteSubject();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the subject.
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
     * Perform the deletion of the subject in the database.
     */
    private void deleteSubject() {
        // Only perform the delete if this is an existing subject.
        if (mCurrentSubjectUri != null) {
            // Call the ContentResolver to delete the subject at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentSubjectUri
            // content URI already identifies the subject that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentSubjectUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_subject_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_subject_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
