package org.cgnetswara.swara;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class RecordingScreen extends AppCompatActivity {
    boolean recordState = true;
    boolean playState = true;
    ImageButton recordButton;
    ImageButton playButton;
    ImageButton clearButton;
    ImageButton acceptButton;
    Chronometer chronometer;
    RecMicToMp3 mRecMicToMp3;
    MediaPlayer audioPlayer;
    String path;
    long audioDuration;
    SharedPreferences sp;
    public static final String MyPREFERENCES = "MyPrefs" ;
    String mMainDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_screen);

        recordButton=findViewById(R.id.imageButton);
        playButton=findViewById(R.id.imageButton5);
        clearButton=findViewById(R.id.imageButton4);
        acceptButton=findViewById(R.id.imageButton3);
        chronometer=findViewById(R.id.chronometer);

        recordButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);
    }

    public void toggleRecording(View view) {//first step in recording
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        if(recordState){
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setVisibility(View.VISIBLE);
            recordButton.setImageResource(R.drawable.stopbtn);
            recordState = false;
            String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File folder = new File(exstPath+"/swararecordings");
            folder.mkdirs();
            Date dt=new Date();
            String filename=dt.toString();
            path=folder+"/"+dt+".mp3";
            Log.d("path",path);
            mRecMicToMp3 = new RecMicToMp3(path, 8000);
            mRecMicToMp3.start();
        }
        else{
            chronometer.stop();
            recordButton.setVisibility(View.INVISIBLE);
            recordButton.setImageResource(R.drawable.recordbtn);
            playButton.setVisibility(View.VISIBLE);
            playButton.setImageResource(R.drawable.play);
            playState=true;
            clearButton.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.VISIBLE);
            mRecMicToMp3.stop();
        }
    }

    public void onClear(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        recordButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        recordState=true;
        stopPlaying();
    }

    public void onPlay(View view) {
        if(playState){
            playButton.setImageResource(R.drawable.pause);
            playState=false;
            audioPlayer = new MediaPlayer();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            try {
                audioPlayer.setDataSource("file://"+path);
                audioPlayer.prepare();
                audioPlayer.start();

                audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        playButton.setImageResource(R.drawable.play);
                        playState=true;
                        stopPlaying();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
              catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            audioPlayer.stop();
            playButton.setImageResource(R.drawable.play);
            playState=true;
            stopPlaying();
        }
    }

    public void stopPlaying(){
        if(audioPlayer!=null) {
            audioPlayer.release();
        }
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    public void onAccept(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        recordButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        recordState=true;
        stopPlaying();
        //saving details in shared prefs for audio and/or photo to be mailed
        sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(path, ""+audioDuration);
        editor.apply();
        //********************************************************************************
        /*code to iterate over sp entries*/
        Map<String,?> paths = sp.getAll();

        for(Map.Entry<String,?> row : paths.entrySet()){
            Log.d("map values",row.getKey() + ": " + row.getValue().toString());
        }
        //********************************************************************************
    }

    public void addPhoto(View view) {

    }
}
