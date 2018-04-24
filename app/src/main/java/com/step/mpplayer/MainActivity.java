package com.step.mpplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.step.mpplayer.adapters.SongsAdapter;
import com.step.mpplayer.entities.Song;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {


    List<Song> songList = new ArrayList<>();
    RecyclerView mSongsView;
    SongsAdapter mSongsAdapter;
    Button mStopButton;
    TextView mPlayingTextView;
    SongsServices songsServices;
    boolean songServicesBuinded = false;
    SeekBar songProgressBar;
    Button nextBtn;
    Button prevBtn;

    void loadSongs(){
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(uri,null,null,null,null);

        if(cursor==null || !cursor.moveToFirst()) return;
        songList.clear();
        do{
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String name  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            songList.add(new Song(id,name,artist));
        }while (cursor.moveToNext());
        cursor.close();
    }
    void initRecyclerSongView(){
        mSongsView = findViewById(R.id.song_list);
        mSongsAdapter = new SongsAdapter();
        mSongsView.setAdapter(mSongsAdapter);
        mSongsView.setLayoutManager(new LinearLayoutManager(this));
        mSongsView.setHasFixedSize(true);
        mSongsAdapter.setSongList(songList);

        mSongsAdapter.setOnClickListener(s->{
            songsServices.setSong(s);
            songsServices.startPlaying();
            //Toast.makeText(this,s.getName(),Toast.LENGTH_LONG).show();
        });
    }
    void initPlayerControls(){
        songProgressBar = findViewById(R.id.play_progress);
        mStopButton = findViewById(R.id.song_stop);
        nextBtn = findViewById(R.id.song_next);
        prevBtn = findViewById(R.id.song_prev);

        mStopButton.setOnClickListener(v->songsServices.stop());
        songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekTo(songProgressBar.getProgress());
            }
        });
        nextBtn.setOnClickListener(v->songsServices.next());
        prevBtn.setOnClickListener(v->songsServices.prev());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayingTextView = findViewById(R.id.song_playing);
        loadSongs();
        initRecyclerSongView();
        initPlayerControls();
    }

    private Runnable onProgressPlay;
    {
        onProgressPlay=() -> {
            songProgressBar.setProgress(getCurrentPosition());
            if (isPlaying()) {
                songProgressBar.postDelayed(onProgressPlay, 250);
            }
        };
    }

    void onStartPlayback(Song s){
        mPlayingTextView.setText(s.getName());
        songProgressBar.setMax(getDuration());
        songProgressBar.setProgress(0);
        mSongsAdapter.setCurrent(songsServices.getCurrent());
        songProgressBar.postDelayed(onProgressPlay, 250);
    }



    private ServiceConnection songServicesConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongsServices.SongsBuinder binder = (SongsServices.SongsBuinder)service;
            songsServices = binder.getServices();
            songsServices.setPlaylist(songList);
            songServicesBuinded = true;
            songsServices.setPlayerListener(new SongsServices.PlayerListener() {
                @Override
                public void onStartPlaing(Song s) {onStartPlayback(s);}

                @Override
                public void onStopPlaying() {
                    mPlayingTextView.setText("--STOPED--");
                    mSongsAdapter.resetCurrent();
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            songsServices.setPlayerListener(null);
            songServicesBuinded=false;
            songsServices=null;
        }

    };


    Intent songsIntent;
    @Override
    protected void onStart() {
        super.onStart();
        if(songsIntent==null){
            songsIntent = new Intent(this,SongsServices.class);
            bindService(songsIntent,songServicesConnection, Context.BIND_AUTO_CREATE);
            startService(songsIntent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(songsIntent);
        songsServices = null;
        songServicesBuinded=false;
        super.onDestroy();
    }

    @Override
    public void start() {
        songsServices.play();
    }

    @Override
    public void pause() {
        songsServices.pause();
    }

    @Override
    public int getDuration() {
        return songsServices!=null && songServicesBuinded && songsServices.isPlayback() ? songsServices.getDuration() : 0;
    }

    @Override
    public int getCurrentPosition() {
        return songsServices!=null && songServicesBuinded && songsServices.isPlayback() ? songsServices.getPosition() : 0;
    }

    @Override
    public void seekTo(int pos) {
        songsServices.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return songsServices!=null && songServicesBuinded && songsServices.isPlayback();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
