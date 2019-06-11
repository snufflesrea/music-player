package com.uiresource.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.SettingInjectorService;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;

import static android.R.attr.duration;
import static android.R.attr.format;
import static android.R.attr.progressBarStyle;
import static android.R.attr.tag;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.media.CamcorderProfile.get;
import static com.uiresource.musicplayer.R.id.tv_song;

public class MainActivity extends AppCompatActivity {

    private String TAG = "#"+ getClass().getSimpleName();

    private int CurrentTrackIndex = 0;
    private List<Mp3File> Playlist;
    private TrackAdapter mAdapter;

    private ImageButton mPlayerControl;
    private ImageButton forwardButton;
    private ImageButton backwardButton;
    private ImageButton repeatButton;
    private ImageButton shufflesButton;

    private MediaPlayer mediaPlayer;
    private ListView TrackList;
    private TextView TitleTv;
    private TextView ArtistTv;
    private TextView duration;
    private TextView currentTime;
    private SeekBar progressBar;

    private Handler myHandler = new Handler();
    private Boolean isRepeat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TrackList = (ListView) findViewById(R.id.playlist_view);

        Playlist = getMp3List(this);
        mAdapter = new TrackAdapter(this, Playlist);
        TrackList.setAdapter(mAdapter);

        TitleTv = (TextView) findViewById(tv_song);
        ArtistTv = (TextView) findViewById(R.id.tv_artis);
        currentTime = (TextView) findViewById(R.id.tv_current_time);
        progressBar = (SeekBar) findViewById(R.id.progress_bar);
        duration = (TextView) findViewById(R.id.tv_full_time);
        forwardButton = (ImageButton)findViewById(R.id.button_forward);
        backwardButton = (ImageButton) findViewById(R.id.button_backward);

        mPlayerControl = (ImageButton) findViewById(R.id.ic_play);
        mPlayerControl.setImageResource(R.drawable.ic_play_button);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        //Engage the listView so when it is clicked the Media Player is prepared
        TrackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mPlayerControl.setImageResource(R.drawable.ic_play_button);
                }
                mediaPlayer.setLooping(false);
                isRepeat = false;
                mediaPlayer.reset();
                PlayMediaPlayer(MainActivity.this, position);
                CurrentTrackIndex = position;
            }
        });

        //set onCompletionListener to Media Player. After the song ended, the next track should be play continously
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isRepeat == false && mediaPlayer != null){
                    if(CurrentTrackIndex < Playlist.size()-1){
                        mediaPlayer.reset();
                        CurrentTrackIndex = CurrentTrackIndex + 1;
                        PlayMediaPlayer(MainActivity.this, CurrentTrackIndex);
                        mediaPlayer.start();
                    }
                    else{
                        mediaPlayer.reset();
                        mPlayerControl.setImageResource(R.drawable.ic_play_button);
                    }
                }
            }
        });

        //Set click for Play and Pause button
        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                {mediaPlayer.pause();
                 SetSeekBarDuration(updateSeekBar);
                mPlayerControl.setImageResource(R.drawable.ic_play_button);}
                else{
                    SetSeekBarDuration(updateSeekBar);
                    mediaPlayer.start();
                    mPlayerControl.setImageResource(R.drawable.ic_pause_button);
                }
            }
        });

        //Set click for Forward Button
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentTrackIndex < Playlist.size()-1 ){
                mediaPlayer.stop();
                mPlayerControl.setImageResource(R.drawable.ic_play_button);
                mediaPlayer.reset();
                CurrentTrackIndex = CurrentTrackIndex +1;
                PlayMediaPlayer(getApplicationContext(),CurrentTrackIndex);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.last_track, Toast.LENGTH_LONG).show();
                }
            }
        });

        //Set click for Backward Button
        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentTrackIndex > 0){
                    mediaPlayer.stop();
                    mPlayerControl.setImageResource(R.drawable.ic_play_button);
                    mediaPlayer.reset();
                    CurrentTrackIndex = CurrentTrackIndex - 1;
                    PlayMediaPlayer(getApplicationContext(), CurrentTrackIndex);
                }else {
                    Toast.makeText(getApplicationContext(), R.string.last_track, Toast.LENGTH_LONG).show();
                }
            }
        });

        //set SeekBar to be draggable
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    public void repeat(View v){
    if (mediaPlayer != null)
    {      isRepeat = true;
        mediaPlayer.setLooping(true);}
    }

    public void shuffles(View v){
        Random random = new Random();
        int integer =0;
            do{
       integer = random.nextInt(Playlist.size()) + 0;
        } while (integer >= Playlist.size() || integer < 0);
        mediaPlayer.stop();
        mediaPlayer.reset();
        PlayMediaPlayer(MainActivity.this, integer);
        mediaPlayer.start();
    }


    //Method to get all MP3 available in Content Provider and return it in list
    private List<Mp3File> getMp3List(Context context) {
        List<Mp3File> PlayList = new ArrayList<Mp3File>();

        Cursor cur = context.getContentResolver().query(
               Config.MEDIA_STORE, null, null, null, null
        );

        if (cur!= null)
        {   while(cur.moveToNext()){
            int titleColumn = cur.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int IDColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumnColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            String title = cur.getString(titleColumn);
            Log.i(TAG, title);
            String artist = cur.getString(artistColumn);
            String albumn = cur. getString(albumnColumn);
            int ID = cur.getInt(IDColumn);

            Mp3File mp3 = new Mp3File(title, artist, albumn, ID);
            PlayList.add(mp3);}
           }
        else {
            Toast.makeText(context, R.string.no_mp3_files, Toast.LENGTH_LONG).show();
            cur.close();
        }
        return PlayList;
    }

    //Method to prepare and play selected mp3 from the List<Mp3File> Playlist
    public void PlayMediaPlayer(Context context, int ID){
        Mp3File mp3  = Playlist.get(ID);
        try {

            mediaPlayer.setDataSource(getApplicationContext(), mp3.getUri());
            mediaPlayer.prepare();
            TitleTv.setText(mp3.getTitle());
            ArtistTv.setText(mp3.getArtist());
            SetSeekBarDuration(updateSeekBar);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e)
        {e.printStackTrace();}
    }

    //This method to se the duration on progress bar
    private void SetSeekBarDuration(Runnable updateSeekBar){
        progressBar.setMax(mediaPlayer.getDuration());
        progressBar.setProgress(mediaPlayer.getCurrentPosition());
        int timeDuration = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition();
        currentTime.setText(TimeFormat(timeDuration));
        duration.setText(TimeFormat(mediaPlayer.getCurrentPosition()));
        myHandler.postDelayed(updateSeekBar,1000);
    }

    //This method change duration (int) from milisecond to MM:SS
    private String TimeFormat (int timing){
        if (timing > 0){
    long milis = ((long) timing);
        String format = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(milis),
            TimeUnit.MILLISECONDS.toSeconds(milis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milis))
            );
        return format;} else {
            return  "00:00";
        }
    }

    //Engage seekBar with MediaPlayer
    final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying())
            {progressBar.setProgress(mediaPlayer.getCurrentPosition());
                int timeDuration = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition();
                duration.setText(TimeFormat(mediaPlayer.getCurrentPosition()));
                currentTime.setText(TimeFormat(timeDuration));}
            myHandler.postDelayed(this, 1000);  }
    };

}
