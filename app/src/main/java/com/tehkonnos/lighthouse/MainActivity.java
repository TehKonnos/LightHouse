package com.tehkonnos.lighthouse;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tehkonnos.lighthouse.ui.accountSettings.SettingsActivity;
import com.tehkonnos.lighthouse.ui.firestore.DatabaseData;
import com.tehkonnos.lighthouse.ui.firestore.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore db;
    private static String username;
    private static Boolean switchPr;
    private final int RC_SIGN_IN = 69;
    @SuppressLint("StaticFieldLeak")
    public static NavController navController;
    private static DatabaseData databaseData;
    private FirebaseUser cUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        db =FirebaseFirestore.getInstance();
        databaseData = new ViewModelProvider(this).get(DatabaseData.class);


    }

    public static String getUsername() {
        return username;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                signOut();
                return true;
            case R.id.settings_account:
                try{
                    startActivity(new Intent(this, SettingsActivity.class));
                }catch (Exception e){
                    Log.e("OnSettingsClick","Error opening Settings: "+e.getMessage());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"Αποσυνδέθηκες επιτυχώς",Toast.LENGTH_SHORT).show();
                        //createSignInIntent();
                        username="";
                        recreate(); //TEST
                    }
                });
        // [END auth_fui_signout]

    }

    public void themeAndLogo() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_theme_logo]
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ic_launcher_background)      // Set logo drawable
                        //.setTheme(R.style.MySuperAppTheme)      // Set theme
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_theme_logo]
    }

    public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();
        // [START auth_fui_pp_tos]
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTosAndPrivacyPolicyUrls(
                                "https://example.com/terms.html",
                                "https://example.com/privacy.html")
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_pp_tos]
    }

    @Override
    protected void onStart() {
        cUser=FirebaseAuth.getInstance().getCurrentUser();
        if(cUser==null) createSignInIntent();
        else getUsernameFromDB();
        super.onStart();
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.SplashTheme)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]

    } //TODO Change logo

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                this.cUser = FirebaseAuth.getInstance().getCurrentUser();
                getUsernameFromDB();
                Toast.makeText(this,"Συνδέθηκες επιτυχώς!",Toast.LENGTH_SHORT).show();
                // ...
            } else {
                System.out.println("Result Code: "+resultCode);
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                finish();
                Toast.makeText(this,"Αποτυχία σύνδεσης",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getUsernameFromDB(){
        try {
            DocumentReference docRef = db.collection("Users").document(cUser.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if(document!=null){
                            Users user = document.toObject(Users.class);
                        if(user!=null && user.getUsername()!=null && !user.getUsername().equals("") && user.getShownName()!=null) {
                            username = user.getUsername();
                            switchPr = user.getShownName();
                        }else{
                            createUsername();
                            addUsername();
                        }
                    }else{
                        createUsername();
                        addUsername();
                    }
                }
            });
        }catch(Exception e){
            Log.e("Search Username on DB: ", ""+Objects.requireNonNull(e.getMessage()));
        }
    }

    private void createUsername(){
        if(cUser.getDisplayName()!=null && !cUser.getDisplayName().replace(" ","").equals("")){
            username=cUser.getDisplayName();
        }else{
            System.out.println("Starting new Settings Activity");
            startActivity(new Intent(this,SettingsActivity.class));
            Toast.makeText(this,"Θα χρειαστεί να μας δώσεις ένα username πρώτα!",Toast.LENGTH_LONG).show();
        }
    }
    public static void updateUser(){
        Users user = new Users();
        user.setUsername(username);
        user.setShownName(switchPr);
        try{
            db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).set(user);
        }catch(Exception e){
            Log.e("Addusername: ",""+e.getMessage());
        }
    }

    public static void addUsername(){
        Users user = new Users();
        user.setUsername(username);
        user.setShownName(true);
        try{
            db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).set(user);
        }catch(Exception e){
            Log.e("Addusername: ",""+e.getMessage());
        }
    }

    public static DatabaseData getDatabaseData() {
        return databaseData;
    }

    public static void setUsername(String username) {
        MainActivity.username = username;
    }

    public static void setSwitchPr(Boolean switchPr) {
        MainActivity.switchPr = switchPr;
    }

    public static Boolean getSwitchPr() {
        return switchPr;
    }
}