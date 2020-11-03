package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static com.example.musicplayer.MainActivity.musicFiles;

public class AlbumDetailsActivity extends AppCompatActivity {

    ImageView albumPhoto;
    RecyclerView recyclerView;
    String albumDetails;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_album_details);

        albumPhoto = findViewById(R.id.album_activity_image);
        recyclerView = findViewById(R.id.album_activity_recycler);
        albumDetails = getIntent().getStringExtra("albumDetails");
        int index = 0;
        for (int i = 0 ; i < musicFiles.size(); i++){
            if (albumDetails.equals(musicFiles.get(i).getAlbum())){
                albumSongs.add(index, musicFiles.get(i));
                index++;
            }
        }
        byte[] albumImage = getAlbumArt(albumSongs.get(0).getPath());
        if (albumImage != null){
            Glide.with(this).load(albumImage).into(albumPhoto);
        }else{
            Glide.with(this).load(R.drawable.music_placeholder).into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (albumSongs.size() > 0){
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
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
