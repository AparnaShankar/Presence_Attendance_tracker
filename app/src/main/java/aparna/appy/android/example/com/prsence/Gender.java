package aparna.appy.android.example.com.prsence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class Gender extends AppCompatActivity {

    private ImageButton male;
    private ImageButton female;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String gender = preferences.getString(getString(R.string.settings_gender_key), getString(R.string.none));

        male = (ImageButton) findViewById(R.id.male);
        female = (ImageButton) findViewById(R.id.female);

        //To check if app is just installed
        if (gender.equals(getString(R.string.none))) {
            //Do nothing
        } else {
            launchActivity();
        }

        //when clicked on male avatar
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = preferences.edit();

                editor.putString(getString(R.string.settings_gender_key), getString(R.string.settings_gender_male_value));

                editor.commit();

                launchActivity();
            }
        });

        //when clicked on female avatar
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = preferences.edit();

                editor.putString(getString(R.string.settings_gender_key), getString(R.string.settings_gender_female_value));

                editor.commit();

                launchActivity();

            }
        });

    }

    //Go to criteria activity
    private void launchActivity() {
        Intent intent = new Intent(Gender.this, CriteriaActivity.class);
        startActivity(intent);
        finish();
    }
}
