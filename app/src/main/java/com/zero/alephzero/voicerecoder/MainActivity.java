package com.zero.alephzero.voicerecoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //VARIABLES
    Button play,startRecord,next;
    TextView textView;
    ArrayList<String> texts = new ArrayList<String>(); // texts
    private int textIndex;

    String pathSv = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Chronometer chronometer;

    final int REQUEST_PERMISSION_CODE = 1000;

    private boolean onRecord = false;
    private boolean onPlay = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buttons
        play = findViewById(R.id.PlayButton);
        startRecord = findViewById(R.id.StartRecordButton);
        next = findViewById(R.id.next);
        textView = findViewById(R.id.text);
        chronometer =findViewById(R.id.chronometer);

        // create index file and read
        SetLevel.createFile(getApplicationContext());
        textIndex = Integer.parseInt( SetLevel.readFile(getApplicationContext()));

        // read texts
        read("texts.txt");
        // initial text
        if(textIndex >= texts.size())
            textIndex = texts.size() -1;

        textView.setText(texts.get(textIndex));
        //first permission
        if(!checkPermissionFromDevice())
            requestPermissions();

        // next text
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!onRecord && !onPlay ){

                    SetLevel.writeFile(textIndex,getApplicationContext());
                    if(textIndex < texts.size()){

                        textView.setText(texts.get(textIndex));
                        textIndex++;
                    }

                }
                if(textIndex == texts.size()){

                    Intent intent = new Intent(MainActivity.this,Upload.class);
                    startActivity(intent);

                }
            }
        });
        // start record and stop
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if(!onRecord) {

                if( checkPermissionFromDevice()){

                    // change status to onrecord
                    onRecord = true;

                    // set path
                    pathSv = SetLevel.getFilename() + "text" + String.valueOf(textIndex) + ".3gp";
                    // create audio parameter
                    setupMediaRecorder();

                    // start recorder
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    // change button text
                    startRecord.setText("BİTİR");
                    // close play button
                    play.setEnabled(false);
                    // start counter
                    startChronometer();

                    Toast.makeText(MainActivity.this,"Recording...",Toast.LENGTH_SHORT).show();
                }else
                    requestPermissions();

            }else {

                // stop record and counter
                stopChronometer();
                onRecord = false;
                // change button name
                startRecord.setText("BAŞLA");
                mediaRecorder.stop();

                play.setEnabled(true);
            }
            }

        });

        // play last record and stop
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!onPlay){

                    onPlay = true;
                    play.setText("DURDUR");
                    startRecord.setEnabled(false);

                    mediaPlayer = new MediaPlayer();
                    try {
                        pathSv = SetLevel.getFilename() + "text" + String.valueOf(textIndex) + ".3gp";
                        mediaPlayer.setDataSource(pathSv);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    Toast.makeText(MainActivity.this,"Playing...",Toast.LENGTH_SHORT).show();
                }else{

                    onPlay = false;
                    play.setText("OYNAT");

                    startRecord.setEnabled(true);

                    if(mediaPlayer != null){

                        mediaPlayer.stop();
                        mediaPlayer.release();
                        setupMediaRecorder();
                    }
                }
            }
        });
    }
    private void setupMediaRecorder(){

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSv);
    }
    private void requestPermissions() {

        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    private boolean checkPermissionFromDevice(){

        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);

        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
    public void startChronometer() {

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    public void stopChronometer() {
       chronometer.stop();
    }
    private void read(String filename){
        String text = "";

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader( getAssets().open(filename)));
            while ((text = in.readLine()) != null) {
                texts.add(text);
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
