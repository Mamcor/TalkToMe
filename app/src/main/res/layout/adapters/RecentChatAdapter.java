package com.mc.mychatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mc.mychatapp.databinding.ItemRecentChatContainerBinding;
import com.mc.mychatapp.listeners.NewMessageListener;
import com.mc.mychatapp.models.Message;
import com.mc.mychatapp.models.User;

import java.util.List;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.RecentChatViewHolder> {

    private final List<Message> recentMessages;
    private final NewMessageListener mNewMessageListener;

    public RecentChatAdapter(List<Message> recentMessages, NewMessageListener newMessageListener) {
        this.recentMessages = recentMessages;
        this.mNewMessageListener = newMessageListener;
    }

    @NonNull
    @Override
    public RecentChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentChatViewHolder(ItemRecentChatContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull RecentChatViewHolder holder, int position) {
        holder.setData(recentMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return recentMessages.size();
    }

    //-----------------------------------------------------
    //                  View Holder
    //-----------------------------------------------------

    class RecentChatViewHolder extends RecyclerView.ViewHolder {

        private ItemRecentChatContainerBinding mBinding;

        public RecentChatViewHolder(ItemRecentChatContainerBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        private void setData(Message message) {
            mBinding.recentReceiverImg.setImageBitmap(decodeImage(message.getRecentSenderImg()));
            mBinding.recentReceiverName.setText(message.getRecentSenderName());
            mBinding.recentReceiverMsg.setText(message.getMessage());

            mBinding.getRoot().setOnClickListener(view -> {
                // Création d'un user (sérialisable) qui sera transmis à ChatActivity via un intent
                User user = new User();
                user.id = message.getRecentSenderId();
                user.name = message.getRecentSenderName();
                user.image = message.getRecentSenderImg();
                mNewMessageListener.onMessageClicked(user);
            });
        }
    }

    private Bitmap decodeImage(String encodedImg) {
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
