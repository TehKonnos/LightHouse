package com.tehkonnos.lighthouse;

import android.content.Intent;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tehkonnos.lighthouse.ui.home.HomeFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class MainActivityView extends Observable {
    private MutableLiveData<FirebaseUser> user;
    private FirebaseUser cUser;

    public MainActivityView() {
        this.cUser = FirebaseAuth.getInstance().getCurrentUser();
        if(cUser==null) createSignInIntent();
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        // Create and launch sign-in intent
        int RC_SIGN_IN = 69;
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.mipmap.ic_launcher_round)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]

    }

    private void startActivityForResult(Intent build, int rc_sign_in) {

    }
}
