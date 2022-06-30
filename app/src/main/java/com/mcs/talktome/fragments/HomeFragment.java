package com.mcs.talktome.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mcs.talktome.adapters.RecentChatAdapter;
import com.mcs.talktome.databinding.FragmentHomeBinding;
import com.mcs.talktome.listeners.MyClickListener;
import com.mcs.talktome.listeners.RecentChatClickedListener;
import com.mcs.talktome.models.Message;
import com.mcs.talktome.models.User;
import com.mcs.talktome.utilities.Constants;
import com.mcs.talktome.utilities.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements RecentChatClickedListener {

    private FragmentHomeBinding mBinding;
    private SharedPreferencesManager mSharedPreferencesManager;
    private List<Message> mRecentMessages;
    private RecentChatAdapter mRecentChatAdapter;
    private FirebaseFirestore mDatabase;
    private MyClickListener callback;
    private Context mContext;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        mContext = container.getContext();

        init();
        listener();
        newMessageListener();

        return mBinding.getRoot();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.createCallbackToParentActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        callback.onFragmentChanged(Constants.FRAGMENT_HOME_ID);
    }

    private void listener() {
        mBinding.addChat.setOnClickListener(v -> {
            callback.onButtonAddClicked();
        });
    }

    private void createCallbackToParentActivity(){
        try {
            callback = (MyClickListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e + " Must implement OnButtonClickedListener");
        }
    }

    private void init() {
        mSharedPreferencesManager = new SharedPreferencesManager(mContext);
        mRecentMessages = new ArrayList<>();
        mRecentChatAdapter = new RecentChatAdapter(mRecentMessages, this);
        mBinding.recentChatRecycleView.setAdapter(mRecentChatAdapter);
        mDatabase = FirebaseFirestore.getInstance();
    }

    private void newMessageListener() {
        mDatabase.collection(Constants.KEY_COLLECTION_RECENT_MSG)
                .whereEqualTo(Constants.KEY_SENDER_ID, mSharedPreferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(mEventListener);
        mDatabase.collection(Constants.KEY_COLLECTION_RECENT_MSG)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, mSharedPreferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(mEventListener);
    }

    private final EventListener<QuerySnapshot> mEventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String sendId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    Message recentMessage = new Message();
                    recentMessage.setSenderId(sendId);
                    recentMessage.setReceiverId(receiverId);

                    if (mSharedPreferencesManager.getString(Constants.KEY_USER_ID).equals(sendId)) {
                        recentMessage.setRecentSenderId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                        recentMessage.setRecentSenderName(documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME));
                        recentMessage.setRecentSenderImg(documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE));

                    } else {
                        recentMessage.setRecentSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                        recentMessage.setRecentSenderName(documentChange.getDocument().getString(Constants.KEY_SENDER_NAME));
                        recentMessage.setRecentSenderImg(documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE));
                    }
                    recentMessage.setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                    recentMessage.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    mRecentMessages.add(recentMessage);

                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < mRecentMessages.size(); i++) {
                        String sendId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(mRecentMessages.get(i).getSenderId().equals(sendId)
                                && mRecentMessages.get(i).getReceiverId().equals(receiverId)) {
                            mRecentMessages.get(i).setMessage(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE));
                            mRecentMessages.get(i).setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                            break;
                        }
                    }
                }
            }
            Collections.sort(mRecentMessages, (object1, object2) -> object2.getDateObject().compareTo(object1.getDateObject()));
            mRecentChatAdapter.notifyDataSetChanged();
            mBinding.recentChatRecycleView.smoothScrollToPosition(0);
            mBinding.recentChatRecycleView.setVisibility(View.VISIBLE);
            mBinding.loadingBar.setVisibility(View.GONE);
        }
    });

    @Override
    public void onRecentChatClicked(User user) {
        callback.onRecentChatClicked(user);
    }

}