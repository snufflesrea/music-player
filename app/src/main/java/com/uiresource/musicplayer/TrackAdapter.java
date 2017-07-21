package com.uiresource.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by Andrea on 24/6/2017.
 */

public class TrackAdapter extends BaseAdapter {

    private Context mContext;
    private List<Mp3File> TrackList;

    public TrackAdapter(Context context, List<Mp3File> playlist){
        mContext = context;
        TrackList = playlist;
    }

    @Override
    public int getCount() {
        return TrackList.size();
    }

    @Override
    public Mp3File getItem(int position) {
        return TrackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        Mp3File track = getItem(position);

        ViewHolder holder;

        if (convertView == null){
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_playlist, parentView, false);
            holder = new ViewHolder();
            holder.titleTextView = (TextView) convertView.findViewById(R.id.item_playlist_title);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.titleTextView.setText(track.getTitle());
        return convertView;
    }

    static class ViewHolder
    {TextView titleTextView;}

}
