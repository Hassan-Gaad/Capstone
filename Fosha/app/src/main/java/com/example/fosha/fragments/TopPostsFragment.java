package com.example.fosha.fragments;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class TopPostsFragment extends PostListFragment {

    public TopPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START my_top_posts_query]
        //top posts by number of stars
        String myUserId = getUid();
        Query myTopPostsQuery = databaseReference.child("posts").orderByChild("starCount");
        // [END my_top_posts_query]

        return myTopPostsQuery;
    }
}
