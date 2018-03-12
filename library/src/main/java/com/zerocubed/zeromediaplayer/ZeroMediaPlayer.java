package com.zerocubed.zeromediaplayer;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by Zero
 * Created on 2017/12/22
 * MediaPlayer interface
 */

public interface ZeroMediaPlayer {
    int MEDIA_INFO_BUFFERING_START = 1001;
    int MEDIA_INFO_BUFFERING_END = 1002;

    int MEDIA_ERROR_UNKNOWN = 1000;

    /*
     * construct methods
     */
    void setSurface(Surface surface);

    void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void prepareAsync() throws IllegalStateException;

    /*
     * control methods
     */
    void start() throws IllegalStateException;

    void stop() throws IllegalStateException;

    void pause() throws IllegalStateException;

    void seekTo(int msec) throws IllegalStateException;

    void reset();

    void release();

    /*
     * get data and status methods
     */
    int getBufferedPercentage();

    int getCurrentPosition();

    int getDuration();

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    /*
     * set listeners
     */
    void setOnPreparedListener(OnPreparedListener listener);

    void setOnCompletionListener(OnCompletionListener listener);

    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    void setOnSeekCompleteListener(OnSeekCompleteListener listener);

    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

    void setOnErrorListener(OnErrorListener listener);

    void setOnInfoListener(OnInfoListener listener);

    /*
     * listeners
     */
    interface OnPreparedListener {
        void onPrepared(ZeroMediaPlayer mediaPlayer);
    }

    interface OnCompletionListener {
        void onCompletion(ZeroMediaPlayer mediaPlayer);
    }

    interface OnBufferingUpdateListener {
        void onBufferingUpdate(ZeroMediaPlayer mediaPlayer, int percent);
    }

    interface OnSeekCompleteListener {
        void onSeekComplete(ZeroMediaPlayer mediaPlayer);
    }

    interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(ZeroMediaPlayer mediaPlayer, int width, int height);
    }

    interface OnErrorListener {
        boolean onError(ZeroMediaPlayer mediaPlayer, int what, int extra);
    }

    interface OnInfoListener {
        boolean onInfo(ZeroMediaPlayer mediaPlayer, int what, int extra);
    }
}