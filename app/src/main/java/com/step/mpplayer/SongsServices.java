package com.step.mpplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import com.step.mpplayer.entities.Song;

import java.util.List;

public class SongsServices extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener {


    public interface PlayerListener{
        void onStartPlaing(Song s);
        void onStopPlaying();
    }
    PlayerListener playerListener;

    public void setPlayerListener(PlayerListener playerListener) {
        this.playerListener = playerListener;
    }

    private MediaPlayer mediaPlayer;
    private List<Song> playlist;
    private int current = 0;


    public int getCurrent() {
        return current;
    }

    void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void startPlaying(){
        mediaPlayer.reset();
        Song song = playlist.get(current);
        long sId = song.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,sId);
        try{
            mediaPlayer.setDataSource(getApplicationContext(),trackUri);
            mediaPlayer.prepareAsync();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setSong(Song s){
        current = playlist.indexOf(s);
        if(current==-1)current=0;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        current=0;
        initMediaPlayer();
    }

    public void setPlaylist(List<Song> playlist) {
        this.playlist = playlist;
    }

    public class SongsBuinder extends Binder {
        SongsServices getServices(){return SongsServices.this;}
    }

    private final SongsBuinder buinder = new SongsBuinder();


    @Override
    public IBinder onBind(Intent intent) {
        return buinder;
    }
    public void stop(){
        mediaPlayer.stop();
        if(playerListener!=null) playerListener.onStopPlaying();
    }



    @Override
    public void onCompletion(MediaPlayer mp) {
        if(current<playlist.size()-1){
            current++;
            startPlaying();
        }else if(playerListener!=null)playerListener.onStopPlaying();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        if(playerListener!=null) playerListener.onStartPlaing(playlist.get(current));

        Intent notificationIntent = new Intent(this,MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        Notification.Builder builder;
            builder=new Notification.Builder(this);

        builder.setContentIntent(pIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(playlist.get(current).getName())
                .setOngoing(true)
                .setContentTitle("MPlayer playback:")
                .setContentText(playlist.get(current).getName());
        Notification notification = builder.build();
        startForeground(321456,notification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void next(){
        current = current<playlist.size()-1?current+1:0;
        startPlaying();
    }
    public void prev(){
        current = current>0?current-1:playlist.size()-1;
        startPlaying();
    }
    public int getPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    public int getDuration(){
        return mediaPlayer.getDuration();
    }
    public void pause(){
        mediaPlayer.pause();
    }
    public void seekTo(int pos){
        mediaPlayer.seekTo(pos);
    }
    public void play(){
        mediaPlayer.start();
    }
    public boolean isPlayback(){
        return mediaPlayer.isPlaying();
    }
}
