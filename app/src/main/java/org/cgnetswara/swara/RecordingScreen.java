package org.cgnetswara.swara;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    public static final int PICK_IMAGE = 1;
    private Bitmap bitmap = null;
    ImageView photoAttach;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Declarations and instantiations
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_screen);

        recordButton=findViewById(R.id.imageButton);
        playButton=findViewById(R.id.imageButton5);
        clearButton=findViewById(R.id.imageButton4);
        acceptButton=findViewById(R.id.imageButton3);
        chronometer=findViewById(R.id.chronometer);
        photoAttach=findViewById(R.id.imageView2);

        recordButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);
    }

    public void toggleRecording(View view) {
        //first step in recording
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        if(recordState){
            //Recording Started
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setVisibility(View.VISIBLE);
            recordButton.setImageResource(R.drawable.stopbtn);
            recordState = false;
            String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File folder = new File(exstPath+"/swararecordings");
            folder.mkdirs();
            Date dt=new Date();
            path=folder+"/"+dt+".mp3";
            Log.d("path",path);
            mRecMicToMp3 = new RecMicToMp3(path, 8000);
            mRecMicToMp3.start();
        }
        else{
            //Recording stop procedure
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
        //Just reset things
        vibrateAndSetViews();
    }

    public void onPlay(View view) {
        //Re-play functionality
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

            } catch (IOException e) {//audioplayer exceptions
                e.printStackTrace();
            }
              catch(Exception e){//audioplayer exceptions
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
        //Repeated tasks on Clear, Play or Accept
        if(audioPlayer!=null) {
            audioPlayer.release();
        }
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    public void vibrateAndSetViews(){
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        recordButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        recordState=true;
        stopPlaying();
    }

    public void onAccept(View view) {
        //Resetting things first
        vibrateAndSetViews();
        //saving details in shared prefs for audio and/or photo to be mailed
        sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(path, ""+audioDuration+","+selectedImageUri);
        editor.apply();
    }

    public void addPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(bitmap != null) {
                while(bitmap.getHeight() > 1000 || bitmap.getWidth() > 1000) {
                    bitmap = halfSize(bitmap);
                }
            }
            photoAttach.setImageBitmap(bitmap);
        }
    }

    private Bitmap halfSize(Bitmap input) {
        int height = input.getHeight();
        int width = input.getWidth();
        return Bitmap.createScaledBitmap(input,  width/2, height/2, false);
    }
}
