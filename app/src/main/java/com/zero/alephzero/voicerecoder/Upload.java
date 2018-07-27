package com.zero.alephzero.voicerecoder;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class Upload extends AppCompatActivity {

    private StorageReference mStorageRef;
    Button upload;
    EditText name;
    String id;
    private int textIndex;
    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        name = findViewById(R.id.editText);
        upload = findViewById(R.id.Upload);

        textIndex = SetLevel.fileNumber();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                id = String.valueOf(name.getText());
                if( id != null ) {
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    uploadOne();
                }else {
                    Toast.makeText(Upload.this,"Lütfen isim yazın",Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void uploadOne(){
        if(i == textIndex){
            Toast.makeText(Upload.this,"Bitti.",Toast.LENGTH_SHORT);
        }else {

            Uri file = Uri.fromFile(new File(SetLevel.getFilename() + "text" + String.valueOf(i) + ".3gp"));
            StorageReference riversRef = mStorageRef.child("sesler-" + id + "/" + file.getLastPathSegment());

            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Toast.makeText(Upload.this, "Dosya yülendi.", Toast.LENGTH_SHORT);
                            i++;
                            uploadOne();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            Toast.makeText(Upload.this, "Failed", Toast.LENGTH_SHORT);
                        }
                    });
        }
    }
}
