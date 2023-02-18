package com.example.ch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ch.Common.Common;
import com.example.ch.Listener.IFirebaseLoadFailed;
import com.example.ch.Listener.ILoadTimeFromFirebaseListener;
import com.example.ch.Models.ChatInfoModel;
import com.example.ch.Models.ChatMessageModel;
import com.example.ch.TextDrawable.ColorGenerator;
import com.example.ch.TextDrawable.TextDrawable;
import com.example.ch.ViewHolders.ChatTextHolder;
import com.example.ch.ViewHolders.ChatTextReceiveHolder;
import com.example.ch.databinding.ActivityChatBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity implements ILoadTimeFromFirebaseListener, IFirebaseLoadFailed {

    private static final int MY_CAMERA_REQUEST_CODE = 7171;
    private static final int MY_RESULT_LOAD_IMAGE = 7172;
    ActivityChatBinding b;
    FirebaseDatabase database;
    DatabaseReference chatRef, offsetRef;
    ILoadTimeFromFirebaseListener listener;
    IFirebaseLoadFailed errorListener;

    FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder> adapter;
    FirebaseRecyclerOptions<ChatMessageModel> options;

    Uri fileUri;
    LinearLayoutManager layoutManager;


    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (adapter != null) {
            adapter.startListening();
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChatBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        initViews();
        loadChatContent();
    }

    private void loadChatContent() {
        String receiverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new FirebaseRecyclerAdapter<ChatMessageModel, RecyclerView.ViewHolder>(options) {

            @Override
            public int getItemViewType(int position) {
                if (adapter.getItem(position).getSenderId()
                        .equals(receiverId)) // if message is own
                    return !adapter.getItem(position).isPicture() ? 0 : 1;
                else
                    return !adapter.getItem(position).isPicture() ? 2 : 3;
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatMessageModel model) {
                if (holder instanceof ChatTextHolder) {
                    ChatTextHolder chatTextHolder = (ChatTextHolder) holder;
                    chatTextHolder.txt_chat_message.setText(model.getContent());
                    chatTextHolder.txt_time.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                    Calendar.getInstance().getTimeInMillis(), 0)
                                    .toString()
                    );
                }
                else if (holder instanceof ChatTextReceiveHolder) {
                    ChatTextReceiveHolder chatTextReceiveHolder = (ChatTextReceiveHolder) holder;
                    chatTextReceiveHolder.txt_chat_message.setText(model.getContent());
                    chatTextReceiveHolder.txt_time.setText(
                            DateUtils.getRelativeTimeSpanString(model.getTimeStamp(),
                                            Calendar.getInstance().getTimeInMillis(), 0)
                                    .toString()
                    );
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if (viewType == 0) // Text message of user's own message
                {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_message_text_own, parent, false);
                    return new ChatTextReceiveHolder(view);
                }
                else // if (viewType == 2) // Text message of friend
                {
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_message_text_friend, parent, false);
                    return new ChatTextHolder(view);
                }
            }
        };

        // Auto scroll when receive new message
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void  onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1)))
                {
                    b.recyclerChatCA.scrollToPosition(positionStart);
                }
            }
        });
        b.recyclerChatCA.setAdapter(adapter);

    }

    private void initViews() {
        listener = this;
        errorListener = this;
        database = FirebaseDatabase.getInstance();
        chatRef = database.getReference(Common.CHAT_REFERENCE);

        offsetRef = database.getReference(".info/serverTimeOffset");

        b.imgSend.setOnClickListener(view -> {
            offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long offset = snapshot.getValue(Long.class);
                    long estimatedServerTimeInMs = System.currentTimeMillis() + offset;
                    listener.onLoadOnlyTimeSuccess(estimatedServerTimeInMs);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    errorListener.onError(error.getMessage());
                }
            });
        });


        Query query = chatRef.child(Common.generateChatRoomId(
                Common.chatUser.getUid(),
                FirebaseAuth.getInstance().getCurrentUser().getUid()
        )).child(Common.CHAT_DETAIL_REFERENCE);

        options = new FirebaseRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();
        layoutManager = new LinearLayoutManager(this);
        b.recyclerChatCA.setLayoutManager(layoutManager);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(Common.chatUser.getUid());
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig().withBorder(4).endConfig().round();

        TextDrawable drawable = builder.build(Common.chatUser.getFirstName().substring(0,1), color);
        b.imgAvatarCA.setImageDrawable(drawable);
        b.txtNameCA.setText(Common.getName(Common.chatUser));

        setSupportActionBar(b.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        b.toolbar.setNavigationOnClickListener(view -> {
            finish();
        });


    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {
        ChatMessageModel chatMessageModel = new ChatMessageModel();
        chatMessageModel.setName(Common.getName(Common.currentUser));
        chatMessageModel.setContent(b.edtChat.getText().toString());
        chatMessageModel.setTimeStamp(estimateTimeInMs);
        chatMessageModel.setSenderId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        chatMessageModel.setPicture(false);
        submitChatToFirebase(chatMessageModel, chatMessageModel.isPicture(), estimateTimeInMs);
    }

    private void submitChatToFirebase(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                FirebaseAuth.getInstance().getCurrentUser().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            appendChat(chatMessageModel, isPicture, estimateTimeInMs);
                        }
                        else {
                            createChat(chatMessageModel, isPicture, estimateTimeInMs);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void appendChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        Map<String,Object> update_data = new HashMap<>();
        update_data.put("lastUpdate", estimateTimeInMs);

        // Only Text
        update_data.put("lastMessage", chatMessageModel.getContent());

        // update on user list
        database.getReference(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .updateChildren(update_data)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(unused -> {
                    // submit success for chat info
                    // copy to friend chat list
                    database.getReference(Common.CHAT_LIST_REFERENCE)
                            .child(Common.chatUser.getUid())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) // swap the sender-receiver
                            .updateChildren(update_data)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(unused1 -> {
                                // add on Chat Ref
                                chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                                                FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                        .child(Common.CHAT_DETAIL_REFERENCE)
                                        .push()
                                        .setValue(chatMessageModel)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    // Clear
                                                    b.edtChat.setText("");
                                                    b.edtChat.requestFocus();
                                                    if (adapter != null){
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                            });
                });

    }

    private void createChat(ChatMessageModel chatMessageModel, boolean isPicture, long estimateTimeInMs) {
        ChatInfoModel chatInfoModel = new ChatInfoModel();
        chatInfoModel.setCreateId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        chatInfoModel.setFriendName(Common.getName(Common.chatUser));
        chatInfoModel.setFriendId(Common.chatUser.getUid());
        chatInfoModel.setCreateName(Common.getName(Common.currentUser));

        // Only text
        chatInfoModel.setLastMessage(chatMessageModel.getContent());
        chatInfoModel.setLastUpdate(estimateTimeInMs);
        chatInfoModel.setCreateDate(estimateTimeInMs);

        // submit on Firebase
        // add on user chat list
        database.getReference(Common.CHAT_LIST_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Common.chatUser.getUid())
                .setValue(chatInfoModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(unused -> {
                    // submit success for chat info
                    // copy to friend chat list
                    database.getReference(Common.CHAT_LIST_REFERENCE)
                            .child(Common.chatUser.getUid())
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) // swap the sender-receiver
                            .setValue(chatInfoModel)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnSuccessListener(unused1 -> {
                                // add on Chat Ref
                                chatRef.child(Common.generateChatRoomId(Common.chatUser.getUid(),
                                        FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                        .child(Common.CHAT_DETAIL_REFERENCE)
                                        .push()
                                        .setValue(chatMessageModel)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    // Clear
                                                    b.edtChat.setText("");
                                                    b.edtChat.requestFocus();
                                                    if (adapter != null){
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                            });
                });

    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}