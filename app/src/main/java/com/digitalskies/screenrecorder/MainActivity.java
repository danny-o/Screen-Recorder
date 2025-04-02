package com.digitalskies.screenrecorder;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
//    public static Context context;
    FragmentManager fm = getSupportFragmentManager();
    public static ContentValues values;
    public int permissionGranted = PackageManager.PERMISSION_DENIED;
    Integer integer;
    File videoFile;

    ActivityResultLauncher<Intent> startMediaProjection;


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

//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O_MR1){
//            File uri=getExternalFilesDir("/");
//            File newFile = new File(uri + "/"+folder  );
//            Uri imageUri = MyFileProvider.getUriForFile(this, "com.digitalskies.screenrecorder.provider" ,newFile);
//            grantUriPermission(getPackageName(),imageUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        }

        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getSystemService
                (Context.MEDIA_PROJECTION_SERVICE);
        integer = Build.VERSION.SDK_INT;

        aSwitch = findViewById(R.id.aswitch);

       startMediaProjection = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent serviceIntent = new Intent(this, ScreenCaprure.class);
                        serviceIntent.putExtra(ScreenCaprure.EXTRA_RESULT_CODE, result.getResultCode());
                        serviceIntent.putExtra(ScreenCaprure.EXTRA_DATA, result.getData());
                        startService(serviceIntent);
                    }
                    else{
                        switchChecked(false);
                    }
                }
        );


        requestPermissions();


        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(((Switch)v).isChecked()){
                    requestPermissions();
                    if (permissionGranted == PackageManager.PERMISSION_GRANTED) {
                        onSwitchScreenShare(v);
                    } else {
                        switchChecked(false);
                    }
//                }


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

        if (id == R.id.action_open_videos) {
            setId(R.layout.dialoglayout);
            showDialog();
            return true;
        } else if (id == R.id.about) {
            setId(R.layout.about);
            showDialog();
            return true;
        } else if (id == R.id.select_video_qality) {
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
//            initRecorder();
//            mMediaProjectionCallback = new MediaProjectionCallback();
//            startForegroundService(new Intent(this,ScreenCaprure.class));
//            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
//            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
//            mVirtualDisplay = createVirtualDisplay();
//            mMediaRecorder.start();

            Intent serviceIntent = new Intent(this, ScreenCaprure.class);
            serviceIntent.putExtra(ScreenCaprure.EXTRA_RESULT_CODE, resultCode);
            serviceIntent.putExtra(ScreenCaprure.EXTRA_DATA, data);
            startService(serviceIntent);

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


//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoFile)));
//            Log.v(TAG, "Stopping Recording");
            Intent intent = new Intent(this, ScreenCaprure.class);
            intent.setAction(ScreenCaprure.ACTION_STOP_FOREGROUND_SERVICE);
            startService(intent);
        }
    }

    private void shareScreen() {
//        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);




        startMediaProjection.launch(mProjectionManager.createScreenCaptureIntent());
//        if (mMediaProjection == null) {
//            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
//            return;
//        }
//        mVirtualDisplay = createVirtualDisplay();
//        mMediaRecorder.start();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    public void createDirectoryIfNonExists(String path) {
        File uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            uri = getExternalFilesDir("/");
        } else {
            uri = Environment.getExternalStoragePublicDirectory("/");
        }
        File myDir = new File(uri, path);
        if (!myDir.exists()) {
            if (!myDir.mkdir()) {
            }
        }
    }

    public void showDialog() {
        DialogWindows dialogWindows = new DialogWindows();
        dialogWindows.show(fm, "about");
    }

    private void initRecorder() {
        createDirectoryIfNonExists(folder);
        File uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            uri = getExternalFilesDir("/");
        } else {
            uri = Environment.getExternalStoragePublicDirectory("/");
        }
        exists = new File(uri, videoName + ".mp4");
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
            mMediaRecorder.setOutputFile(uri+ videoName + ".mp4");
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(DialogWindows.getVideoEncodingBitrate());
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            File fileOutput;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                fileOutput = getExternalFilesDir(dir);
            } else {
                fileOutput = Environment.getExternalStoragePublicDirectory(dir);
            }


            videoFile = new File(fileOutput + videoName + ".mp4");
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
            Log.d(TAG, "ACTIVITY DESTROYED MEDIA RECORDING STOPPED!!!!!");
            mMediaRecorder.reset();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoFile)));
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
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0) {

                int granted = grantResults[0];
                if (grantResults.length > 1) {
                    granted += grantResults[1];
                }

                if (granted != PackageManager.PERMISSION_GRANTED) {
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

    public void requestPermissions() {

        ArrayList<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.RECORD_AUDIO);
        int permissionStatus = ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO);
        boolean shouldShowRationale=   ActivityCompat.shouldShowRequestPermissionRationale
                (MainActivity.this, Manifest.permission.RECORD_AUDIO);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionStatus += ContextCompat
                    .checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
            shouldShowRationale=ActivityCompat.shouldShowRequestPermissionRationale
                    (MainActivity.this, Manifest.permission.RECORD_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale
                    (MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRationale) {

                Snackbar.make(findViewById(android.R.id.content), R.string.label_permissions,
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        permissions.toArray(new String[0]),
                                        REQUEST_PERMISSIONS);
                            }
                        }).show();


            } else {

                String[] pm= permissions.toArray(new String[0]);

                ActivityCompat.requestPermissions(MainActivity.this,
                        pm,
                        REQUEST_PERMISSIONS);
            }
        } else {
            permissionGranted = PackageManager.PERMISSION_GRANTED;
        }


    }


}