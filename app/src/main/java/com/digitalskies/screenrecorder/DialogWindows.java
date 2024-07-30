package com.digitalskies.screenrecorder;


import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class DialogWindows extends DialogFragment {


    ListView dialog_ListView;
    Uri mediaUri=MediaStore.Video.Media.getContentUri("external");
    File curFolder;
    Intent intent;
    public int checkedButton;
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedPreferences;
    String folder = "/ScreenVideos";
    ArrayAdapter<String> directoryList;
    View v = null;
    TextView textView;

    private List<String> fileList = new ArrayList<>();
    private RadioGroup radioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(MyPREFERENCES, requireActivity().MODE_PRIVATE);
        checkedButton = sharedPreferences.getInt("CheckedId", 0);

    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (com.digitalskies.screenrecorder.MainActivity.id == R.layout.dialoglayout) {
            textView = v.findViewById(R.id.no_video_files);
            listDir(folder);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(MainActivity.id==R.layout.dialoglayout){
            v = inflater.inflate(R.layout.dialoglayout, container, false);
            Toolbar toolbar = v.findViewById(R.id.dialog_toolbar);
            toolbar.setTitle("Recorded Videos");
            dialog_ListView = v.findViewById(R.id.dialoglist);
            registerForContextMenu(dialog_ListView);
            dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    File uri=null;
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O_MR1){
                        uri=requireActivity().getExternalFilesDir("/");
//                        File file=new File( uri + "hhhhh" + "/" + fileList.get(position));
//                        File imagePath = new File(requireActivity().getExternalFilesDir("/"), "ScreenVideos");
                        File newFile = new File(uri + folder + "/" + fileList.get(position));
                        Uri imageUri = MyFileProvider.getUriForFile(requireActivity(), "com.digitalskies.screenrecorder.provider" ,newFile);

//                        imageUri=Uri.parse(uri + folder + "/" + fileList.get(position));
                        requireActivity().grantUriPermission(requireActivity().getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        intent = new Intent(Intent.ACTION_VIEW, imageUri);
                        intent.setDataAndType(imageUri, "video/mp4");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(intent);
                    }
                    else{
                        uri=Environment.getExternalStoragePublicDirectory("/");

                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri + folder + "/" + fileList.get(position)));
                        intent.setDataAndType(Uri.parse(uri + folder + "/" + fileList.get(position)), "video/mp4");
                        startActivity(intent);
                    }
//                    Uri fileUri=Uri.parse(uri + folder + "/" + fileList.get(position));
//
//                    Uri imageUri = FileProvider.getUriForFile(requireActivity(), "com.example.homefolder.example.provider" ,fileUri);
//
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri + folder + "/" + fileList.get(position)));
//                    intent.setDataAndType(Uri.parse(uri + folder + "/" + fileList.get(position)), "video/mp4");
//                    startActivity(intent);

                    dismiss();


                }
            });
            return v;

        }
        else if(MainActivity.id==R.layout.about){
            v = inflater.inflate(R.layout.about, container, false);
            Toolbar toolbar = v.findViewById(R.id.about_app_toolbar);
            toolbar.setTitle("About app");
            (v.findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });
            return v;
        }
        else if(MainActivity.id==R.layout.video_quality_selector){
            v = inflater.inflate(R.layout.video_quality_selector, container, false);
            Toolbar toolbar = v.findViewById(R.id.video_quality_toolbar);
            toolbar.setTitle("Select Video Quality");
            radioGroup = v.findViewById(R.id.RadioGroup);
            radioGroup.check(checkedButton);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("CheckedId", checkedId);
                    editor.apply();
                    if (checkedId == R.id.high_quality) {
                        setVideoEncodingBitrate(1200000);
                    }
                    if (checkedId == R.id.medium_quality) {
                        setVideoEncodingBitrate(900000);

                    }
                    if (checkedId == R.id.low_quality) {
                        setVideoEncodingBitrate(700000);
                    }
                }
            });
            (v.findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

//        switch (MainActivity.id) {
//
//            case R.layout.dialoglayout:
//                v = inflater.inflate(R.layout.dialoglayout, container, false);
//                Toolbar toolbar = v.findViewById(R.id.dialog_toolbar);
//                toolbar.setTitle("Recorded Videos");
//                dialog_ListView = v.findViewById(R.id.dialoglist);
//                registerForContextMenu(dialog_ListView);
//                dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view,
//                                            int position, long id) {
//
//                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Environment.getExternalStoragePublicDirectory("") + folder + "/" + fileList.get(position)));
//                        intent.setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory("") + folder + "/" + fileList.get(position)), "video/mp4");
//                        startActivity(intent);
//
//                        dismiss();
//
//
//                    }
//                });
//                return v;
//            case R.layout.about:
//                v = inflater.inflate(R.layout.about, container, false);
//                toolbar = v.findViewById(R.id.about_app_toolbar);
//                toolbar.setTitle("About app");
//                (v.findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        dismiss();
//                    }
//                });
//                return v;
//            case R.layout.video_quality_selector:
//                v = inflater.inflate(R.layout.video_quality_selector, container, false);
//                toolbar = v.findViewById(R.id.video_quality_toolbar);
//                toolbar.setTitle("Select Video Quality");
//                radioGroup = v.findViewById(R.id.RadioGroup);
//                radioGroup.check(checkedButton);
//                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(RadioGroup group, int checkedId) {
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putInt("CheckedId", checkedId);
//                        editor.apply();
//                        if (checkedId == R.id.high_quality) {
//                            setVideoEncodingBitrate(1200000);
//                        }
//                        if (checkedId == R.id.medium_quality) {
//                            setVideoEncodingBitrate(900000);
//
//                        }
//                        if (checkedId == R.id.low_quality) {
//                            setVideoEncodingBitrate(700000);
//                        }
//                    }
//                });
//                (v.findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        dismiss();
//                    }
//                });
//        }

        return v;
    }

    public void setVideoEncodingBitrate(int Bitrate) {
        MainActivity.videoEncodingBitrate = Bitrate;
    }

    public static int getVideoEncodingBitrate() {
        return MainActivity.videoEncodingBitrate;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo contextMenuInfo) {
        super.onCreateContextMenu(menu, v, contextMenuInfo);
        if (v.getId() == R.id.dialoglist) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onContextItemSelected(item);
                return true;
            }
        };
        for (int i = 0, n = menu.size(); i < n; i++)
            menu.getItem(i).setOnMenuItemClickListener(listener);

    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        boolean del;
        final int position = info.position;

        File file;
        String path;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            path=requireActivity().getExternalFilesDir("")+ folder + "/" + fileList.get(position);

        } else {
            path=Environment.getExternalStoragePublicDirectory("") + folder + "/" + fileList.get(position);
        }
        file = new File(path);;
        if (menuItem.getItemId() == R.id.delete) {
            Uri videUri=getMediaUri(path);
            if(videUri!=null){
                requireActivity().getContentResolver().delete(getMediaUri(path),null,null);
            }
            directoryList.remove(directoryList.getItem(info.position));
            del = file.delete();
            Toast.makeText(getActivity(), "file deleted", Toast.LENGTH_SHORT).show();
            return del;
        } else {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("video/*");

            Uri uri = Uri.fromFile(file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "MY SCREENVIDEO");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);


            startActivity(Intent.createChooser(intent, "Share video using"));

        }
        return super.onContextItemSelected(menuItem);
    }

    void listDir(String path) {
        File f = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            f = new File(requireActivity().getExternalFilesDir("")+path);
        } else {
            f =new File(Environment.getExternalStoragePublicDirectory("") + path);
        }



        curFolder = f;

        File[] files = f.listFiles();
        fileList.clear();
        if(files!=null && files.length>0){
            for (File file : files) {
                fileList.add(file.getName());
            }

            directoryList
                    = new ArrayAdapter<>(requireActivity(), R.layout.mylist, R.id.file_name, fileList);
            dialog_ListView.setAdapter(directoryList);

        }
        else {
            textView.setText(R.string.no_video_records);
            textView.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(),"No video records",Toast.LENGTH_SHORT).show();

        }




    }
    private Uri getMediaUri(String filePath){
        long videoId;
        String[] projection={MediaStore.Video.Media._ID};
        Cursor cursor=requireActivity().getContentResolver().query(mediaUri,projection,MediaStore.Video.Media.DATA+" LIKE? ",new String []{filePath},null);
        cursor.moveToFirst();

        int columnIndex=cursor.getColumnIndex(projection[0]);
        if(columnIndex==0){
            return null;
        }
        videoId=cursor.getLong(columnIndex);
        cursor.close();
        return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,videoId);

    }
}
