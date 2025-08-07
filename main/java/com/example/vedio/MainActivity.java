package com.example.vedio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener{

    private static final int REQUEST_PERMISSION = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100; // 权限请求码
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<Video> videoList;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(this, videoList, this);
        recyclerView.setAdapter(videoAdapter);

        // 检查权限
        //checkPermission();
        // 替换原来直接调用loadVideoList()的逻辑，先检查权限（2）
        if (checkStoragePermission()) {
            // 已授权，加载视频
            loadVideoList();
            triggerMediaScan(); // 新增媒体扫描触发
        } else {
            // 未授权，请求权限
            requestStoragePermission();
        }
    }

    // 添加：检查存储权限的方法（3）
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 用READ_MEDIA_VIDEO
            return ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12及以下用READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }
    }
    // 添加：请求存储权限的方法（4）
    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.READ_MEDIA_VIDEO},
                    STORAGE_PERMISSION_REQUEST_CODE
            );
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE
            );
        }
    }
    // 添加：处理权限请求结果的方法（5）
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限通过，加载视频
                loadVideoList();
            } else {
                // 权限被拒，提示用户
                Toast.makeText(this, "请授予存储权限以查看视频", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 新增方法：触发媒体扫描
    private void triggerMediaScan() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(new File(Environment.getExternalStorageDirectory())));
        sendBroadcast(mediaScanIntent);
    }

    // 新增方法：强制刷新媒体库
    private void refreshMediaStore() {
        new Thread(() -> {
            MediaScannerConnection.scanFile(this,
                    new String[]{Environment.getExternalStorageDirectory().toString()},
                    new String[]{"video/*"},
                    (path, uri) -> {
                        // 扫描完成后重新加载视频
                        runOnUiThread(this::loadVideoList);
                    });
        }).start();
    }

    // 加载本地视频
    private void loadVideoList() {
        progressBar.setVisibility(View.VISIBLE);
        videoList.clear();

        new Thread(() -> {
            // 查询本地视频
            String[] projection = {
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE
            };

            Cursor cursor = getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, //这里可以用Uri.parse("/storage/0/Movies")设置在哪里查找视频
                    projection,
                    null,
                    null,
                    MediaStore.Video.Media.DATE_ADDED + " DESC"
            );

            if (cursor != null) {
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);

                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleColumn);
                    String path = cursor.getString(pathColumn);
                    long duration = cursor.getLong(durationColumn);
                    long size = cursor.getLong(sizeColumn);

                    videoList.add(new Video(title, path, duration, size));
                }
                cursor.close();
            }

            // 在UI线程更新列表
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                videoAdapter.notifyDataSetChanged();

                // 如果没有视频，显示提示信息
                if (videoList.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                }
            });
        }).start();
    }


    @Override
    public void onVideoClick(Video video) {
        // 点击视频项，跳转到播放页面
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("videoPath", video.getPath());
        intent.putExtra("videoTitle", video.getTitle());
        startActivity(intent);
    }
    // 可选：添加onResume重新加载视频（6）
    @Override
    protected void onResume() {
        super.onResume();
        // 重新进入页面时，如果已授权则刷新视频列表
        if (checkStoragePermission()) {
            loadVideoList();
        }
    }

}