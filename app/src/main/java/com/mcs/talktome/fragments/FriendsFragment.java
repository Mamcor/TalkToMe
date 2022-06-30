package com.mcs.talktome.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mcs.talktome.R;
import com.mcs.talktome.activities.MainActivity;
import com.mcs.talktome.adapters.UsersAdapter;
import com.mcs.talktome.databinding.FragmentFriendsBinding;
import com.mcs.talktome.listeners.MyClickListener;
import com.mcs.talktome.listeners.UserClickedListener;
import com.mcs.talktome.models.User;
import com.mcs.talktome.utilities.Constants;
import com.mcs.talktome.utilities.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class FriendsFragment extends Fragment implements UserClickedListener {

    private FragmentFriendsBinding mBinding;

    private MyClickListener callback;
    private SharedPreferencesManager mSharedPreferencesManager;
    private static List<User> users = new ArrayList<>();
    private static final List<User> searchList = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mBinding = FragmentFriendsBinding.inflate(inflater, container, false);

        init(container);
        configRecycleView(users);

        return mBinding.getRoot();
    }

    private void init(ViewGroup container) {
        mSharedPreferencesManager = new SharedPreferencesManager(container.getContext());
        users = getUsers();
        searchList.addAll(users);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Recherche...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    users.clear();
                    for (User user : searchList) {
                        if (user.name.toLowerCase().contains(newText.toLowerCase())) {
                            users.add(user);
                        }
                        mBinding.usersRecycleView.getAdapter().notifyDataSetChanged();
                    }
                }else {
                    users.clear();
                    users.addAll(searchList);
                    mBinding.usersRecycleView.getAdapter().notifyDataSetChanged();
                }

                return true;
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.createCallbackToParentActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        callback.onFragmentChanged(Constants.FRAGMENT_FRIENDS_ID);
    }

    @Override
    public void onStop() {
        callback.onFragmentChanged(Constants.FRAGMENT_FRIENDS_ID_BIS);
        super.onStop();
    }

    private void createCallbackToParentActivity(){
        try {
            callback = (MainActivity) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e + " Must implement OnButtonClickedListener");
        }
    }

    private List<User> getUsers() {
        loading(true);
        List<User> userList = new ArrayList<>();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = mSharedPreferencesManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_USER_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_USER_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_USER_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            userList.add(user);
                        }
                        if (userList.size() > 0) {
                            searchList.addAll(users);
                            configRecycleView(users);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
        return userList;
    }

    private void showErrorMessage(){
        mBinding.errorMessage.setText(String.format("%s", "Votre liste d'amis est vide"));
        mBinding.errorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
           mBinding.loadingBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.loadingBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        callback.onUserSelected(user);
    }

    private void configRecycleView(List<User> users) {
        UsersAdapter usersAdapter = new UsersAdapter(users, this);
        mBinding.usersRecycleView.setAdapter(usersAdapter);
        mBinding.usersRecycleView.setVisibility(View.VISIBLE);
    }
}