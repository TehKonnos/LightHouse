package com.tehkonnos.lighthouse.ui.firestore;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;


public class SavedMarker {
    private String description,needs,comments;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNeeds() {
        return needs;
    }

    public void setNeeds(String needs) {
        this.needs = needs;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
