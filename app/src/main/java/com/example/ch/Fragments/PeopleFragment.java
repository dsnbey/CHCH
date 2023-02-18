package com.example.ch.Fragments;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
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

import com.example.ch.ChatActivity;
import com.example.ch.Common.Common;
import com.example.ch.Models.UserModel;
import com.example.ch.R;
import com.example.ch.TextDrawable.ColorGenerator;
import com.example.ch.TextDrawable.TextDrawable;
import com.example.ch.ViewHolders.UserViewHolder;
import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class PeopleFragment extends Fragment {

    private PeopleViewModel mViewModel;

    static PeopleFragment instance;
    private RecyclerView recyclerViewPeople;
    FirebaseRecyclerAdapter adapter;

    public static PeopleFragment getInstance() {
        return instance == null ? new PeopleFragment() : instance;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_people, container, false);
        initView(itemView);
        loadPeople();
        return itemView;
    }

    private void loadPeople() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(Common.USER_REFERENCE);
        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions.
                Builder<UserModel>().setQuery(query, UserModel.class).build();
        adapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_people, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel model) {
                if (!adapter.getRef(position).getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    // Hide
                    ColorGenerator generator = ColorGenerator.MATERIAL;
                    int color = generator.getColor(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    TextDrawable.IBuilder builder = TextDrawable.builder()
                            .beginConfig().withBorder(4).endConfig().round();
                    TextDrawable drawable = builder.build(model.getFirstName().substring(0,1), color);
                    holder.img_avatar.setImageDrawable(drawable);
                    StringBuilder sBuilder = new StringBuilder();
                    sBuilder.append(model.getFirstName()).append(" ").append(model.getLastname());
                    holder.txt_name.setText(sBuilder.toString());
                    holder.txt_bio.setText(model.getBio());

                    // Event
                    holder.itemView.setOnClickListener(view -> {
                        Common.chatUser = model;
                        Common.chatUser.setUid(adapter.getRef(position).getKey());
                        startActivity(new Intent(getContext(), ChatActivity.class));
                    });

                }
                else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
            }
        };

        adapter.startListening();
        recyclerViewPeople.setAdapter(adapter);
    }

    private void initView(View itemView) {
        recyclerViewPeople = itemView.findViewById(R.id.recycler_people);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewPeople.setLayoutManager(layoutManager);
        recyclerViewPeople.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PeopleViewModel.class);
        // TODO: Use the ViewModel
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
}