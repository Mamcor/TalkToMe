package com.mcs.talktome.listeners;


import com.mcs.talktome.models.User;

public interface MyClickListener {
    void onButtonAddClicked();
    void onUserSelected(User user);
    void onRecentChatClicked(User user);
    void onFragmentChanged(int flag);
    void onUserAvailable(User user);
}
