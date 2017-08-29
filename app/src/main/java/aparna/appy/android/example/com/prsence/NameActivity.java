package aparna.appy.android.example.com.prsence;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * To accept username and store it in shared preferences when the app in opened for the first time
 */
public class NameActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_name);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString(getString(R.string.settings_name_key), getString(R.string.none));

        final EditText userName = (EditText) findViewById(R.id.username);


        if (username.equals(getString(R.string.none))) {
            //Do nothing
        } else {
            launchActivity();
        }

        //--Code for Username SharedPreference--//
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String user = userName.getText().toString();

                if (user.isEmpty()) {
                    Toast.makeText(NameActivity.this, getString(R.string.name_message), Toast.LENGTH_SHORT).show();

                } else {

                    String newUser = userName.getText().toString();

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getString(R.string.settings_name_key), newUser);
                    editor.commit();

                    launchActivity();
                }
            }
        });
    }

    private void launchActivity() {
        Intent intent = new Intent(NameActivity.this, Gender.class);
        startActivity(intent);
        finish();
    }
}
