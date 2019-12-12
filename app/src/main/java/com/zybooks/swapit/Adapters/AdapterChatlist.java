package com.zybooks.swapit.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.zybooks.swapit.Models.ModelUser;
import com.zybooks.swapit.R;
import com.zybooks.swapit.Activities.chatActivity;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder>{

    List<ModelUser> userList; //get user info
    Context context;
    private HashMap<String, String> lastMessageMap;

    //constructor
    public AdapterChatlist(Context context, List<ModelUser> userList){
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate row_chatlist.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        final String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        holder.nameTV.setText(userName);
        if(lastMessage==null || lastMessage.equals("default")){
            holder.lastmessageTV.setVisibility(View.GONE);
        } else{
            holder.lastmessageTV.setVisibility(View.VISIBLE);
            holder.lastmessageTV.setText(lastMessage);
        }
        try{
            Picasso.get().load(userImage).into(holder.profileIV);
        } catch (Exception e){
            Picasso.get().load(R.drawable.ic_person_outline_black_24dp).into(holder.profileIV);
        }

        //handle click in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, chatActivity.class);
                intent.putExtra("SELLER_ID", hisUid);
                context.startActivity(intent);

            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }


    @Override
    public int getItemCount() {
        return userList.size(); //size of the list
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //row_chatlist.xml views
        ImageView profileIV;
        TextView nameTV, lastmessageTV;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            profileIV = itemView.findViewById(R.id.chatlist_userpic);
            nameTV = itemView.findViewById(R.id.chatlist_name);
            lastmessageTV = itemView.findViewById(R.id.chatlist_lastMessage);
        }
    }
}
