package com.zybooks.swapit;

import android.content.Context;
import android.graphics.ColorSpace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import android.text.format.DateFormat;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;

    public AdapterChat (Context context, List<ModelChat> chatList, String imageUrl){
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layouts
        if(viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.rowchat_right, parent, false);
            return new MyHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.rowchat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timestamp = chatList.get(position).getTimestamp();

        //convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        holder.messageTV.setText(message);
        holder.timeTV.setText(dateTime);
        try{
            Picasso.get().load(imageUrl).into(holder.profileIv);
        } catch (Exception e){

        }

        //set seen/delivered of message
        if(position==chatList.size()-1){
            if(chatList.get(position).isSeen()){
                holder.isSeenTV.setText("Seen");
            } else{
                holder.isSeenTV.setText("Delivered");
            }
        } else{
            holder.isSeenTV.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else{
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //views
        ImageView profileIv;
        TextView messageTV, timeTV, isSeenTV;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            profileIv = itemView.findViewById(R.id.rowleft_profileIV);
            messageTV = itemView.findViewById(R.id.row_message);
            timeTV = itemView.findViewById(R.id.row_time);
            isSeenTV = itemView.findViewById(R.id.row_isSeenTv);
        }
    }
}

