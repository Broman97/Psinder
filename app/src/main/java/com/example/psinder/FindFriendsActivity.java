package com.example.psinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;
    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton) findViewById(R.id.search_people_friends_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);


        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchBoxInput = SearchInputText.getText().toString();


                SearchPeopleAndFriends(searchBoxInput);

            }
        });

    }
    // ze to ponizej dziala to jest cud

    private void SearchPeopleAndFriends(String searchBoxInput)
    {
        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>().
                setQuery(allUsersDatabaseRef.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff"), FindFriends.class).build();
        FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsActivity.FindFriendViewHolder holder, final int position, @NonNull FindFriends model) {
                final String PostKey = getRef(position).getKey();
                holder.username.setText(model.getFullname());
                holder.status.setText(model.getStatus());
                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);

                    }
                });
                // to gowno usunac i dziala


                // o to tot to
            }

            @NonNull
            @Override
            public FindFriendsActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);

                FindFriendsActivity.FindFriendViewHolder viewHolder = new FindFriendsActivity.FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        SearchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    public class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        TextView username, status;
        CircleImageView profileimage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.all_users_profile_name);
            status = itemView.findViewById(R.id.all_users_status);
            profileimage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }

}