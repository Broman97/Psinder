package com.example.psinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    private ImageButton AddNewPostButton;



    String currentUserID;
    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);



        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);



        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exist", Toast.LENGTH_SHORT).show();
                    }





                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToPostActivity();

            }
        });

        DisplayAllUsersPosts();



    }

    private void DisplayAllUsersPosts()
{
    Query SortPostInDecendingOrder = PostsRef.orderByChild("counter");

    FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(SortPostInDecendingOrder, Posts.class).build();
    FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
            new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model)
                {
                    final String PostKey = getRef(position).getKey();

                    holder.username.setText(model.getFullname());
                    holder.time.setText("  " + model.getTime());
                    holder.date.setText("  " + model.getDate());
                    holder.description.setText(model.getDescription());
                    Picasso.get().load(model.getProfileimage()).into(holder.user_post_image);
                    Picasso.get().load(model.getPostimage()).into(holder.postImage);

                    holder.setLikeButtonStatus(PostKey);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                            clickPostIntent.putExtra("PostKey", PostKey);
                            startActivity(clickPostIntent);

                        }
                    });

                    holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                            commentsIntent.putExtra("PostKey", PostKey);
                            startActivity(commentsIntent);


                        }
                    });

                    holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {

                            LikeChecker = true;

                            LikesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    if(LikeChecker.equals(true))
                                    {
                                        if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                        {
                                            LikesRef.child(PostKey).child(currentUserID).removeValue();
                                            LikeChecker = false;

                                        }
                                        else
                                        {
                                            LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                            LikeChecker = false;


                                        }
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                    });

                }

                @NonNull
                @Override
                public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                    PostsViewHolder viewHolder = new PostsViewHolder(view);
                    return viewHolder;
                }
            };
    postList.setAdapter(firebaseRecyclerAdapter);
    firebaseRecyclerAdapter.startListening();





}

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,date,time,description;
        CircleImageView user_post_image;
        ImageView postImage;

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;






        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);



            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            description = itemView.findViewById(R.id.post_description);
            postImage = itemView.findViewById(R.id.post_image);
            user_post_image = itemView.findViewById(R.id.post_profile_image);

            LikePostButton = (ImageButton) itemView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) itemView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();



        }

        public void setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }
                    else
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }





    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }


    private void CheckUserExistence()
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }


    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;

            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;

            case R.id.nav_messages:
                SendUserToFriendsActivity();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;

             case R.id.nav_Logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }


    private void SendUserToFriendsActivity()
    {
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToSettingsActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToFindFriendsActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToProfileActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(loginIntent);
    }




    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }
}

