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
import com.mcs.talktome.databinding.FragmentHomeBinding;
import com.mcs.talktome.databinding.FragmentSettingsBinding;
import com.mcs.talktome.listeners.MyClickListener;
import com.mcs.talktome.utilities.Constants;


public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding mBinding;
    private MyClickListener callback;

    public static SettingsFragment newInstance() {
        return (new SettingsFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSettingsBinding.inflate(inflater, container, false);
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
        callback.onFragmentChanged(Constants.FRAGMENT_SETTINGS_ID);
    }
}