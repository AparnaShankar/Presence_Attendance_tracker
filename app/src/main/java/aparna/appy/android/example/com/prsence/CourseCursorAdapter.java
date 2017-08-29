package aparna.appy.android.example.com.prsence;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.CourseEntry;

/**
 * Created by Administrator on 6/10/2017.
 */

public class CourseCursorAdapter extends CursorAdapter {


    private ProgressBar mProgress;

    /**
     * Constructs a new {@link CourseCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public CourseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.course_list_item, parent, false);
    }

    /**
     * This method binds the course data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current course can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {


        mProgress = (ProgressBar) view.findViewById(R.id.circularProgressbar);
        mProgress.setSecondaryProgress(100); // Secondary Progress
        mProgress.setMax(100); // Maximum Progress

        // Find individual views that we want to modify in the list item layout
        TextView course_name = (TextView) view.findViewById(R.id.course_name);
        TextView course_total = (TextView) view.findViewById(R.id.course_total_hours);
        TextView course_attended = (TextView) view.findViewById(R.id.course_comp_hours);
        TextView tv = (TextView) view.findViewById(R.id.tv);//percentage

        // Find the columns of course attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(CourseEntry.COLUMN_COURSE_NAME);
        int idColumnIndex = cursor.getColumnIndex(CourseEntry._ID);
        int totalColumnIndex = cursor.getColumnIndex(CourseEntry.COLUMN_TOTAL);
        int completedColumnIndex = cursor.getColumnIndex(CourseEntry.COLUMN_HOURS);

        // Read the course attributes from the Cursor for the current course
        final int id = cursor.getInt(idColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        final int total = cursor.getInt(totalColumnIndex);
        final int completed = cursor.getInt(completedColumnIndex);

        int complete = completed;
        int percent = complete * 100 / total;

        mProgress.setProgress(percent);   // Main Progress
        tv.setText(percent + context.getString(R.string.p));

        // Update the TextViews with the attributes for the current course
        course_name.setText(name);
        course_total.setText("" + total);
        course_attended.setText("" + completed);

        // 1 hour button
        Button one = (Button) view.findViewById(R.id.one_button);

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Total Hours incremented
                int tot = completed;
                tot++;

                //Toast message
                if (completed == total) {

                    Toast.makeText(context, R.string.completed, Toast.LENGTH_SHORT).show();
                } else if (tot > total) {

                    Toast.makeText(context, R.string.course_error, Toast.LENGTH_SHORT).show();
                } else {

                    //updating the card view and database
                    ContentValues values = new ContentValues();
                    Uri uri = ContentUris.withAppendedId(CourseEntry.CONTENT_URI, id);
                    values.put(CourseEntry.COLUMN_HOURS, tot);

                    int rowsAffected = context.getContentResolver().update(uri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == -1 || rowsAffected == 0) {

                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(context, R.string.update_course_failed, Toast.LENGTH_SHORT).show();
                    } else {

                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(context, R.string.editor_update_course_successful, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //2 hour Button
        Button two = (Button) view.findViewById(R.id.two_button);

        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Increment the total hours by 2
                float tot = completed;
                tot++;
                tot++;

                if (completed == total) {

                    Toast.makeText(context, R.string.completed, Toast.LENGTH_SHORT).show();
                } else if (tot > total) {

                    Toast.makeText(context, R.string.course_error, Toast.LENGTH_SHORT).show();
                } else {

                    //Updating the card and database
                    ContentValues values = new ContentValues();
                    Uri uri = ContentUris.withAppendedId(CourseEntry.CONTENT_URI, id);
                    values.put(CourseEntry.COLUMN_HOURS, tot);

                    int rowsAffected = context.getContentResolver().update(uri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == -1 || rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(context, R.string.update_course_failed, Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(context, R.string.editor_update_course_successful, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
