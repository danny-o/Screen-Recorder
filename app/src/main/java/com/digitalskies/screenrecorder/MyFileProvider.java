package com.digitalskies.screenrecorder;

import androidx.core.content.FileProvider;

public class MyFileProvider extends FileProvider {
    public MyFileProvider() {
        super(R.xml.file_paths);
    }
}
