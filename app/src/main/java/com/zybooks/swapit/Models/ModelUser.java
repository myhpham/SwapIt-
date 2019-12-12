package com.zybooks.swapit.Models;

import android.graphics.ColorSpace;

public class ModelUser {

    String name, email, image, uid, onlineStatus;

    public ModelUser(){}

    public ModelUser(String name, String email, String image, String uid, String onlineStatus) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.uid = uid;
        this.onlineStatus = onlineStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}
