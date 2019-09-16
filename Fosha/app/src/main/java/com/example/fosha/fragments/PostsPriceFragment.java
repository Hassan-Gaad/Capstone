package com.example.fosha.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class PostsPriceFragment extends PostListFragment {

    public PostsPriceFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // place price
        String myUserId = getUid();
        Query myCheapPriceQuery = databaseReference.child("posts")
                .orderByChild("price");

        return myCheapPriceQuery;
    }
}