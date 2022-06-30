package com.mcs.talktome.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mcs.talktome.R;
import com.mcs.talktome.databinding.ActivityMainBinding;
import com.mcs.talktome.fragments.AboutFragment;
import com.mcs.talktome.fragments.ChatFragment;
import com.mcs.talktome.fragments.FriendsFragment;
import com.mcs.talktome.fragments.HomeFragment;
import com.mcs.talktome.fragments.ProfileFragment;
import com.mcs.talktome.fragments.SettingsFragment;
import com.mcs.talktome.listeners.MyClickListener;
import com.mcs.talktome.models.User;
import com.mcs.talktome.utilities.Constants;
import com.mcs.talktome.utilities.SharedPreferencesManager;
import com.mcs.talktome.utilities.ShowToast;

import java.util.HashMap;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements MyClickListener {

    private ActivityMainBinding mBinding;
    private static Fragment homeFragment;
    private static Fragment profileFragment;
    private static Fragment chatFragment;
    private static Fragment friendsFragment;
    private static Fragment aboutFragment;
    private static Fragment settingsFragment;
    private static Bundle mBundle;
    private SharedPreferencesManager mSharedPreferencesManager;
    private FirebaseFirestore database;
    private DocumentReference mDocumentReference;
    private Boolean isReceiverAvailable = false;
    private RoundedImageView navUserProfile;
    private TextView navUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        configToolbar();
        configDrawerLayout();
        configNavigationView();
        init();
        displayDefaultFragment();
        loadUserData();
        getToken();
        displayDefaultFragment();
        configDrawerLayout();
        configNavigationView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDocumentReference.update(Constants.KEY_AVAILABILITY, 0);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mDocumentReference.update(Constants.KEY_AVAILABILITY, 1);
    }

    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void init() {
        mSharedPreferencesManager = new SharedPreferencesManager(this);
        database = FirebaseFirestore.getInstance();
        mDocumentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(mSharedPreferencesManager.getString(Constants.KEY_USER_ID));

        mDocumentReference.update(Constants.KEY_AVAILABILITY, 1);

        View navHeader = mBinding.navigationView.getHeaderView(0);
        navUserProfile = navHeader.findViewById(R.id.nav_user_profile);
        navUserName = navHeader.findViewById(R.id.nav_user_name);

        mBundle = new Bundle();
    }

    private void configToolbar() {
        setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void configDrawerLayout() {
        mBinding.navigationView.setItemIconTintList(null);
        mBinding.navigationView.setItemTextColor(ColorStateList.valueOf(Color.WHITE));
        mBinding.iconesMenu.setOnClickListener(view -> mBinding.drawerLayout.openDrawer(Gravity.LEFT));
    }

    @SuppressLint("NonConstantResourceId")
    private void configNavigationView() {
        mBinding.navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_home:
                    displayFragment(Constants.FRAGMENT_HOME_ID, mBundle);
                    break;
                case R.id.menu_profile:
                    displayFragment(Constants.FRAGMENT_PROFILE_ID, mBundle);
                    break;
                case R.id.menu_chat:
                    if (mBundle.getSerializable(Constants.KEY_USER) != null) {
                        displayFragment(Constants.FRAGMENT_CHAT_ID, mBundle);
                        mBinding.userProfile.setImageBitmap(getBitmapFromEncodedImage(
                                ((User)mBundle.getSerializable(Constants.KEY_USER)).image
                                )
                        );
                        break;
                    }
                case R.id.menu_friends:
                    displayFragment(Constants.FRAGMENT_FRIENDS_ID, mBundle);
                    break;
                case R.id.menu_about:
                    displayFragment(Constants.FRAGMENT_ABOUT_ID, mBundle);
                    break;
                case R.id.menu_settings:
                    displayFragment(Constants.FRAGMENT_SETTINGS_ID, mBundle);
                    break;
                case R.id.menu_logout:
                    logout();
                    break;
            }
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void displayFragment(int fragmentId, Bundle bundle) {
        switch (fragmentId) {
            case Constants.FRAGMENT_HOME_ID:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                startTransactionFragment(homeFragment, bundle);
                break;
            case Constants.FRAGMENT_PROFILE_ID:
                if (profileFragment == null) {
                    profileFragment = new ProfileFragment();
                }
                startTransactionFragment(profileFragment, bundle);
                break;
            case Constants.FRAGMENT_CHAT_ID:
                if (chatFragment == null) {
                    chatFragment = new ChatFragment();
                }
                startTransactionFragment(chatFragment, bundle);
                break;
            case Constants.FRAGMENT_FRIENDS_ID:
                if (friendsFragment == null) {
                    friendsFragment = new FriendsFragment();
                }
                startTransactionFragment(friendsFragment, bundle);
                break;
            case Constants.FRAGMENT_ABOUT_ID:
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                }
                startTransactionFragment(aboutFragment, bundle);
                break;
            case Constants.FRAGMENT_SETTINGS_ID:
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                }
                startTransactionFragment(settingsFragment, bundle);
                break;
            default:
                break;
        }
    }

    private void startTransactionFragment(Fragment fragment, Bundle bundle){
        if (!fragment.isVisible()){
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_host, fragment, null)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void displayDefaultFragment() {
        Fragment fragmentHost = getSupportFragmentManager().findFragmentById(R.id.fragment_host);
        if (fragmentHost == null) {
            displayFragment(Constants.FRAGMENT_HOME_ID, mBundle);
            mBinding.navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    private void displayChatFragment(User user) {
        mBundle.putSerializable(Constants.KEY_USER, user);
        mBundle.putBoolean(Constants.KEY_AVAILABILITY, isReceiverAvailable);
        displayFragment(Constants.FRAGMENT_CHAT_ID, mBundle);
        mBinding.userProfile.setImageBitmap(getBitmapFromEncodedImage(user.image));
        mBinding.navigationView.getMenu().getItem(Constants.FRAGMENT_CHAT_ID).setChecked(true);
    }

    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        mSharedPreferencesManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(mSharedPreferencesManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token).addOnFailureListener(e ->
                ShowToast.toast(this, "Unable to update token")
        );
    }

    private void logout() {
        ShowToast.toast(this, "Déconnexion");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(mSharedPreferencesManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates).addOnSuccessListener(unused -> {
                    mSharedPreferencesManager.clear();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }).addOnFailureListener(e ->
                ShowToast.toast(this, "La déconnection à échouée")
        );
    }

    private void loadUserData() {
        byte[] bytes = Base64.decode(
                mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE), Base64.DEFAULT
        );
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        mBinding.userProfile.setImageBitmap(bitmap);
        navUserProfile.setImageBitmap(bitmap);
        navUserName.setText(mSharedPreferencesManager.getString(Constants.KEY_USER_NAME));
    }

    @Override
    public void onButtonAddClicked() {
        displayFragment(Constants.FRAGMENT_FRIENDS_ID, mBundle);
        mBinding.navigationView.getMenu().getItem(Constants.FRAGMENT_FRIENDS_ID).setChecked(true);
    }

    @Override
    public void onUserSelected(User user) {
        displayChatFragment(user);
    }

    @Override
    public void onRecentChatClicked(User user) {
        displayChatFragment(user);
    }

    @Override
    public void onFragmentChanged(int flag) {
        switch (flag) {
            case Constants.FRAGMENT_HOME_ID:
                mBinding.toolbarTitle.setText(R.string.app_name);
                mBinding.userProfile.setImageBitmap(getBitmapFromEncodedImage(
                        mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE))
                );
                mBinding.userAvailability.setVisibility(View.GONE);
                break;
            case Constants.FRAGMENT_PROFILE_ID:
                mBinding.toolbarTitle.setText(R.string.profile);
                mBinding.userProfile.setImageBitmap(getBitmapFromEncodedImage(
                        mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE))
                );
                mBinding.userAvailability.setVisibility(View.GONE);
                break;
            case Constants.FRAGMENT_CHAT_ID:
                mBinding.toolbarTitle.setText(R.string.chat);
                break;
            case Constants.FRAGMENT_FRIENDS_ID:
                mBinding.toolbarTitle.setText(R.string.friends);
                mBinding.userProfile.setVisibility(View.GONE);
                mBinding.userAvailability.setVisibility(View.GONE);
                break;
            case Constants.FRAGMENT_FRIENDS_ID_BIS:
                mBinding.userProfile.setVisibility(View.VISIBLE);
                break;
            case Constants.FRAGMENT_ABOUT_ID:
                mBinding.toolbarTitle.setText(R.string.about);
                mBinding.userProfile.setImageBitmap(getBitmapFromEncodedImage(
                        mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE))
                );
                mBinding.userAvailability.setVisibility(View.GONE);
                break;
            case Constants.FRAGMENT_SETTINGS_ID:
                mBinding.toolbarTitle.setText(R.string.settings);
                mBinding.userProfile.setImageBitmap(getBitmapFromEncodedImage(
                        mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE))
                );
                mBinding.userAvailability.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onUserAvailable(User user) {
        userAvailabilityListener(user);
    }


    private void userAvailabilityListener(User user) {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(user.id)
                .addSnapshotListener(((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null){
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            int availability = value.getLong(Constants.KEY_AVAILABILITY).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        //user.token = value.getString(Constants.KEY_FCM_TOKEN);
                    }
                    if (isReceiverAvailable) {
                        mBinding.userAvailability.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.userAvailability.setVisibility(View.GONE);
                    }
                })
                );
    }

}