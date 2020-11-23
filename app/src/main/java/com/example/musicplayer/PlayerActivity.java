package com.example.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.musicplayer.AlbumDetailsAdapter.albumFiles;
import static com.example.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.musicplayer.MainActivity.musicFiles;
import static com.example.musicplayer.MusicAdapter.mFiles;

public class PlayerActivity extends AppCompatActivity implements ServiceConnection, ActionPlaying {

    TextView song_name, song_artist, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playBtn;
    SeekBar seekBar;

    static ArrayList<MusicFiles> allSongs = new ArrayList<>();
    int position = -1;
    boolean shuffleBoolean = false, repeatBoolean = false;

    static Uri uri;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    MusicService musicService;


    public static final String TAG = PlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        initViews();
        getIntentMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int currentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    duration_played.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean){
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle__icon);
                    Toast.makeText(getApplicationContext(), "Shuffle OFF", Toast.LENGTH_SHORT).show();
                }else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on_icon);
                    Toast.makeText(getApplicationContext(), "Shuffle ON", Toast.LENGTH_SHORT).show();
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean){
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_icon);
                    Toast.makeText(getApplicationContext(), "Repeat OFF", Toast.LENGTH_SHORT).show();
                }else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on_icon);
                    Toast.makeText(getApplicationContext(), "Repeat ON", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setFullScreen(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    private void initViews() {
        song_name = findViewById(R.id.song_name);
        song_artist = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.start_timer);
        duration_total = findViewById(R.id.end_timer);
        cover_art = findViewById(R.id.music_logo);
        nextBtn = findViewById(R.id.next);
        prevBtn = findViewById(R.id.previous);
        shuffleBtn = findViewById(R.id.shuffle);
        repeatBtn = findViewById(R.id.repeat);
        backBtn = findViewById(R.id.back_button);
        playBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String albumSongs = getIntent().getStringExtra("sender");
        if(albumSongs != null && albumSongs.equals("albumDetails")){
            allSongs = albumFiles;
        }else {
            allSongs = mFiles;
        }
        if (allSongs != null) {
            playBtn.setImageResource(R.drawable.ic_pause_icon);
            uri = Uri.parse(allSongs.get(position).getPath());
        }

        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("position", position);
        startService(intent);

    }

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int totalDuration = Integer.parseInt(allSongs.get(position).getDuration());
        duration_total.setText(formattedTime(totalDuration / 1000));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            imageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener(){
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null){
                        ImageView gradient = findViewById(R.id.ImageView_logo_gradient);
                        RelativeLayout container = findViewById(R.id.music_logo_container);
                        gradient.setBackgroundResource(R.drawable.music_logo_gradient);
                        container.setBackgroundResource(R.drawable.complete_black);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),  0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),swatch.getRgb()});
                        container.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        song_artist.setTextColor(Color.DKGRAY);
                    }else{
                        ImageView gradient = findViewById(R.id.ImageView_logo_gradient);
                        RelativeLayout container = findViewById(R.id.music_logo_container);
                        gradient.setBackgroundResource(R.drawable.music_logo_gradient);
                        container.setBackgroundResource(R.drawable.complete_black);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        container.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        song_artist.setTextColor(Color.DKGRAY);
                    }
                }
            });
        } else {
            Glide.with(this).load(R.drawable.music_item_placeholder).into(cover_art);
            ImageView gradient = findViewById(R.id.ImageView_logo_gradient);
            RelativeLayout container = findViewById(R.id.music_logo_container);
            gradient.setBackgroundResource(R.drawable.music_logo_gradient);
            container.setBackgroundResource(R.drawable.complete_black);
            song_name.setTextColor(Color.WHITE);
            song_artist.setTextColor(Color.DKGRAY);
        }

    }

    private String formattedTime(int currentPosition) {
        String totalOut = "";
        String totalNew = "";
        String minutes = String.valueOf(currentPosition / 60);
        String seconds = String.valueOf(currentPosition % 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        prevThreadBtn();
        nextThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(this);
        super.onPause();
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            musicService.showNotification(R.drawable.notification_play_icon);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
        } else {
            playBtn.setImageResource(R.drawable.ic_pause_icon);
            musicService.showNotification(R.drawable.notification_pause_icon);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
        }
        runUiThread();
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if (shuffleBoolean && !repeatBoolean){
            position = getRandom(allSongs.size());
        }else if(!shuffleBoolean && !repeatBoolean){
            position = (position - 1) < 0 ? (allSongs.size() - 1) : (position - 1);
        }
        runPlayer(position);
        musicService.onCompleted();
        if (musicService.isPlaying()) {
            playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            musicService.showNotification(R.drawable.notification_play_icon);
            musicService.start();
        } else {
            playBtn.setImageResource(R.drawable.ic_pause_icon);
            musicService.showNotification(R.drawable.notification_pause_icon);
            musicService.start();
        }
    }

    private int getRandom(int size) {
        Random random = new Random();
        return  random.nextInt(size + 1);
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (shuffleBoolean && !repeatBoolean){
            position = getRandom(allSongs.size() - 1);
        }else if(!shuffleBoolean && !repeatBoolean){
            position = (position + 1) % allSongs.size();
        }
        runPlayer(position);
        musicService.onCompleted();
        if (musicService.isPlaying()) {
            musicService.showNotification(R.drawable.notification_play_icon);
            playBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            musicService.start();
        } else {
            musicService.showNotification(R.drawable.notification_pause_icon);
            playBtn.setImageResource(R.drawable.ic_pause_icon);
            musicService.start();
        }
    }

    public void runPlayer(int mPosition) {
        musicService.stop();
        musicService.release();
        uri = Uri.parse(allSongs.get(mPosition).getPath());
        musicService.createMediaPlayer(position);
        metaData(uri);
        song_name.setText(allSongs.get(mPosition).getTitle());
        song_artist.setText(allSongs.get(mPosition).getArtist());
        seekBar.setMax(musicService.getDuration() / 1000);
        runUiThread();
    }

    public void runUiThread() {
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int currentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }

        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBacks(this);
        Toast.makeText(this, "connection binded", Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(allSongs.get(position).getTitle());
        song_artist.setText(allSongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.notification_pause_icon);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    public void imageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }


}
