package com.example.psinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity
{
    private ImageButton PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;

    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private String Post_Key, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");


        CommentsList = (RecyclerView) findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);


        CommentInputText = (EditText) findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton) findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(userName);

                            CommentInputText.setText("");
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });


    }

    // powyzej wszystko ok nie ruszac


    @Override
    protected void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(PostsRef, Comments.class).build();
        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                holder.username.setText(model.getUsername());
                holder.comment.setText(model.getComment());
                holder.date.setText(model.getDate());
                holder.time.setText(model.getTime());


            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                CommentsViewHolder viewHolder = new CommentsViewHolder(view);
                return viewHolder;
            }
        };

        CommentsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView comment, date, time, username;


        public CommentsViewHolder(@NonNull View itemView) {


            super(itemView);


            username = itemView.findViewById(R.id.comment_username);
            comment = itemView.findViewById(R.id.comment_text);
            date = itemView.findViewById(R.id.comment_date);
            time = itemView.findViewById(R.id.comment_time);


        }
    }


    //ponizej wszystko ok nie ruszac
    private void ValidateComment(String userName)
    {
        String commentText = CommentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please write text", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", current_user_id);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);

            PostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this, "You have commented successfulluy", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(CommentsActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


        }
    }
}
