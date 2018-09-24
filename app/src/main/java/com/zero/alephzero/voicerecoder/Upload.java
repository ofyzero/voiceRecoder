package com.zero.alephzero.voicerecoder;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class Upload extends AppCompatActivity {

    private StorageReference mStorageRef;
    ProgressBar progressBar;
    Button upload;
    EditText name;
    String id;
    private int textIndex;
    private int textIndexBitti;
    private TextView mTextField;
    int totalBytes, currentBytes;
    double progress = 0;
    int i = 0;

    ArrayList<String> filenames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        name = findViewById(R.id.editText);
        upload = findViewById(R.id.Upload);
        mTextField = findViewById(R.id.mTextField);
        progressBar = findViewById(R.id.progressBar);
        textIndexBitti = SetLevel.fileNumber();
        textIndex = Integer.parseInt(SetLevel.readFile(getApplicationContext(), "start"));
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                id = String.valueOf(name.getText());
                if( id != null ) {
                    Toast.makeText(Upload.this,"Yükleme Başladı.",Toast.LENGTH_SHORT);
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    getNames();
                    uploadOne();
                    new CountDownTimer(300000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            mTextField.setText("Kalan Süre ( saniye ) : " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            mTextField.setText("Yükleme tamamlandı!");
                        }
                    }.start();
                }else {
                    Toast.makeText(Upload.this,"Lütfen isim yazın",Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void uploadOne(){
        if(i == textIndexBitti){
            Toast.makeText(Upload.this,"Bitti.",Toast.LENGTH_SHORT);
        }else {
            for(String path : filenames){

                Uri file = Uri.fromFile(new File (SetLevel.getFilename() + path));
                StorageReference riversRef = mStorageRef.child("sesler-" + id + "/" + file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        totalBytes = (int) taskSnapshot.getTotalByteCount();
                        currentBytes = (int) taskSnapshot.getBytesTransferred();
                        progress = (100.0*currentBytes)/totalBytes;
                        progressBar.setProgress((int) progress);
                    }
                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(Upload.this, "Dosya yülendi.", Toast.LENGTH_SHORT);
                    }
                });
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Upload.this, "Failed", Toast.LENGTH_SHORT);
                    }
                });

//                riversRef.putFile(file)
//                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                // Get a URL to the uploaded content
//                                Toast.makeText(Upload.this, "Dosya yülendi.", Toast.LENGTH_SHORT);
//
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception exception) {
//                                // Handle unsuccessful uploads
//                                // ...
//                                Toast.makeText(Upload.this, "Failed", Toast.LENGTH_SHORT);
//                            }
//                        });
            }
        }

    }
    private void getNames(){
        File folder = new File(SetLevel.getFilename());
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                filenames.add(listOfFiles[i].getName());
                //System.out.println("File " + listOfFiles[i].getName());
            }
        }
    }

}
