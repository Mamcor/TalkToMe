package com.mcs.talktome.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mcs.talktome.R;
import com.mcs.talktome.databinding.ActivityRegisterBinding;
import com.mcs.talktome.utilities.Constants;
import com.mcs.talktome.utilities.SharedPreferencesManager;
import com.mcs.talktome.utilities.ShowToast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding mBinding;
    private String mEncodeImage;
    private SharedPreferencesManager mSharedPreferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        configToolbar();
        mSharedPreferencesManager = new SharedPreferencesManager(getApplicationContext());
        setListeners();
    }

    private void configToolbar() {
        setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void setListeners() {
        mBinding.textLogin.setOnClickListener(view -> {
            onBackPressed();
        });

        mBinding.btnRegister.setOnClickListener(view -> {
            if (isValidRegistrationForm()) {
                register();
            }
        });

        mBinding.cardViewImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void register() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> userData = new HashMap<>();
        userData.put(Constants.KEY_USER_NAME, mBinding.registerName.getText().toString());
        userData.put(Constants.KEY_USER_EMAIL, mBinding.registerEmail.getText().toString());
        userData.put(Constants.KEY_USER_PASSWORD, mBinding.registerPassword.getText().toString());
        userData.put(Constants.KEY_USER_IMAGE, mEncodeImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(userData)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    mSharedPreferencesManager.putBoolean(Constants.KEY_IS_LOGIN, true);
                    mSharedPreferencesManager.putString(Constants.KEY_USER_ID, documentReference.getId()); //  save doc ref in sharedPref
                    mSharedPreferencesManager.putString(Constants.KEY_USER_NAME, mBinding.registerName.getText().toString());
                    mSharedPreferencesManager.putString(Constants.KEY_USER_IMAGE, mEncodeImage);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    loading(false);
                    ShowToast.toast(this, e.getMessage());
                });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            mBinding.userProfile.setImageBitmap(bitmap);
                            mEncodeImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidRegistrationForm() {
        if (mEncodeImage == null) {
            ShowToast.toast(this, "Choisissez une image de profil");
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.error));
            return false;
        } else if (mBinding.registerName.getText().toString().trim().isEmpty()) {
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.secondary_text));
            mBinding.registerNameLayout.setError("Entrez votre nom");
            return false;
        } else if (mBinding.registerEmail.getText().toString().trim().isEmpty()) {
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.secondary_text));
            mBinding.registerNameLayout.setErrorEnabled(false);
            mBinding.registerEmailLayout.setError("Entrez une adresse email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(mBinding.registerEmail.getText().toString()).matches()) {
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.secondary_text));
            mBinding.registerNameLayout.setErrorEnabled(false);
            mBinding.registerNameLayout.setErrorEnabled(false);
            mBinding.registerEmailLayout.setError("L'adresse email n'est pas valide");
            return false;
        } else if (mBinding.registerPassword.getText().toString().trim().isEmpty()) {
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.secondary_text));
            mBinding.registerNameLayout.setErrorEnabled(false);
            mBinding.registerEmailLayout.setErrorEnabled(false);
            mBinding.registerPasswordLayout.setError("Entrez un mot de passe");
            return false;
        } else if (mBinding.registerPassword.getText().toString().length() < 8) {
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.secondary_text));
            mBinding.registerNameLayout.setErrorEnabled(false);
            mBinding.registerEmailLayout.setErrorEnabled(false);
            mBinding.registerPasswordLayout.setError("Le mot de passe doit comporter au minimun 8 caractères");
            return false;
        } else if (mBinding.registerConfirmPassword.getText().toString().trim().isEmpty()) {
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.secondary_text));
            mBinding.registerNameLayout.setErrorEnabled(false);
            mBinding.registerEmailLayout.setErrorEnabled(false);
            mBinding.registerPasswordLayout.setErrorEnabled(false);
            mBinding.registerConfirmPasswordLayout.setError("Confirmer le mot de passe");
            return false;
        } else if (!mBinding.registerPassword.getText().toString().equals(mBinding.registerConfirmPassword.getText().toString())) {
            mBinding.choiceImage.setTextColor(getResources().getColor(R.color.secondary_text));
            mBinding.registerNameLayout.setErrorEnabled(false);
            mBinding.registerEmailLayout.setErrorEnabled(false);
            mBinding.registerPasswordLayout.setErrorEnabled(false);
            mBinding.registerConfirmPasswordLayout.setError("Le mot de passe et la confirmation doivent être identiques");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            mBinding.btnRegister.setEnabled(false);
            mBinding.btnRegister.setBackgroundColor(getResources().getColor(R.color.input_background));
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.progressBar.setVisibility(View.INVISIBLE);
            mBinding.btnRegister.setEnabled(false);
            mBinding.btnRegister.setBackgroundColor(getResources().getColor(R.color.primary));
        }
    }
}