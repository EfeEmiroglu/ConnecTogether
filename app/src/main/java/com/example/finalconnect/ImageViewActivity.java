package com.example.finalconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.mukesh.tinydb.TinyDB;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageViewActivity extends AppCompatActivity {
    ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private float xCoOrdinate, yCoOrdinate;
    String value;
    View view;
    private boolean isTouch = false;
    private boolean isNotified = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_activity);

        imageView = findViewById(R.id.imageView2);
        view = findViewById(R.id.viewId);
        TinyDB tinyDB = new TinyDB(getApplicationContext());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());


        if(getIntent().hasExtra("imageBitmap")) {
            Bitmap b = BitmapFactory.decodeByteArray(
                    getIntent().getByteArrayExtra("imageBitmap"),0,getIntent()
                            .getByteArrayExtra("imageBitmap").length);

            Bundle extras = getIntent().getExtras();
            value = extras.getString("statusText");

            if(value.equals("Left")){
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), b);
                ClipDrawable clipDrawable = new ClipDrawable(bitmapDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
                clipDrawable.setLevel(5000);

                imageView.setImageDrawable(clipDrawable);
                imageView.setBackground(clipDrawable);
                imageView.setImageResource(android.R.color.transparent);

                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int X = (int) event.getX();
                        int Y = (int) event.getY();

                        int eventaction = event.getAction();
                        switch (eventaction) {
                            case MotionEvent.ACTION_DOWN:

                                int x = tinyDB.getInt("xCoord");
                                int y = tinyDB.getInt("yCoord");
                                Toast.makeText(ImageViewActivity.this, "ACTION_DOWN AT COORDS "+"X: "+x+" Y: "+y, Toast.LENGTH_SHORT).show();

                                isTouch = true;
                                break;

                            case MotionEvent.ACTION_MOVE:
                                Toast.makeText(ImageViewActivity.this, "MOVE "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                                break;

                            case MotionEvent.ACTION_UP:
                                Toast.makeText(ImageViewActivity.this, "ACTION_UP "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });

                MoveBitmap();
            }else{
                //sets the image to only show the right part
                BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), b);
                ClipDrawable clipDrawable = new ClipDrawable(bitmapDrawable, Gravity.RIGHT, ClipDrawable.HORIZONTAL);
                clipDrawable.setLevel(5000);
                imageView.setImageDrawable(clipDrawable);
                imageView.setBackground(clipDrawable);
                imageView.setImageResource(android.R.color.transparent);

                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int X = (int) event.getX();
                        int Y = (int) event.getY();

                        int eventaction = event.getAction();
                        switch (eventaction) {
                            case MotionEvent.ACTION_DOWN:
                                Toast.makeText(ImageViewActivity.this, "ACTION_DOWN AT COORDS "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                                tinyDB.putInt("xCoord", X);
                                tinyDB.putInt("yCoord", Y);

                                isTouch = true;
                                break;

                            case MotionEvent.ACTION_MOVE:
                                Toast.makeText(ImageViewActivity.this, "MOVE "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                                break;

                            case MotionEvent.ACTION_UP:
                                Toast.makeText(ImageViewActivity.this, "ACTION_UP "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });

                MoveBitmap();
            }
        }


    }

    //zoom in and out
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void MoveBitmap(){
        //to move the image
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        xCoOrdinate = view.getX() - event.getRawX();
                        yCoOrdinate = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                        break;
                    default:
                        return false;
                }
                scaleGestureDetector.onTouchEvent(event);

                return true;
            }
        });
    }
}
