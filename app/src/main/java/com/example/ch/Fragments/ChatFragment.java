package com.example.ch.Fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ch.Common.Common;
import com.example.ch.Models.ChatInfoModel;
import com.example.ch.Models.UserModel;
import com.example.ch.R;
import com.example.ch.TextDrawable.ColorGenerator;
import com.example.ch.TextDrawable.TextDrawable;
import com.example.ch.ViewHolders.ChatInfoHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;

import butterknife.BindView;

public class ChatFragment extends Fragment {

    private ChatViewModel mViewModel;


    static  ChatFragment instance;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM dd/MM/yyyy");
    FirebaseRecyclerAdapter adapter;

    RecyclerView recyclerViewChat;

    public static ChatFragment getInstance() {
        return instance == null ? new ChatFragment() : instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView =  inflater.inflate(R.layout.fragment_chat, container, false);
        initView(itemView);
        loadChatList();
        return itemView;
    }

    private void loadChatList() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseRecyclerOptions<ChatInfoModel> options = new FirebaseRecyclerOptions.
                Builder<ChatInfoModel>().setQuery(query, ChatInfoModel.class).build();

        adapter = new FirebaseRecyclerAdapter<ChatInfoModel, ChatInfoHolder>(options) {

            @NonNull
            @Override
            public ChatInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_chat_item, parent, false);
                return new ChatInfoHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatInfoHolder holder, int position, @NonNull ChatInfoModel model) {
                if (!adapter.getRef(position).getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    // Hide
                    ColorGenerator generator = ColorGenerator.MATERIAL;
                    int color = generator.getColor(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    TextDrawable.IBuilder builder = TextDrawable.builder()
                            .beginConfig().withBorder(4).endConfig().round();

                    String displayName = FirebaseAuth.getInstance().getCurrentUser().getUid()
                            .equals(model.getCreateId()) ? model.getFriendName() : model.getFriendName();
                    TextDrawable drawable = builder.build(displayName.substring(0,1), color);
                    holder.img_avatar.setImageDrawable(drawable);

                    holder.txt_last_message.setText(model.getLastMessage());
                    holder.txt_name.setText(displayName);
                    holder.txt_time.setText(simpleDateFormat.format(model.getLastUpdate()));

                    // Event
                    holder.itemView.setOnClickListener(view -> {
                        // TODO
                    });

                }
                else {
                    // if equal key, hide yourself
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }
        };
    }

    private void initView(View itemView) {
        recyclerViewChat = itemView.findViewById(R.id.recycler_chat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        if (adapter != null) adapter.stopListening();
        super.onStop();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        // TODO: Use the ViewModel
    }

}