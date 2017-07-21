package com.uiresource.musicplayer;

import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Created by Andrea on 10/6/2017.
 */

public class Mp3File extends MediaPlayer {

    private String Title;
    private String Artist;
    private String Albumn;
    private Uri uriAddres;
    private int ID;

    public Mp3File (String title, String artist, String albumn, int ID){
        this.Title = title;
        this.Artist = artist;
        this.Albumn = albumn;
        this.ID = ID;
        uriAddres = Uri.withAppendedPath(Config.MEDIA_STORE,Integer.toString(ID));

    }

    public String getTitle()
    {return Title;}

    public String getArtist ()
    {return Artist;}

    public Uri getUri()
    {return uriAddres;}
}
