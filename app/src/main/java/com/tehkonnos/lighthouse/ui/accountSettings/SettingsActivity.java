package com.tehkonnos.lighthouse.ui.accountSettings;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.tehkonnos.lighthouse.MainActivity;
import com.tehkonnos.lighthouse.R;

import java.util.Objects;


public class SettingsActivity extends AppCompatActivity {
    private String oldUsername;
    private Boolean oldSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        oldUsername = MainActivity.getUsername();
        oldSwitch = MainActivity.getSwitchPr();

        Button savebutton = findViewById(R.id.saveSettings);
        savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        Button cancelButton = findViewById(R.id.cancelSettings);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });




    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        private EditTextPreference username;
        private SwitchPreferenceCompat switchAnon;
        private String oldUsername;
        private Boolean oldSwitch;


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.root_preferences);

            oldUsername = MainActivity.getUsername();

            username = findPreference("username");
            assert username != null;
            username.setText(MainActivity.getUsername());
            System.out.println("Settings: " + username.getText());
            username.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            switchAnon = findPreference("anonymous");
            assert switchAnon != null;
            switchAnon.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            oldSwitch = switchAnon.isChecked();

            final Preference delAccBtn = findPreference(getString(R.string.deleteAccount));
            assert delAccBtn != null;
            delAccBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) { //TODO Add prompt if user is sure
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(getContext(), "Ο λογαριασμός σας διαγράφηκε με επιτυχια", Toast.LENGTH_SHORT).show();
                                            requireActivity().recreate();

                                }});
                    return true;
                }
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if(username.toString().replace(" ","").equals("")){
                Toast.makeText(getContext(),"Δεν μπορείς να αφήσεις το όνομα σου κενό!",Toast.LENGTH_SHORT).show();
                username.setText(oldUsername);
            }else{
                if(!username.getText().equals(oldUsername) || oldSwitch&switchAnon.isChecked())
                MainActivity.setUsername(username.getText());
                MainActivity.setSwitchPr(switchAnon.isChecked());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if(MainActivity.getUsername()==null || MainActivity.getUsername().equals("")){
                Toast.makeText(getApplicationContext(),"Δεν μπορείς να αφήσεις το όνομα σου κενό!",Toast.LENGTH_SHORT).show();
            }else{
                MainActivity.setUsername(oldUsername);
                onBackPressed();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void save(){
        if(MainActivity.getUsername()==null || MainActivity.getUsername().equals("")){
            Toast.makeText(getApplicationContext(),"Δεν μπορείς να αφήσεις το όνομα σου κενό!",Toast.LENGTH_SHORT).show();
        }else{
            if(!MainActivity.getUsername().equals(oldUsername) || !oldSwitch&MainActivity.getSwitchPr())
                MainActivity.updateUser();
            Toast.makeText(getApplicationContext(),"Αποθήκευτηκε!",Toast.LENGTH_SHORT).show();
            //MainActivity.addUsername();
            finish();
        }
    }

    private void cancel(){
        if(MainActivity.getUsername()==null || MainActivity.getUsername().equals("")){
            Toast.makeText(getApplicationContext(),"Δεν μπορείς να αφήσεις το όνομα σου κενό!",Toast.LENGTH_SHORT).show();
        }else{
            MainActivity.setUsername(oldUsername);
            MainActivity.setSwitchPr(oldSwitch);
            finish();
        }
    }

}