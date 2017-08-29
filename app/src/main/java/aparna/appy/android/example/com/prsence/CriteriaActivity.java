package aparna.appy.android.example.com.prsence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * To set criteria intintally using shared preferences
 */
public class CriteriaActivity extends AppCompatActivity {

    SeekBar seekbar;
    TextView textView;
    int progress = 75;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criteria);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String criteria = preferences.getString(getString(R.string.settings_criteria_key), getString(R.string.none));

        //Checking if the app is just installed
        if (criteria.equals(getString(R.string.none))) {
            //Do nothing
        } else {
            launchActivity();
        }

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(100);
        seekbar.setProgress(progress);

        textView = (TextView) findViewById(R.id.textView3);
        textView.setText("" + progress + " " + getString(R.string.p));


        Button buttonNext = (Button) findViewById(R.id.next);

        //--Code for Shared Preference of criteria--//
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.settings_criteria_key), progress + "");
                editor.commit();

                launchActivity();

            }
        });


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override

            public void onProgressChanged(SeekBar seekBar, int m_progress, boolean fromUser) {
                progress = m_progress;
                textView.setText("" + m_progress + " " + getString(R.string.p));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }


        });
    }

    //Go to next activity
    private void launchActivity() {
        Intent intent = new Intent(CriteriaActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
