package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static com.example.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.musicplayer.PlayerActivity.allSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener{
    public static final String TAG = "MAGESH";
    IBinder iBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "MyAudio");
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
            mediaPlayer.stop();
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
    void createMediaPlayer(int positionInner){
        position = positionInner;
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
            if (mediaPlayer != null){
                createMediaPlayer(position);
                mediaPlayer.start();
                onCompleted();
            }
        }

    }
    public void setCallBacks(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    public void showNotification(int playPauseBtn){
        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending  = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending  = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending  = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        byte[] picture = null;
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null){
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music_item_placeholder);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_previous_icon, "previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_next_icon, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
        //startForeground(0, notification);
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
