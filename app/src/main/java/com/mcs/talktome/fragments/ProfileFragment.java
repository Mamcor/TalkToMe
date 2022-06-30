package com.mcs.talktome.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.FileNotFoundException;
import java.io.InputStream;


public class ProfileFragment extends Fragment {

    FragmentProfileBinding mBinding;
    private MyClickListener callback;

    public static ProfileFragment newInstance() {
        return (new ProfileFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentProfileBinding.inflate(inflater, container, false);

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
}