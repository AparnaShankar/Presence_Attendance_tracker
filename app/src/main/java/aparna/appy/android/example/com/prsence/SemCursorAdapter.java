package aparna.appy.android.example.com.prsence;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry;

/**
 * Created by Administrator on 6/10/2017.
 */

public class SemCursorAdapter extends CursorAdapter {

    private TextView nameTextView;
    private TextView percentTextView;
    private TextView attendedTextView;
    private TextView totalTextView;
    private TextView trackTextView;
    private Context context;

    /**
     * Constructs a new {@link SemCursorAdapter}.
     *
     * @param mcontext The context
     * @param c        The cursor from which to get the data.
     */
    public SemCursorAdapter(Context mcontext, Cursor c) {
        super(mcontext, c, 0 /* flags */);
        context = mcontext;
    }

    /**
     * To get percentage color
     *
     * @param p percentage
     * @param c criteria
     * @return color
     */
    private int getPercentageColor(float p, int c) {
        int colorResourceId;
        if (c == 0) {
            colorResourceId = R.color.safe;
        } else if (p >= (float) c) {
            colorResourceId = R.color.safe;
        } else {
            colorResourceId = R.color.danger;
        }
        return ContextCompat.getColor(context, colorResourceId);
    }

    /**
     * Subject percentage
     *
     * @param a attended classes
     * @param t total classes
     * @return percentage
     */
    private float percentage(int t, int a) {
        float p = 0.0f;
        p = (float) a / (float) t * 100;
        int scale = (int) Math.pow(10, 1);
        p = (float) Math.round(p * scale) / scale;
        return p;
    }

    /**
     * To get appropriate makeup text
     *
     * @param t total
     * @param a attended
     * @param p percentage
     * @param c criteria
     * @return make up classes as string
     */
    private String display_criteria(int t, int a, float p, int c) {
        int a1 = a;
        String track = context.getString(R.string.track);
        if (c == 0) {
            track = context.getString(R.string.no_criteria);
        } else if (t == 0) {
            track = context.getString(R.string.no_start);
        } else if (p < (float) c && t != 0) {
            while (true) {
                t++;
                a1++;
                if (percentage(t, a1) >= (float) c)
                    break;
            }
            int makeup = a1 - a;
            track = context.getString(R.string.makeup_1) + " " + makeup + " " + context.getString(R.string.makeup_2);
        }
        return track;

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

        return LayoutInflater.from(context).inflate(R.layout.sem_list_item, parent, false);
    }

    /**
     * This method binds the subject data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current subject can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        nameTextView = (TextView) view.findViewById(R.id.subject_name);
        percentTextView = (TextView) view.findViewById(R.id.subject_percent);
        attendedTextView = (TextView) view.findViewById(R.id.subject_attended);
        totalTextView = (TextView) view.findViewById(R.id.subject_total);
        trackTextView = (TextView) view.findViewById(R.id.track);

        // Find the columns of subject attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(SemesterEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_SUBJECT_NAME);
        int totalColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_TOTAL);
        int attendedColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_ATTENDED);
        int criteriaColumnIndex = cursor.getColumnIndex(SemesterEntry.COLUMN_CRITERIA);


        // Read the subject attributes from the Cursor for the current subject
        final int id = cursor.getInt(idColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        final int total = cursor.getInt(totalColumnIndex);
        final int attended = cursor.getInt(attendedColumnIndex);
        int criteria = cursor.getInt(criteriaColumnIndex);

        float percent = 0.0f;
        //Check if denominator is not zero
        if (total > 0) {
            percent = percentage(total, attended);
        }
        //makeup information
        String track = display_criteria(total, attended, percent, criteria);

        // Update the TextViews with the attributes for the current subject
        nameTextView.setText(name);
        totalTextView.setText("" + total);
        attendedTextView.setText("" + attended);
        percentTextView.setText("" + percent + context.getString(R.string.p));
        trackTextView.setText(track);

        //Background color for percentage
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable percentageCircle = (GradientDrawable) percentTextView.getBackground();

        // Get the appropriate background color based on the current percentage
        int pColor = getPercentageColor(percent, criteria);

        // Set the color on the magnitude circle
        percentageCircle.setColor(pColor);

        //Absent Button
        Button total_add = (Button) view.findViewById(R.id.total_button);

        total_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int tot = total;

                tot++;

                ContentValues values = new ContentValues();
                Uri uri = ContentUris.withAppendedId(SemesterEntry.CONTENT_URI, id);
                values.put(SemesterEntry.COLUMN_TOTAL, tot);

                int rowsAffected = context.getContentResolver().update(uri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(context, R.string.update_subject_total_fail, Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(context, R.string.update_subject_total_sucess, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Present Button
        Button attended_add = (Button) view.findViewById(R.id.attended_button);

        attended_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int tot = total;
                int at = attended;

                tot++;
                at++;

                ContentValues values = new ContentValues();
                Uri uri = ContentUris.withAppendedId(SemesterEntry.CONTENT_URI, id);
                values.put(SemesterEntry.COLUMN_ATTENDED, at);
                values.put(SemesterEntry.COLUMN_TOTAL, tot);

                int rowsAffected = context.getContentResolver().update(uri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(context, R.string.update_subject_attended_fail, Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(context, R.string.update_subject_attended_sucess, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

