package com.mcs.talktome.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mcs.talktome.databinding.ItemUserContainerBinding;
import com.mcs.talktome.listeners.UserClickedListener;
import com.mcs.talktome.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> mUserList;
    private UserClickedListener mUserClickedListener;

    public UsersAdapter(List<User> userList, UserClickedListener userClickedListener) {
        mUserList = userList;
        mUserClickedListener = userClickedListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserContainerBinding itemUserBinding = ItemUserContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(itemUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(mUserList.get(position));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    //-----------------------------------------------------
    //                  View Holder
    //-----------------------------------------------------

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemUserContainerBinding binding;

        UserViewHolder(ItemUserContainerBinding itemUserBinding) {
            super(itemUserBinding.getRoot());
            binding = itemUserBinding;
        }

        void setUserData(User user) {
            binding.userName.setText(user.name);
            binding.userEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(view -> mUserClickedListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String imageEncoded) {
        byte[] bytes = Base64.decode(imageEncoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
