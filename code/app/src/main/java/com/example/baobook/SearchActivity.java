package com.example.baobook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.baobook.controller.FirestoreHelper;
import com.example.baobook.model.User;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    SearchView searchView;
    ListView listView;
    FirestoreHelper firestoreHelper;
    UserArrayAdapter adapter;
    List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        searchView = findViewById(R.id.searchView);
        listView = findViewById(R.id.listView);
        firestoreHelper = new FirestoreHelper(); // Initialize FirestoreHelper

        users = new ArrayList<>();
        adapter = new UserArrayAdapter((Context) this, (ArrayList<User>) users, "SearchActivity",null);
        listView.setAdapter(adapter);

        searchView.setQueryHint("Search users");

         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return false;
            }
        });
         Button backButton = findViewById(R.id.back_button);
         backButton.setOnClickListener(v -> finish());
         listView.setOnItemClickListener((parent, view, position, id) -> {
             User selectedUser = users.get(position);
             Intent intent = new Intent(SearchActivity.this, UserProfileActivity.class);
             intent.putExtra("userID", selectedUser.getUsername());
             startActivity(intent);
         });
    }
    private void searchUsers(String query) {
        if (!query.isEmpty()) {
            firestoreHelper.searchUsers(query, foundUsers -> {
                users.clear();
                users.addAll(foundUsers);
                adapter.notifyDataSetChanged();
            });
        }
    }
}
