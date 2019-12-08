package com.zju.openeye;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class ShowActivity extends AppCompatActivity {
    private static final String TAG = "OpenEye::HdActivity";

    private String photoPath = "";

    private ImageView imageView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_show);

        Intent intent=getIntent();

        photoPath = intent.getStringExtra("photoPath");

        Log.d(TAG,"..............."+photoPath+"...............");
        imageView = findViewById(R.id.show_activity_image_view);

        imageView.setImageURI(Uri.fromFile(new File(photoPath)));
    }
}
