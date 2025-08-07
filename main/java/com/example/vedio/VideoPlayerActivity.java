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
import com.google.android.exoplayer2.Player;

public class VideoPlayerActivity extends AppCompatActivity {

    private StyledPlayerView playerView;
    private ExoPlayer player;
    private String videoPath;
    private String videoTitle;
    private boolean isFullScreen = false;
    private long currentPosition = 0; // 用于保存视频播放位置
    private boolean isPlaying = false; // 记录播放状态
    private ImageButton fullscreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

        // 设置全屏按钮点击事件（适配自定义控制器）
        setupFullscreenButton();
    }

    // 初始化播放器
    private void initializePlayer() {
        if (videoPath == null) return;

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // 配置控制器
        playerView.setControllerVisibilityListener((StyledPlayerView.ControllerVisibilityListener) visibility -> {
            if (visibility == View.VISIBLE) {
                updateControllerButtons();
            }
        });
        // 设置控制器自动显示
        playerView.setControllerAutoShow(true);
        playerView.setControllerHideOnTouch(true);
        playerView.setShowNextButton(false); //还没加上组件
        playerView.setShowPreviousButton(true);
        playerView.setShowRewindButton(true);
        playerView.setShowFastForwardButton(true);

        // 创建媒体项
        MediaItem mediaItem = MediaItem.fromUri(videoPath);
        player.setMediaItem(mediaItem);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    // 确保控制器显示所有按钮
                    playerView.showController();
                }
            }

            // 可以添加其他需要的方法
        });

        // 准备并播放
        player.prepare();
        // 恢复之前的播放位置
//        if (currentPosition > 0) {
//            player.seekTo(currentPosition);
//        }
//        // 如果之前是播放状态，恢复播放
//        if (isPlaying) {
//            player.play();
//        }
        player.play();
    }

    private void updateControllerButtons() {
        // 更新播放/暂停按钮状态
        // 使用正确的资源ID
        ImageButton playButton = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_play);
        ImageButton pauseButton = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_pause);

        if (player != null && player.isPlaying()) {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        } else {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }

        // 确保全屏按钮可见
        ImageButton fullscreenButton = playerView.findViewById(R.id.exo_fullscreen);
        fullscreenButton.setVisibility(View.VISIBLE);
    }

    // 设置全屏按钮点击事件
    private void setupFullscreenButton() {
        fullscreenButton = playerView.findViewById(R.id.exo_fullscreen);
        fullscreenButton.setOnClickListener(v -> toggleFullScreen());
        // 初始状态设置
        fullscreenButton.setImageResource(isFullScreen ?
                R.drawable.exo_icon_fullscreen_exit :
                R.drawable.exo_icon_fullscreen_enter);
    }

    // 切换全屏/窗口模式
    private void toggleFullScreen() {
//        // 保存当前播放状态和位置
//        if (player != null) {
//            currentPosition = player.getCurrentPosition();
//            isPlaying = player.isPlaying();
//        }

        isFullScreen = !isFullScreen;

        ImageButton fullscreenButton = playerView.findViewById(R.id.exo_fullscreen);
        fullscreenButton.setImageResource(isFullScreen ?
                R.drawable.exo_icon_fullscreen_exit :
                R.drawable.exo_icon_fullscreen_enter);

        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        // 重新初始化播放器以应用新布局
//        releasePlayer();
//        initializePlayer();
    }




    // 释放播放器资源
    private void releasePlayer() {
        if (player != null) {
            currentPosition = player.getCurrentPosition();
            isPlaying = player.isPlaying();
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
        if (player != null && !isPlaying) {
            player.play();
            isPlaying = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            // 全屏模式下按返回键先退出全屏
            toggleFullScreen();
        } else {
            // 确保退出时释放资源
            releasePlayer();
            finish();
            super.onBackPressed();
        }
    }

}
