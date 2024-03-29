package com.zero.alephzero.voicerecoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //VARIABLES
    Button play,startRecord,next,sil;
    TextView textView;
    ArrayList<String> texts = new ArrayList<String>(); // texts
    private int textIndex,current;
    private int startIndex,endIndex;

    String[] introText = {"     Merhaba, uygulamamıza vakit ayırdığınız için çok teşekkür ederiz. Bu programda, 2-7 cümle uzunluğunda 30 kısa metni okuyup, kaydetmenizi rica ediyoruz.     \nOkumaya başlamadan önce kayıt tuşuna basın ve bitince yine aynı tuşa basarak kaydı bitirin. Devam etmek için ve okurken bir sonraki parçaya geçmek için sıradaki tuşuna basın." ,
            "     Eğer okuduğunuz parçayı dinlemek isterseniz oynat tuşuna basın. Oynatma işlemi bitince, aynı tuşa, bitir tuşuna bastıktan sonra bir sonraki parçayı okumaya başlayabilirsiniz.     \nYanlış okuduysanız veya iyi olmadığını düşünüyorsanız, sil tuşu ile kaydı silip aynı metni tekrar kaydedebilirsiniz.Parçalar bitince okuduklarınızı otomatik olarak bize göndereceğiniz bir ekran göreceksiniz. Şimdi okumaya başlayabilirsiniz.",
            "     Okuma kısmı sona erdi. İsminizi ve soyadınızı yazıp, gönder tuşuna bastıktan sonra gönderme işlemi otomatik olarak başlayacaktır. Bu işlem yaklaşık olarak 5 dakika kadar sürecektir. Lütfen yükleme yapmak için SIRADAKİ tuşuna basın."};
    String pathSv = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Chronometer chronometer;

    final int REQUEST_PERMISSION_CODE = 1000;

    private boolean onRecord = false;
    private boolean onPlay = false;
    private boolean intro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //first permission
        if(!checkPermissionFromDevice())
            requestPermissions();
        //Buttons
        play = findViewById(R.id.PlayButton);
        startRecord = findViewById(R.id.StartRecordButton);
        next = findViewById(R.id.next);
        textView = findViewById(R.id.text);
        chronometer = findViewById(R.id.chronometer);
        sil = findViewById(R.id.Silme);

        startRecord.setBackgroundResource(R.drawable.record3);
        play.setBackgroundResource(R.drawable.play3);
        sil.setBackgroundResource(R.drawable.delete2);

        // create index file and read
        SetLevel.createFile(getApplicationContext());
        startIndex = Integer.parseInt(SetLevel.readFile(getApplicationContext(), "start"));
        current = Integer.parseInt(SetLevel.readFile(getApplicationContext(), "level"));
        textIndex = current + startIndex;
        endIndex = startIndex + 30;


        // read texts
        read("texts.txt");
        // initial text
        setTitle("  Ses Kaydına Başlamadan Önce");

        if (current == 1000) {
            intro = true;
            textView.setText(introText[0]);
        } else if (current == 1001){
            textView.setText(introText[1]);
            intro = true;
        }else if (current == 2002) {
            setTitle("  Dosyaları Yüklemeden Önce");
            textView.setText(introText[2]);

        }else{
            setTitle("  Okunan Yazı No: " + String.valueOf(current));
            textView.setText(texts.get(textIndex));

        }



        sil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFile();
                Toast.makeText(MainActivity.this,"Dosya silindi.",Toast.LENGTH_SHORT).show();
            }
        });
        // next text
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current == 2002){
                    setTitle("  Dosyaları Yüklemeden Önce");
                    Intent intent = new Intent(MainActivity.this,Upload.class);
                    startActivity(intent);
                }
                if(textIndex == endIndex ){//reading is finihed.
                    current = 2002;
                    SetLevel.writeFile(2002  ,getApplicationContext(),"level");
                    setTitle("  Dosyaları Yüklemeden Önce");
                    textView.setText(introText[2]);
                }
                if(!onRecord && !onPlay && !intro &&  ( current > 2002 || current < 1000 ) ){

                    SetLevel.writeFile(current  ,getApplicationContext(),"level");
                    File file = new File(pathSv); // check to record is exist
                    if(file.exists())
                        SetLevel.writeFile(getApplicationContext(),texts.get(textIndex)); //record exits , save content of the record
                    /*if ( current == 2002)
                        setTitle("  Dosyaları Yüklemeden Önce");
                    else*/
                        setTitle("  Okunan Yazı No: " + String.valueOf(current)) ;
                    if(textIndex < endIndex){

                        textView.setText(texts.get(textIndex));
                        textIndex++;
                        current++;
                    }
                }
                if (intro){// if in the intro
                    current++;
                    SetLevel.writeFile(current  ,getApplicationContext(),"level");
                    if (current == 1001){// pass intro
                        SetLevel.writeFile(0,getApplicationContext(),"level");
                        textView.setText(introText[1]);
                        intro = false;
                        current = 0;
                        textIndex = current + startIndex;
                        endIndex = startIndex + 30;
                    }
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
                    startRecord.setBackgroundResource(R.drawable.finish2);
                    //startRecord.setText("BİTİR");
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
                startRecord.setBackgroundResource(R.drawable.record3);
                //startRecord.setText("BAŞLA");
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

                    play.setBackgroundResource(R.drawable.pause3);
                    //play.setText("DURDUR");
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
                    play.setBackgroundResource(R.drawable.play3);
                    //play.setText("OYNAT");

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
    private void deleteFile(){

        File file = new File(pathSv);
        if (file.exists())
            file.delete();


    }
    private void requestPermissions() {

        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    /*@Override
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
    }*/

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

    //this function add text to array
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
