package com.mc.mychatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mc.mychatapp.databinding.ItemReceivedMessageContainerBinding;
import com.mc.mychatapp.databinding.ItemSentMessageContainerBinding;
import com.mc.mychatapp.models.Message;

import java.util.List;


/**
 * Cette adapteur doit adapter l'affichage de deux types d'item dans le recycleView: message envoyé
 * et message reçu. Chaque item aura donc son propre ViewHolder. Les items (pris depuis la base de donnée)
 * seront stocké dans un list transmis à l'adapteur. Pour différentes les item afin de les placer dans
 * le bon item_view, la fonction getItemViewType(int position) identifiera chaque item de la liste
 * de la list part son Id et lui affectera une valaur stockée dans une constante VIEW_TYPE. C'est
 * cette constante qui permettra a la méthode onBindViewHolder() de savoir quel item afficher dans
 * quel item_view
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Bitmap senderProfileImage, receiverProfileImage;
    private final List<Message> mMessages;
    private final String senderId;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public void setSenderProfileImage(Bitmap senderProfileImage) {
        this.senderProfileImage = senderProfileImage;
    }

    public void setReceiverProfileImage(Bitmap receiverProfileImage) {
        this.receiverProfileImage = receiverProfileImage;
    }

    public MessageAdapter(List<Message> messages,
                          Bitmap senderProfileImage,
                          Bitmap receiverProfileImage,
                          String senderId) {

        this.senderProfileImage = senderProfileImage;
        this.receiverProfileImage = receiverProfileImage;
        mMessages = messages;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SendMessageViewHolder(ItemSentMessageContainerBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false)
            );
        } else {
            return new ReceiverMessageViewHolder(ItemReceivedMessageContainerBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false)
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SendMessageViewHolder) holder).setData(mMessages.get(position), senderProfileImage);
        } else {
            ((ReceiverMessageViewHolder)holder).setData(mMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(mMessages.get(position).getSenderId().equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    //----------------------------------------------------------------
    //                     SENT MESSAGE VIEW HOLDER
    //----------------------------------------------------------------

    static class SendMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemSentMessageContainerBinding mBinding;

        SendMessageViewHolder(ItemSentMessageContainerBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void setData(Message message, Bitmap senderProfileImage) {
            mBinding.messageText.setText(message.getMessage());
            mBinding.messageDateTime.setText(message.getMessageDateTime());
            if (senderProfileImage != null) {
                mBinding.imageProfile.setImageBitmap(senderProfileImage);
            }
        }

    }

    //----------------------------------------------------------------
    //                     RECEIVED MESSAGE VIEW HOLDER
    //----------------------------------------------------------------

    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemReceivedMessageContainerBinding mBinding;

        ReceiverMessageViewHolder(ItemReceivedMessageContainerBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void setData(Message message, Bitmap receiverProfileImage) {
            mBinding.messageText.setText(message.getMessage());
            mBinding.messageDateTime.setText(message.getMessageDateTime());
            if (receiverProfileImage != null) {
                mBinding.imageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }
}
