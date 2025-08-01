package com.example.vedio;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import android.view.ViewGroup;

public class VideoPlayerActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ExoPlayer player;
    private String videoPath;
    private String videoTitle;
    private boolean isFullScreen = false;
    private long currentPosition = 0; // 用于保存视频播放位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // 获取视频路径和标题
        videoPath = getIntent().getStringExtra("videoPath");
        videoTitle = getIntent().getStringExtra("videoTitle");

        // 设置标题
        if (videoTitle != null) {
            setTitle(videoTitle);
        }

        playerView = findViewById(R.id.player_view);

        // 初始化播放器
        initializePlayer();

        // 设置全屏按钮点击事件
        setupFullscreenButton();
    }

    // 初始化播放器
    private void initializePlayer() {
        if (videoPath == null) return;

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // 创建媒体项
        MediaItem mediaItem = MediaItem.fromUri(videoPath);
        player.setMediaItem(mediaItem);

        // 准备并播放
        player.prepare();
        // 恢复之前的播放位置
        if (currentPosition > 0) {
            player.seekTo(currentPosition);
        }
        player.play();
    }

    // 设置全屏按钮点击事件
    private void setupFullscreenButton() {
        // 获取ExoPlayer默认的全屏按钮
        View fullscreenButton = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_fullscreen);
        if (fullscreenButton instanceof ImageButton) {
            ((ImageButton) fullscreenButton).setOnClickListener(v -> toggleFullScreen());
        } else {
            Toast.makeText(this, "全屏按钮初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 切换全屏/窗口模式
    private void toggleFullScreen() {
        // 保存当前播放位置
        currentPosition = player.getCurrentPosition();

        if (isFullScreen) {
            // 退出全屏
            exitFullScreen();
        } else {
            // 进入全屏
            enterFullScreen();
        }

        // 重新初始化播放器以应用新布局
        releasePlayer();
        initializePlayer();
    }

    // 进入全屏模式
    private void enterFullScreen() {
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 隐藏系统UI（导航栏和状态栏）
        hideSystemUI();

        isFullScreen = true;
    }

    // 退出全屏模式
    private void exitFullScreen() {
        // 显示ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        // 设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 显示系统UI
        showSystemUI();

        isFullScreen = false;
    }

    // 隐藏系统UI（兼容各版本）
    private void hideSystemUI() {
        // 确保内容在导航栏和状态栏后面绘制
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Android 10及以下版本
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    // 显示系统UI（兼容各版本）
    private void showSystemUI() {
        // 恢复默认布局适配
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.show(WindowInsets.Type.systemBars());
            }
        } else {
            // Android 10及以下版本
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    // 释放播放器资源
    private void releasePlayer() {
        if (player != null) {
            currentPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            // 全屏模式下按返回键先退出全屏
            toggleFullScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 屏幕旋转时调整播放器尺寸
        if (playerView != null) {
            playerView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            playerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
    }
}
