package com.zybooks.swapit;

public class User {
    private String fullname, email, password, zipcode, profilepic;

    public User(String name, String email, String password, String zipcode){
        this.fullname = name;
        this.email = email;
        this.password = password;
        this.zipcode = zipcode;

    }

    public void setName(String name){
        this.fullname = name;
    }
    public String getName(){
        return this.fullname;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getEmail(){
        return this.email;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public String getPassword(){
        return this.password;
    }
    public void setZip(String zipcode){
        this.zipcode = zipcode;
    }
    public String getZip(){
        return this.zipcode;
    }
    public void setProfilepic(String picture){
        this.profilepic = picture;
    }
    public String getProfilepic(){
        return this.profilepic;
    }


}
