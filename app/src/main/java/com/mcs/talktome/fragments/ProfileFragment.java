package com.mcs.talktome.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mcs.talktome.R;
import com.mcs.talktome.activities.MainActivity;
import com.mcs.talktome.databinding.FragmentHomeBinding;
import com.mcs.talktome.databinding.FragmentProfileBinding;
import com.mcs.talktome.listeners.MyClickListener;
import com.mcs.talktome.utilities.Constants;
import com.mcs.talktome.utilities.SharedPreferencesManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding mBinding;
    private SharedPreferencesManager mSharedPreferencesManager;
    private String mEncodeImage;
    private Context mContext;
    private MyClickListener callback;

    public static ProfileFragment newInstance() {
        return (new ProfileFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false);
        mContext = container.getContext();
        mSharedPreferencesManager = new SharedPreferencesManager(mContext);

        listeners();
        loadUserData();

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.createCallbackToParentActivity();
    }

    private void createCallbackToParentActivity(){
        try {
            callback = (MainActivity) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e + " Must implement OnButtonClickedListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        callback.onFragmentChanged(Constants.FRAGMENT_PROFILE_ID);
    }

    private void listeners() {
        mBinding.cardViewImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void loadUserData() {
        byte[] bytes = Base64.decode(
                mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE), Base64.DEFAULT
        );
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        mBinding.userProfile.setImageBitmap(bitmap);
        mBinding.registerName.setText(mSharedPreferencesManager.getString(Constants.KEY_USER_NAME));
        mBinding.registerEmail.setText(mSharedPreferencesManager.getString(Constants.KEY_USER_EMAIL));
        mBinding.registerPassword.setText(mSharedPreferencesManager.getString(Constants.KEY_USER_PASSWORD));
        mBinding.registerConfirmPassword.setText(mSharedPreferencesManager.getString(Constants.KEY_USER_PASSWORD));
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = mContext.getContentResolver().openInputStream(imageUri);
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

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}