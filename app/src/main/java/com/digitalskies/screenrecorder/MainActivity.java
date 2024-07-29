package com.digitalskies.screenrecorder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static int id;
    public static Integer videoEncodingBitrate = 1200000;
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private MediaProjectionManager mProjectionManager;
    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 800;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private Switch aSwitch;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    String dir = "/ScreenVideos";
    String folder = "ScreenVideos";
    String developer = "Developer: DigitalSkies";
    Integer count = 0;
    String videoName = "/ScreenVideo";
    File exists;
    public static Context context;
    FragmentManager fm = getSupportFragmentManager();
    public static ContentValues values;
    public int permissionGranted=PackageManager.PERMISSION_DENIED;
    Integer integer;
    File videoFile;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createDirectoryIfNonExists(folder);
        setContentView(R.layout.activity_main);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
        integer=Build.VERSION.SDK_INT;

        aSwitch = findViewById(R.id.aswitch);


            requestPermissions();


        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
                if(permissionGranted==PackageManager.PERMISSION_GRANTED){
                    onSwitchScreenShare(v);
                }
                else{
                    switchChecked(false);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.action_open_videos){
            setId(R.layout.dialoglayout);
            showDialog();
            return true;
        }
        else if(id== R.id.about){
            setId(R.layout.about);
            showDialog();
            return true;
        }
        else if(id==R.id.select_video_qality){
            setId(R.layout.video_quality_selector);
            showDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static int setId(int idSet) {
        id = idSet;
        return id;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            initRecorder();
            mMediaProjectionCallback = new MediaProjectionCallback();
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();

        } else if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        } else {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            mMediaRecorder.reset();
            switchChecked(false);
        }


    }

    public void onSwitchScreenShare(View view) {
        if (((Switch) view).isChecked()) {
            switchChecked(true);
            shareScreen();
        } else {
            switchChecked(false);
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(videoFile)));
            Log.v(TAG, "Stopping Recording");
            stopScreenSharing();
        }
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
                mVirtualDisplay = createVirtualDisplay();
                mMediaRecorder.start();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    public static void createDirectoryIfNonExists(String path) {
        File myDir = new File(Environment.getExternalStoragePublicDirectory("/"), path);
        if (!myDir.exists()) {
            if (!myDir.mkdir()) {
            }
        }
    }

    public void showDialog() {
        context = MainActivity.this;
        DialogWindows dialogWindows = new DialogWindows();
        dialogWindows.show(fm, "about");
    }

    private void initRecorder() {
        createDirectoryIfNonExists(folder);
        exists = new File(Environment.getExternalStoragePublicDirectory(dir), videoName + ".mp4");
        if (exists.exists()) {
            count++;
            videoName = "/ScreenVideo_" + count.toString();
        } else {
            videoName = "/ScreenVideo";
            count = 0;
        }
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(Environment.getExternalStoragePublicDirectory(dir) + videoName + ".mp4");
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(DialogWindows.getVideoEncodingBitrate());
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            videoFile=new File(Environment.getExternalStoragePublicDirectory(dir) + videoName + ".mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (aSwitch.isChecked()) {
                switchChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (aSwitch.isChecked()) {
            Toast.makeText(this,
                    "you pressed back button, screen recording stopped", Toast.LENGTH_LONG).show();
            Log.d(TAG,"ACTIVITY DESTROYED MEDIA RECORDING STOPPED!!!!!");
            mMediaRecorder.reset();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(videoFile)));
            switchChecked(false);


        }
        destroyMediaProjection();
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) != PackageManager.PERMISSION_GRANTED) {

                    aSwitch.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                    permissionGranted = PackageManager.PERMISSION_DENIED;
                }
            }
        }
    }

    public void switchChecked(boolean checkedStatus) {

        int thumbColor;
        int trackColor;
        String status;

        if (checkedStatus) {
            thumbColor = Color.argb(255, 236, 236, 0);
            trackColor = thumbColor;
            status = "On";
        } else {
            thumbColor = Color.argb(255, 255, 0, 255);
            trackColor = Color.argb(255, 0, 0, 0);
            status = "Off";
            aSwitch.setChecked(false);
        }

        try {
            aSwitch.getThumbDrawable().setColorFilter(thumbColor, PorterDuff.Mode.MULTIPLY);
            aSwitch.getTrackDrawable().setColorFilter(trackColor, PorterDuff.Mode.MULTIPLY);
            aSwitch.setText(status);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }
    public void requestPermissions(){
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                    .checkSelfPermission(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale
                            (MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale
                                    (MainActivity.this, Manifest.permission.RECORD_AUDIO)) {

                        Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission
                                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                                REQUEST_PERMISSIONS);
                                    }
                                }).show();


                    }
                else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission
                                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                            REQUEST_PERMISSIONS);
                }
            }

        else{
            permissionGranted=PackageManager.PERMISSION_GRANTED;
        }


    }



}