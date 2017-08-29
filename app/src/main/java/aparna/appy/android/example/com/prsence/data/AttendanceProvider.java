package aparna.appy.android.example.com.prsence.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.CourseEntry;
import aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry;

import static aparna.appy.android.example.com.prsence.data.AttendanceContract.CONTENT_AUTHORITY;
import static aparna.appy.android.example.com.prsence.data.AttendanceContract.PATH_COURSE;
import static aparna.appy.android.example.com.prsence.data.AttendanceContract.PATH_SEMESTER;

/**
 * Created by Administrator on 6/10/2017.
 */

public class AttendanceProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = AttendanceProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the semester table
     */
    private static final int SEMESTER = 100;

    /**
     * URI matcher code for the content URI for a single subject in the semester table
     */
    private static final int SEMESTER_ID = 101;

    /**
     * URI matcher code for the content URI for the course table
     */
    private static final int COURSE = 200;

    /**
     * URI matcher code for the content URI for a single course in the course table
     */
    private static final int COURSE_ID = 201;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://aparna.appy.android.example.com.prsence/semester" will map to the
        // integer code {@link #SEMESTER}. This URI is used to provide access to MULTIPLE rows
        // of the semester table.
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_SEMESTER, SEMESTER);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_COURSE, COURSE);

        // The content URI of the form "content://aparna.appy.android.example.com.prsence/semester/#" will map to the
        // integer code {@link #SEMESTER_ID}. This URI is used to provide access to ONE single row
        // of the semester table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://aparna.appy.android.example.com.prsence/semester/3" matches, but
        // "content://aparna.appy.android.example.com.prsence/semester" (without a number at the end) doesn't match.
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_SEMESTER + "/#", SEMESTER_ID);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_COURSE + "/#", COURSE_ID);
    }

    /**
     * Database helper object
     */
    private AttendanceDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new AttendanceDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SEMESTER:
                // For the SEMESTER code, query the semester table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(SemesterEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SEMESTER_ID:
                // For the SEMESTER code, extract out the ID from the URI.
                // For an example URI such as "content://aparna.appy.android.example.com.prsence/semester/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = SemesterEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the semester table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(SemesterEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case COURSE:
                //COURSE
                cursor = database.query(CourseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case COURSE_ID:
                // COURSE_ID
                selection = CourseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(CourseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEMESTER:
                return insertSubject(uri, contentValues);
            case COURSE:
                return insertCourse(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a subject into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSubject(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(SemesterEntry.COLUMN_SUBJECT_NAME);
        if (name == null || name.length() == 0) {
            return null;
        }

        // If the total is provided, check that it's greater than or equal to 0
        Integer total = values.getAsInteger(SemesterEntry.COLUMN_TOTAL);
        if (total != null && total < 0) {
            return null;
        }
        // If the attended is provided, check that it's greater than or equal to 0
        Integer attended = values.getAsInteger(SemesterEntry.COLUMN_ATTENDED);
        if (attended != null && attended < 0) {
            return null;
        }
        // If the criteria is provided, check that it's greater than or equal to 0
        Integer criteria = values.getAsInteger(SemesterEntry.COLUMN_CRITERIA);
        if (criteria != null && criteria < 0) {
            return null;
        }
        if (attended > total) {
            return null;
        }
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new subject with the given values
        long id = database.insert(SemesterEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the semester content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Insert a subject into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertCourse(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(CourseEntry.COLUMN_COURSE_NAME);
        if (name == null || name.length() == 0) {
            return null;
        }

        // If the total is provided, check that it's greater than or equal to 0
        Integer total = values.getAsInteger(CourseEntry.COLUMN_TOTAL);
        if (total != null && total <= 0) {
            return null;
        }

        // If the attended hours is provided, check that it's greater than or equal to 0
        Integer attended = values.getAsInteger(CourseEntry.COLUMN_HOURS);
        if (attended != null && attended < 0) {
            return null;
        }
        if (attended > total) {
            return null;
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new course with the given values
        long id = database.insert(CourseEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the semester content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEMESTER:
                return updateSubject(uri, contentValues, selection, selectionArgs);
            case SEMESTER_ID:
                // For the SEMESTER_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = SemesterEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateSubject(uri, contentValues, selection, selectionArgs);
            case COURSE:
                return updateCourse(uri, contentValues, selection, selectionArgs);
            case COURSE_ID:
                selection = CourseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCourse(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update subjects and overall in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more subjects).
     * Return the number of rows that were successfully updated.
     */
    private int updateSubject(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link SemesterEntry#COLUMN_SUBJECT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(SemesterEntry.COLUMN_SUBJECT_NAME)) {
            String name = values.getAsString(SemesterEntry.COLUMN_SUBJECT_NAME);
            if (name == null || name.length() == 0) {
                return -1;
            }
        }
        // If the {@link SemesterEntry#COLUMN_TOTAL} key is present,
        // check that the total value is valid.
        Integer total = 0;
        if (values.containsKey(SemesterEntry.COLUMN_TOTAL)) {
            // Check that the total is greater than or equal to 0
            total = values.getAsInteger(SemesterEntry.COLUMN_TOTAL);
            if (total != null && total < 0) {
                return -1;
            }
        }

        // If the {@link SemesterEntry#COLUMN_ATTENDED} key is present,
        // check that the attended value is valid.
        if (values.containsKey(SemesterEntry.COLUMN_ATTENDED)) {
            // Check that the attended is greater than or equal to 0
            Integer attended = values.getAsInteger(SemesterEntry.COLUMN_ATTENDED);
            if (attended != null && attended < 0) {
                return -1;
            }
            if (attended > total) {
                return -1;
            }
        }

        // If the {@link SemesterEntry#COLUMN_CRITERIA} key is present,
        // check that the criteria value is valid.
        if (values.containsKey(SemesterEntry.COLUMN_CRITERIA)) {
            // Check that the critera is greater than or equal to 0
            Integer criteria = values.getAsInteger(SemesterEntry.COLUMN_CRITERIA);
            if (criteria != null && criteria < 0) {
                return -1;
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(SemesterEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Update courses in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more courses).
     * Return the number of rows that were successfully updated.
     */
    private int updateCourse(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link CourseEntry#COLUMN_COURSE_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(CourseEntry.COLUMN_COURSE_NAME)) {
            String name = values.getAsString(CourseEntry.COLUMN_COURSE_NAME);
            if (name == null || name.length() == 0) {
                return -1;
            }
        }
        // If the {@link CourseEntry#COLUMN_TOTAL} key is present,
        // check that the total value is valid.
        Integer total = 0;
        if (values.containsKey(CourseEntry.COLUMN_TOTAL)) {
            // Check that the total is greater than or equal to 1
            total = values.getAsInteger(CourseEntry.COLUMN_TOTAL);
            if (total != null && total <= 0) {
                return -1;
            }
        }

        // If the {@link CourseEntry#COLUMN_HOURS} key is present,
        // check that the attended value is valid.
        if (values.containsKey(CourseEntry.COLUMN_HOURS)) {
            // Check that the attended hours is greater than or equal to 0
            Integer hours = values.getAsInteger(CourseEntry.COLUMN_HOURS);
            if (hours != null && hours < 0) {
                return -1;
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(CourseEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEMESTER:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(SemesterEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SEMESTER_ID:
                // Delete a single row given by the ID in the URI
                selection = SemesterEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SemesterEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COURSE:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(CourseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COURSE_ID:
                // Delete a single row given by the ID in the URI
                selection = CourseEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(CourseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEMESTER:
                return SemesterEntry.CONTENT_LIST_TYPE;
            case SEMESTER_ID:
                return SemesterEntry.CONTENT_ITEM_TYPE;
            case COURSE:
                return CourseEntry.CONTENT_LIST_TYPE;
            case COURSE_ID:
                return CourseEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}