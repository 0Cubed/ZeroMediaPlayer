package com.zerocubed.zeromediaplayer.android;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;

import com.zerocubed.zeromediaplayer.ZeroMediaPlayer;

import java.io.IOException;

/**
 * Created by Zero
 * Created on 2017/12/22
 * ZeroAndroidMediaPlayer implements ZeroMediaPlayer with Android System MediaPlayer as player core
 */

public class ZeroAndroidMediaPlayer implements ZeroMediaPlayer {
    private static final String TAG = ZeroAndroidMediaPlayer.class.getSimpleName();

    private MediaPlayer mInternalMediaPlayer;
    private volatile int mBufferingPercent;

    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    public ZeroAndroidMediaPlayer() {
        mInternalMediaPlayer = new MediaPlayer();
        mInternalMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer
                .OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mBufferingPercent = percent;
            }
        });
    }

    @Override
    public void setSurface(Surface surface) {
        mInternalMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri);
    }

    @Override
    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(path);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mInternalMediaPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        mInternalMediaPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        mInternalMediaPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        mInternalMediaPlayer.pause();
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        mInternalMediaPlayer.seekTo(msec);
    }

    @Override
    public void reset() {
        mInternalMediaPlayer.reset();
    }

    @Override
    public void release() {
        mInternalMediaPlayer.release();
        mInternalMediaPlayer = null;
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferingPercent;
    }

    @Override
    public int getCurrentPosition() {
        return mInternalMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mInternalMediaPlayer.getDuration();
    }

    @Override
    public int getVideoWidth() {
        return mInternalMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mInternalMediaPlayer.getVideoHeight();
    }

    @Override
    public boolean isPlaying() {
        return mInternalMediaPlayer.isPlaying();
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
        mInternalMediaPlayer.setOnPreparedListener(mInternalOnPreparedListener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
        mInternalMediaPlayer.setOnCompletionListener(mInternalOnCompletionListener);
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
        mInternalMediaPlayer.setOnBufferingUpdateListener(mInternalOnBufferingUpdateListener);
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
        mInternalMediaPlayer.setOnSeekCompleteListener(mInternalOnSeekCompleteListener);
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
        mInternalMediaPlayer.setOnVideoSizeChangedListener(mInternalOnVideoSizeChangedListener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
        mInternalMediaPlayer.setOnErrorListener(mInternalOnErrorListener);
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
        mInternalMediaPlayer.setOnInfoListener(mInternalOnInfoListener);
    }

    private MediaPlayer.OnBufferingUpdateListener mInternalOnBufferingUpdateListener = new
            MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mBufferingPercent = percent;
                    if (mOnBufferingUpdateListener != null) {
                        mOnBufferingUpdateListener.onBufferingUpdate(ZeroAndroidMediaPlayer.this,
                                percent);
                    }
                }
            };
    private MediaPlayer.OnCompletionListener mInternalOnCompletionListener = new MediaPlayer
            .OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(ZeroAndroidMediaPlayer.this);
            }
        }
    };
    private MediaPlayer.OnErrorListener mInternalOnErrorListener = new MediaPlayer
            .OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return mOnErrorListener != null && mOnErrorListener.onError(ZeroAndroidMediaPlayer
                    .this, what, extra);
        }
    };
    private MediaPlayer.OnInfoListener mInternalOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            return mOnInfoListener != null && mOnInfoListener.onInfo(ZeroAndroidMediaPlayer.this,
                    what, extra);
        }
    };
    private MediaPlayer.OnPreparedListener mInternalOnPreparedListener = new MediaPlayer
            .OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(ZeroAndroidMediaPlayer.this);
            }
        }
    };
    private MediaPlayer.OnSeekCompleteListener mInternalOnSeekCompleteListener = new MediaPlayer
            .OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(ZeroAndroidMediaPlayer.this);
            }
        }
    };
    private MediaPlayer.OnVideoSizeChangedListener mInternalOnVideoSizeChangedListener = new
            MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if (mOnVideoSizeChangedListener != null) {
                        mOnVideoSizeChangedListener.onVideoSizeChanged(ZeroAndroidMediaPlayer.this,
                                width, height);
                    }
                }
            };
}