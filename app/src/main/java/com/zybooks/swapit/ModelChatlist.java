package com.zybooks.swapit;

public class ModelChatlist {
    String id; //we need this to get sender/receiver uid

    public ModelChatlist(){}

    public ModelChatlist(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
