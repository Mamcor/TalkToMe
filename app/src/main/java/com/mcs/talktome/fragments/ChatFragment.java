package com.mcs.talktome.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mcs.talktome.activities.MainActivity;
import com.mcs.talktome.adapters.MessageAdapter;
import com.mcs.talktome.databinding.FragmentChatBinding;
import com.mcs.talktome.listeners.MyClickListener;
import com.mcs.talktome.models.Message;
import com.mcs.talktome.models.User;
import com.mcs.talktome.network.ApiClient;
import com.mcs.talktome.network.ApiService;
import com.mcs.talktome.utilities.Constants;
import com.mcs.talktome.utilities.SharedPreferencesManager;
import com.mcs.talktome.utilities.ShowToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private FragmentChatBinding mBinding;
    private User receiver;
    private List<Message> mMessages;
    private MessageAdapter mMessageAdapter;
    private FirebaseFirestore database;
    private SharedPreferencesManager mSharedPreferencesManager;
    private String lastMessageDocId = null;
    private Boolean isReceiverAvailable = false;
    private Context mContext;
    private MyClickListener callback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentChatBinding.inflate(inflater, container, false);
        mContext = container.getContext();
        mSharedPreferencesManager = new SharedPreferencesManager(mContext);

        init();
        listeners();
        messageListener();

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
        callback.onFragmentChanged(Constants.FRAGMENT_CHAT_ID);
        callback.onUserAvailable(receiver);
    }

    private void createCallbackToParentActivity(){
        try {
            callback = (MainActivity) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e + " Must implement OnButtonClickedListener");
        }
    }

    private void loadReceiverDetail() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            receiver = (User) bundle.getSerializable(Constants.KEY_USER);
        }
    }

    private void init() {
        loadReceiverDetail();
        mSharedPreferencesManager = new SharedPreferencesManager(mContext);
        mMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(
                mMessages,
                getBitmapFromEncodedImage(mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE)),
                getBitmapFromEncodedImage(receiver.image),
                mSharedPreferencesManager.getString(Constants.KEY_USER_ID)
        );
        mBinding.messageRecycleView.setAdapter(mMessageAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listeners() {
        mBinding.sendMessageBtn.setOnClickListener(view -> sendMessage());
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, mSharedPreferencesManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiver.id);
        message.put(Constants.KEY_USER_MESSAGE, mBinding.messageInput.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_MSG).add(message);

        if (lastMessageDocId != null ) {
            updateLastMessage(mBinding.messageInput.getText().toString());
        } else {
            HashMap<String, Object> recentMessage = new HashMap<>();
            recentMessage.put(Constants.KEY_SENDER_ID, mSharedPreferencesManager.getString(Constants.KEY_USER_ID));
            recentMessage.put(Constants.KEY_SENDER_NAME, mSharedPreferencesManager.getString(Constants.KEY_USER_NAME));
            recentMessage.put(Constants.KEY_SENDER_IMAGE, mSharedPreferencesManager.getString(Constants.KEY_USER_IMAGE));
            recentMessage.put(Constants.KEY_RECEIVER_ID, receiver.id);
            recentMessage.put(Constants.KEY_RECEIVER_NAME, receiver.name);
            recentMessage.put(Constants.KEY_RECEIVER_IMAGE, receiver.image);
            recentMessage.put(Constants.KEY_LAST_MESSAGE, mBinding.messageInput.getText().toString());
            recentMessage.put(Constants.KEY_TIMESTAMP, new Date());
            addLastMessageInCollection(recentMessage);
        }
        getReceiverToken();
        if (!isReceiverAvailable){
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiver.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, mSharedPreferencesManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_USER_NAME, mSharedPreferencesManager.getString(Constants.KEY_USER_NAME));
                data.put(Constants.KEY_FCM_TOKEN, mSharedPreferencesManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_USER_MESSAGE, mBinding.messageInput.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);
                sendNotification(body.toString());
            } catch (Exception e) {
                ShowToast.toast(mContext, e.getMessage());
            }
        }
        mBinding.messageInput.setText(null);
    }

    private void getReceiverToken() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiver.id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                if (document.getLong(Constants.KEY_AVAILABILITY) != null) {
                                    int availability = document.getLong(Constants.KEY_AVAILABILITY).intValue();
                                    isReceiverAvailable = availability == 1;
                                }
                               receiver.token = document.getString(Constants.KEY_FCM_TOKEN);
                            }
                        }
                    }
                });
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(), messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray result = responseJson.getJSONArray("result");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) result.get(0);
                                ShowToast.toast(mContext, error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ShowToast.toast(mContext, "Notification envoyée");

                }else {
                    ShowToast.toast(mContext, "Erreur envoie" + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                ShowToast.toast(mContext, "Erreur: " + t.getMessage());
            }
        });
    }

    private void updateLastMessage(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_RECENT_MSG)
                .document(lastMessageDocId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void addLastMessageInCollection(HashMap<String, Object> recentMessage) {
        database.collection(Constants.KEY_COLLECTION_RECENT_MSG)
                .add(recentMessage)
                .addOnSuccessListener(documentReference -> lastMessageDocId = documentReference.getId());
    }

    private void messageListener() {
        database.collection(Constants.KEY_COLLECTION_MSG)
                .whereEqualTo(Constants.KEY_SENDER_ID, mSharedPreferencesManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiver.id)
                .addSnapshotListener(mEventListener);
        database.collection(Constants.KEY_COLLECTION_MSG)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiver.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, mSharedPreferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(mEventListener);
    }

    private final EventListener<QuerySnapshot> mEventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = mMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message();
                    message.setSenderId(documentChange.getDocument().getString(Constants.KEY_SENDER_ID));
                    message.setReceiverId(documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID));
                    message.setMessage(documentChange.getDocument().getString(Constants.KEY_USER_MESSAGE));
                    message.setMessageDateTime(getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP)));
                    message.setDateObject(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    mMessages.add(message);
                }
            }
            Collections.sort(mMessages, (object1, object2) ->
                    object1.getDateObject().compareTo(object2.getDateObject())
            );
            if (count == 0) {
                mMessageAdapter.notifyDataSetChanged();
            } else {
                mMessageAdapter.notifyItemRangeInserted(mMessages.size(), mMessages.size());
                mBinding.messageRecycleView.smoothScrollToPosition(mMessages.size() - 1);
            }
            mBinding.messageRecycleView.setVisibility(View.VISIBLE);
        }
        mBinding.loadingBar.setVisibility(View.GONE);

        if (lastMessageDocId == null) {
            getCurrentChatLastMessageDoc();
        }
    };

    private void getCurrentChatLastMessageDoc() {
        if (mMessages.size() > 0) {
            getLastMessageDoc(
                    mSharedPreferencesManager.getString(Constants.KEY_USER_ID),
                    receiver.id);
            getLastMessageDoc(
                    receiver.id,
                    mSharedPreferencesManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void getLastMessageDoc(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_RECENT_MSG)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(recentMessageOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> recentMessageOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            lastMessageDocId = documentSnapshot.getId();
        }
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM yyyy - hh:mm", Locale.FRANCE).format(date);
    }

    private Bitmap getBitmapFromEncodedImage(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

}