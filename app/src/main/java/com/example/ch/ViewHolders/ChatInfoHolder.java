package com.example.ch.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ch.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ChatInfoHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.img_avatarC) public ImageView img_avatar;
    @BindView(R.id.txt_nameC) public TextView txt_name;
    @BindView(R.id.txt_last_messageC) public TextView txt_last_message;
    @BindView(R.id.txt_timeC) public TextView txt_time;

    Unbinder unbinder;


    public ChatInfoHolder(@NonNull View itemView) {
        super(itemView);
        unbinder = ButterKnife.bind(this, itemView);
    }
}
