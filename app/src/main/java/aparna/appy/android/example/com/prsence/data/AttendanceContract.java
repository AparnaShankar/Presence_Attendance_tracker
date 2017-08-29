package aparna.appy.android.example.com.prsence.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Administrator on 6/10/2017.
 */

public final class AttendanceContract {
    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "aparna.appy.android.example.com.prsence";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://aparna.appy.android.example.com.prsence/semester/ is a valid path for
     * looking at semester data. content://aparna.appy.android.example.com.prsence/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_SEMESTER = "semester";
    public static final String PATH_COURSE = "course";

    private AttendanceContract() {
    }

    public static final class SemesterEntry implements BaseColumns {
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of subjects.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEMESTER;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single subject.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEMESTER;

        /**
         * The content URI to access the semester data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SEMESTER);
        public static final String TABLE_NAME = "semester";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_SUBJECT_NAME = "name";
        public static final String COLUMN_TOTAL = "total";
        public static final String COLUMN_ATTENDED = "attended";
        public static final String COLUMN_CRITERIA = "criteria";

    }

    public static final class CourseEntry implements BaseColumns {
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of courses.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single course.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COURSE;

        /**
         * The content URI to access the course data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_COURSE);
        public static final String TABLE_NAME = "course";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_COURSE_NAME = "name";
        public static final String COLUMN_TOTAL = "total";
        public static final String COLUMN_HOURS = "attended";
    }
}

