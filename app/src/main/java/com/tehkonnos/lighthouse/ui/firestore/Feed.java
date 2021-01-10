package com.tehkonnos.lighthouse.ui.firestore;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

public class Feed {

    private DocumentReference User,Marker;
    private int category;
    private Timestamp date;
    private GeoPoint geoPoint;

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public DocumentReference getUser() {
        return User;
    }

    public void setUser(DocumentReference user) {
        User = user;
    }

    public DocumentReference getMarker() {
        return Marker;
    }

    public void setMarker(DocumentReference marker) {
        Marker = marker;
    }
}
