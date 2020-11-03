package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.MyAlbumViewHolder> {
    Context context;
    static ArrayList<MusicFiles> albumFiles;
    View view;
    public AlbumDetailsAdapter(Context context, ArrayList<MusicFiles> albumFiles) {
        this.context = context;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.songs_item, parent, false);
        return new MyAlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAlbumViewHolder holder, final int position) {
        holder.album_name.setText(albumFiles.get(position).getTitle());

        byte[] image = getAlbumArt(albumFiles.get(position).getPath());
        if (image != null){
            Glide.with(context).load(image).into(holder.album_image);
        }else {
            Glide.with(context).load(R.drawable.music_placeholder).into(holder.album_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender", "albumD etails");
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyAlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name;
        public MyAlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.song_item_image);
            album_name = itemView.findViewById(R.id.songs_item_text);

        }
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
