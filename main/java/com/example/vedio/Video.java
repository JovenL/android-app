package com.example.vedio;

import android.net.Uri;
public class Video {
    private String title;
    private String path;
    private long duration;
    private long size;

    public Video(String title, String path, long duration, long size) {
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public Uri getUri() {
        return Uri.parse(path);
    }

    public long getDuration() {
        return duration;
    }

    public String getFormattedDuration() {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = duration / (1000 * 60 * 60);

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public long getSize() {
        return size;
    }

    public String getFormattedSize() {
        if (size < 1024 * 1024) {
            return String.format("%.2f KB", (float) size / 1024);
        } else {
            return String.format("%.2f MB", (float) size / (1024 * 1024));
        }
    }
}
