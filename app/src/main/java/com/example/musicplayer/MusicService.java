package com.example.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import static com.example.musicplayer.PlayerActivity.allSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener{
    public static final String TAG = "MAGESH";
    IBinder iBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
        musicFiles = allSongs;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: method");
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("position", -1);
        String actionName = intent.getStringExtra("actionName");

        if (myPosition != -1){
            playMusic(myPosition);
        }
        if (actionName != null){
            switch (actionName){
                case "play":
                    if (actionPlaying != null){
                        actionPlaying.playPauseBtnClicked();
                    }
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    break;
                case "next":
                    if (actionPlaying != null){
                        actionPlaying.nextBtnClicked();
                    }
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    break;
                case "previous":
                    if (actionPlaying != null){
                        actionPlaying.prevBtnClicked();
                    }
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMusic(int startPosition) {
        musicFiles = allSongs;
        position = startPosition;
        if (mediaPlayer != null){
            mediaPlayer.stop();;
            mediaPlayer.release();
            if (musicFiles != null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }

        }else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }



    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }
    void start(){
        mediaPlayer.start();
    }
    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    void pause(){
        mediaPlayer.pause();
    }
    void stop(){
        mediaPlayer.stop();
    }
    void release(){
        mediaPlayer.release();
    }
    int getDuration(){
        return mediaPlayer.getDuration();
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    void createMediaPlayer(int position){
        uri = Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
    }
    void onCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null){
            actionPlaying.nextBtnClicked();
        }
        createMediaPlayer(position + 1);
        mediaPlayer.start();
        onCompleted();
    }
    public void setCallBacks(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }
}
