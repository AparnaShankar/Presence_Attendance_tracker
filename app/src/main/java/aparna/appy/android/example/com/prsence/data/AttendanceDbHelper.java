package aparna.appy.android.example.com.prsence.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import aparna.appy.android.example.com.prsence.data.AttendanceContract.CourseEntry;
import aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry;

import static aparna.appy.android.example.com.prsence.data.AttendanceContract.SemesterEntry.COLUMN_SUBJECT_NAME;

/**
 * Created by Administrator on 6/10/2017.
 */

public class AttendanceDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "attendance.db";

    private static final int DATABASE_VERSION = 1;

    public AttendanceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_SEMESTER_TABLE = "CREATE TABLE " + SemesterEntry.TABLE_NAME + "("
                + SemesterEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SUBJECT_NAME + " TEXT NOT NULL, "
                + SemesterEntry.COLUMN_TOTAL + " INTEGER NOT NULL DEFAULT 0, "
                + SemesterEntry.COLUMN_ATTENDED + " INTEGER NOT NULL DEFAULT 0, "
                + SemesterEntry.COLUMN_CRITERIA + " INTEGER NOT NULL DEFAULT 0)";

        String SQL_CREATE_COURSE_TABLE = "CREATE TABLE " + CourseEntry.TABLE_NAME + "("
                + CourseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CourseEntry.COLUMN_COURSE_NAME + " TEXT NOT NULL, "
                + CourseEntry.COLUMN_TOTAL + " INTEGER NOT NULL, "
                + CourseEntry.COLUMN_HOURS + " INTEGER NOT NULL DEFAULT 0)";

        db.execSQL(SQL_CREATE_SEMESTER_TABLE);
        db.execSQL(SQL_CREATE_COURSE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
