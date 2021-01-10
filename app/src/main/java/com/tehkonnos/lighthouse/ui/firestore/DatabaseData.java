package com.tehkonnos.lighthouse.ui.firestore;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import static com.tehkonnos.lighthouse.MainActivity.db;


public class DatabaseData extends ViewModel {

    private MutableLiveData<List<SavedMarker>> mutableMarker;
    private MutableLiveData<List<Users>> mutableUser;
    private MutableLiveData<List<Feed>> mutableFeed;
    private List<SavedMarker> myMarker;
    private List<Users> myUser;
    private List<Feed> feedList;
    private List<Task<DocumentSnapshot>> tasks;



    public DatabaseData(){
        this.myMarker = new ArrayList<>();
        this.myUser = new ArrayList<>();
        this.feedList = new ArrayList<>();
        mutableMarker = new MutableLiveData<>();
        mutableUser = new MutableLiveData<>();
        mutableFeed = new MutableLiveData<>();
        tasks= new ArrayList<>();
        loadFeed();
    }

    private void loadFeed() {
        db.collection("Feed").orderBy("date", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
            feedList.addAll(task.getResult().toObjects(Feed.class));
            deployFeed(); //Deploys feed into view
    }}).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("DatabaseData Error: ",""+e.getMessage());
            }
        }); }


    private void deployFeed() {
        for(Feed feed: feedList) {
            tasks.add(db.document(feed.getMarker().getPath()).get());
            tasks.add(db.document(feed.getUser().getPath()).get());
        }
        Tasks.whenAllSuccess(tasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
            @Override
            public void onComplete(@NonNull Task<List<Object>> task) {
            for(int counter=0;counter<task.getResult().size();counter+=2){
                myMarker.add(((DocumentSnapshot) task.getResult().get(counter)).toObject(SavedMarker.class));
                myUser.add(((DocumentSnapshot) task.getResult().get(counter+1)).toObject(Users.class));
            }
            mutableFeed.setValue(feedList);
            mutableMarker.setValue(myMarker);
            mutableUser.setValue(myUser);
            }
        });

        }


    public LiveData<List<SavedMarker>> getMutableMarker() {
        return mutableMarker;
    }

    public LiveData<List<Users>> getMutableUser() {
        return mutableUser;
    }

    public LiveData<List<Feed>> getMutableFeed() {
        return mutableFeed;
    }
}

