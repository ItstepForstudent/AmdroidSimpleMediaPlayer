package com.step.mpplayer.adapters;

import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.step.mpplayer.R;
import com.step.mpplayer.entities.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mamedov on 13.04.2018.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder>{
    public interface OnClickListener{
        void onClick(Song s);
    }
    int current =-1;

    public void setCurrent(int current) {
        this.current = current;
        this.notifyDataSetChanged();
    }
    public void resetCurrent(){
        setCurrent(-1);
    }

    OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
        notifyDataSetChanged();
    }

    List<Song> songList = new ArrayList<>();
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.song_item,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(songList.get(position),position==current);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView mNameText;
        TextView mArtistText;


        public ViewHolder(View itemView) {
            super(itemView);
            mNameText = itemView.findViewById(R.id.song_name);
            mArtistText = itemView.findViewById(R.id.song_artist);
        }
        public void bind(Song s,boolean cur){
            itemView.setOnClickListener(v->{
                if(onClickListener!=null) onClickListener.onClick(s);
            });
            mNameText.setText(s.getName());
            mArtistText.setText(s.getArtist());
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(),cur? R.color.colorActiveSong:R.color.colorPrimary));
        }

    }
}
