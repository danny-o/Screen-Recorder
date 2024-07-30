package com.digitalskies.screenrecorder;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.io.IOException;
import java.security.Provider;

public class ScreenCaprure extends Service {


    public static final String EXTRA_RESULT_CODE = "SCREEN_RECORD";
    public static final String EXTRA_DATA = "EXTRA_DATA";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    private static final String TAG = "MainActivity";
    static int id;
    public static Integer videoEncodingBitrate = 1200000;
    private static final int REQUEST_CODE = 1000;
    private int mScreenDensity;
    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 800;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 10;
    String dir = "/ScreenVideos";
    String folder = "ScreenVideos";
    String developer = "Developer: DigitalSkies";
    Integer count = 0;
    String videoName = "/ScreenVideo";
    File exists;
    public static ContentValues values;
    File videoFile;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_STOP_FOREGROUND_SERVICE.equals(action)) {
                stopForegroundService();
                return START_NOT_STICKY;
            }
        }

        // Start the service in the foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent activityIntent = new Intent(this, MainActivity.class);
            activityIntent.setAction("stop");
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);
            String channelId = "001";
            String channelName = "myChannel";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.createNotificationChannel(channel);
                Notification notification = new Notification.
                        Builder(getApplicationContext(), channelId)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setContentTitle(getString(R.string.ClickToCancel))
                        .setContentIntent(contentIntent)
                        .build();
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            }
        } else {
            startForeground(
                    1,
                    new Notification()

            );
        }

        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra(EXTRA_DATA);

        MediaProjectionManager mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        initRecorder();
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();


        mMediaRecorder.start();


        return START_STICKY;
    }

    private void stopForegroundService() {

        // Stop foreground service and remove the notification.
        stopScreenSharing();
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    private void initRecorder() {
        createDirectoryIfNonExists(folder);
        File uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            uri = getExternalFilesDir(dir);
        } else {
            uri = Environment.getExternalStoragePublicDirectory(dir);
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
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(uri+ videoName + ".mp4");
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(DialogWindows.getVideoEncodingBitrate());
            mMediaRecorder.setVideoFrameRate(30);
            WindowManager windowService = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int rotation = windowService.getDefaultDisplay().getRotation();
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

    private VirtualDisplay createVirtualDisplay() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowService = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowService.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        return mMediaProjection.createVirtualDisplay("ScreenCapture",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();


        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O_MR1){
//            Uri imageUri = FileProvider.getUriForFile(this, "com.digitalskies.screenrecorder.provider" ,videoFile);
//            File imagePath = new File(requireActivity().getExternalFilesDir("/"), "ScreenVideos");
//            File newFile = new File(imagePath, videoFile);
//            Uri imageUri = MyFileProvider.getUriForFile(this, "com.digitalskies.screenrecorder.provider" ,newFile);
            Uri imageUri = MyFileProvider.getUriForFile(this, "com.digitalskies.screenrecorder.provider" ,videoFile);
//
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
        }
        else{
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoFile)));
        }
    }

    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        Log.i(TAG, "MediaProjection Stopped");

    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {

            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaProjection = null;

        }
    }
}
