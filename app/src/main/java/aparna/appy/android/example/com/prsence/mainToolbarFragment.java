package aparna.appy.android.example.com.prsence;

/**
 * Created by Administrator on 8/15/2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static aparna.appy.android.example.com.prsence.R.string.p;

/**
 * Toolbar fragment which displays name, criteria, makeup classes, attendance percentage
 */

public class mainToolbarFragment extends Fragment {

    static private Context context;
    static private int total;
    static private int attend;
    static private float percent;
    private TextView username;
    private TextView criteria;
    private TextView track1;
    private TextView aggregate;
    private ImageView gender;

    public mainToolbarFragment() {
    }


    public static Fragment newInstance(Context mcontext, int t, int a, float p) {
        context = mcontext;
        total = t;
        attend = a;
        percent = p;
        return new mainToolbarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_sem_toolbar_fragment, container, false);

        username = (TextView) v.findViewById(R.id.user_name);
        criteria = (TextView) v.findViewById(R.id.attendance_criteria);
        track1 = (TextView) v.findViewById(R.id.toolbar_makeup);
        gender = (ImageView) v.findViewById(R.id.user_img);
        aggregate = (TextView) v.findViewById(R.id.aggregate);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        //Username
        String userName = sharedPrefs.getString(
                getString(R.string.settings_name_key), getString(R.string.settings_name_default));

        if (userName.isEmpty()) {

            userName = getString(R.string.settings_name_default);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(getString(R.string.settings_name_key), userName);
            editor.commit();
        } else if (userName.length() > 10) {

            userName = userName.substring(0, 10) + "..";
        }

        username.setText(userName);

        //Criteria
        String criTeria = sharedPrefs.getString(
                getString(R.string.settings_criteria_key), getString(R.string.settings_criteria_default));

        if (criTeria.isEmpty()) {
            criTeria = getString(R.string.settings_criteria_default);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(getString(R.string.settings_criteria_key), criTeria);
            editor.commit();
        }

        criteria.setText(getString(R.string.toolbar_criteria) + " " + criTeria + getString(p));

        //Makeup attendance
        int C = Integer.parseInt(criTeria);

        track1.setText(display_criteria(total, attend, percent, C));

        aggregate.setText(percent + context.getString(R.string.p));

        //Percentage
        // Get the appropriate text color based on the current percentage
        int pColor = getPercentageColor(percent, C);

        //Set text color
        aggregate.setTextColor(pColor);

        //Avatar
        //ImageSharedPref
        String userImage = sharedPrefs.getString(getString(R.string.settings_gender_key), getString(R.string.settings_gender_default));

        if (userImage.equalsIgnoreCase(getString(R.string.settings_gender_female_value))) {
            gender.setImageResource(R.drawable.girl);
        } else {
            gender.setImageResource(R.drawable.boy);
        }

        return v;
    }

    /**
     * Semester Percentage color
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
     * Semester percentage
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
     * To display appropriate make up classes text
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
}

