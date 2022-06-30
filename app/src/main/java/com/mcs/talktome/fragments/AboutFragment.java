package com.mcs.talktome.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mcs.talktome.R;
import com.mcs.talktome.activities.MainActivity;
import com.mcs.talktome.databinding.FragmentAboutBinding;
import com.mcs.talktome.databinding.FragmentHomeBinding;
import com.mcs.talktome.listeners.MyClickListener;
import com.mcs.talktome.utilities.Constants;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding mBinding;
    private MyClickListener callback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentAboutBinding.inflate(inflater, container, false);
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
        callback.onFragmentChanged(Constants.FRAGMENT_ABOUT_ID);
    }
}