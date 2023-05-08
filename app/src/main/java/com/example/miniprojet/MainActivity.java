package com.example.miniprojet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private FirebaseAuth fAuth;
    private StorageReference mStorageRef;
    private List<String> mImageUrls;
    private View log,dec;
    private MyAdapter myAdapter;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fAuth = fAuth.getInstance();
        // Set up the login button
        log = findViewById(R.id.imageView);
        dec = findViewById(R.id.imageView2);
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { if (fAuth.getCurrentUser() !=null) {
                    fAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    Toast.makeText(getApplicationContext(), "Login out successful.", Toast.LENGTH_SHORT).show();


            }

            }
        });

        // Set up the RecyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        // Set up Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Image");

        // Set up the adapter and attach it to the RecyclerView
        mImageUrls = new ArrayList<>();
        myAdapter = new MyAdapter(mImageUrls);
        mRecyclerView.setAdapter(myAdapter);

        // Load the image URLs from Firebase Storage
        mStorageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mImageUrls.add(uri.toString());
                            myAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    // Adapter for the RecyclerView
    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private FirebaseAuth fAuth;
        private List<String> mImageUrls;

        public MyAdapter(List<String> imageUrls) {
            mImageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list, parent, false);
            fAuth = fAuth.getInstance();
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String imageUrl = mImageUrls.get(position);

            // Charger l'image à partir de l'URL de téléchargement avec Glide
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .into(holder.mImageView);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(fAuth.getCurrentUser() !=null){
                        Context context = v.getContext();
                        Intent intent = new Intent(context, Recette.class);
                        context.startActivity(intent);

                    }else{

                        Context context = v.getContext();
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                    }



                }
            });

        }

        @Override
        public int getItemCount() {
            return mImageUrls.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView mImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.imageView);
                mImageView.setAdjustViewBounds(true);
            }
        }
    }
}

