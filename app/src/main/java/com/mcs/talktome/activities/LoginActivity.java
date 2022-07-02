package com.mcs.talktome.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mcs.talktome.R;
import com.mcs.talktome.databinding.ActivityLoginBinding;
import com.mcs.talktome.utilities.Constants;
import com.mcs.talktome.utilities.Errors;
import com.mcs.talktome.utilities.SharedPreferencesManager;
import com.mcs.talktome.utilities.ShowToast;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding mBinding;
    private SharedPreferencesManager mSharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mSharedPreferencesManager = new SharedPreferencesManager(this);

        if (mSharedPreferencesManager.getBoolean(Constants.KEY_IS_LOGIN)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setListeners();
    }

    private void setListeners() {
        mBinding.textRegister.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        });

        mBinding.btnLogin.setOnClickListener(vi -> {
            if (isValidLoginForm()) {
                login();
            }
        });
    }

    private void login() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_EMAIL, mBinding.loginEmail.getText().toString())
                .whereEqualTo(Constants.KEY_USER_PASSWORD, mBinding.loginPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()
                            && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        mSharedPreferencesManager.putBoolean(Constants.KEY_IS_LOGIN, true);
                        mSharedPreferencesManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        mSharedPreferencesManager.putString(Constants.KEY_USER_NAME, documentSnapshot.getString(Constants.KEY_USER_NAME));
                        mSharedPreferencesManager.putString(Constants.KEY_USER_EMAIL, documentSnapshot.getString(Constants.KEY_USER_EMAIL));
                        mSharedPreferencesManager.putString(Constants.KEY_USER_PASSWORD, documentSnapshot.getString(Constants.KEY_USER_PASSWORD));
                        mSharedPreferencesManager.putString(Constants.KEY_USER_IMAGE, documentSnapshot.getString(Constants.KEY_USER_IMAGE));
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        mBinding.loginEmailLayout.setError(" ");
                        mBinding.loginPasswordLayout.setError("Identifient ou mot de passe incorrect");
                        ShowToast.toast(this, "Identifient ou mot de passe incorrect");
                    }
                });
    }

    private boolean isValidLoginForm() {
        if (mBinding.loginEmail.getText().toString().trim().isEmpty()) {
            mBinding.loginEmailLayout.setError("Renseignez votre adresse e-mail");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(mBinding.loginEmail.getText().toString()).matches()) {
           mBinding.loginEmailLayout.setError("Identifient incorrect");
            return false;
        } else if(mBinding.loginPassword.getText().toString().trim().isEmpty()) {
            mBinding.loginPassword.setError("Entrez votre mot de passe");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            mBinding.btnLogin.setEnabled(false);
            mBinding.btnLogin.setBackgroundColor(getResources().getColor(R.color.input_background));
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.progressBar.setVisibility(View.INVISIBLE);
            mBinding.btnLogin.setEnabled(true);
            mBinding.btnLogin.setBackgroundColor(getResources().getColor(R.color.primary));
        }
    }
}