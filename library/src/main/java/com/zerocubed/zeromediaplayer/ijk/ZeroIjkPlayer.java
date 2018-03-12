package com.zerocubed.zeromediaplayer.ijk;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import com.zerocubed.zeromediaplayer.ZeroMediaPlayer;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by Zero
 * Created on 2017/12/22
 * ZeroIjkPlayer implements ZeroMediaPlayer with ijkplayer as player core
 */

public class ZeroIjkPlayer implements ZeroMediaPlayer {
    private static final String TAG = ZeroIjkPlayer.class.getSimpleName();

    private IjkMediaPlayer mInternalMediaPlayer;
    private volatile int mBufferingPercent;

    private IjkMediaPlayer.OnBufferingUpdateListener mInternalOnBufferingUpdateListener = new
            IjkMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            mBufferingPercent = percent;
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdate(ZeroIjkPlayer.this, percent);
            }
        }
    };
    private IjkMediaPlayer.OnCompletionListener mInternalOnCompletionListener = new
            IjkMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(ZeroIjkPlayer.this);
            }
        }
    };
    private IjkMediaPlayer.OnErrorListener mInternalOnErrorListener = new IjkMediaPlayer
            .OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            return mOnErrorListener != null && mOnErrorListener.onError(ZeroIjkPlayer.this, what,
                    extra);
        }
    };
    private IjkMediaPlayer.OnInfoListener mInternalOnInfoListener = new IjkMediaPlayer
            .OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            return mOnInfoListener != null && mOnInfoListener.onInfo(ZeroIjkPlayer.this, what,
                    extra);
        }
    };
    private IjkMediaPlayer.OnPreparedListener mInternalOnPreparedListener = new IjkMediaPlayer
            .OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(ZeroIjkPlayer.this);
            }
        }
    };
    private IjkMediaPlayer.OnSeekCompleteListener mInternalOnSeekCompleteListener = new
            IjkMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(ZeroIjkPlayer.this);
            }
        }
    };
    private IjkMediaPlayer.OnVideoSizeChangedListener mInternalOnVideoSizeChangedListener = new
            IjkMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int
                sar_den) {
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(ZeroIjkPlayer.this, width, height);
            }
        }
    };

    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPreparedListener mOnPreparedListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    public ZeroIjkPlayer() {
        this(true, false);
    }

    public ZeroIjkPlayer(boolean useHardwareDecoder, boolean useOpenSLES) {
        mInternalMediaPlayer = new IjkMediaPlayer();

        if (useHardwareDecoder) {
            mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        } else {
            mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        }

        if (useOpenSLES) {
            mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        } else {
            mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        }

        mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format",
                IjkMediaPlayer.SDL_FCC_RV32);

        mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

        mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT,
                "http-detect-range-support", 0);

        mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

        mInternalMediaPlayer.setOnBufferingUpdateListener(new IjkMediaPlayer
                .OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                mBufferingPercent = percent;
            }
        });
    }

    @Override
    public void setSurface(Surface surface) {
        mInternalMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException,
            IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException {
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
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferingPercent;
    }

    @Override
    public int getCurrentPosition() {
        return (int) mInternalMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return (int) mInternalMediaPlayer.getDuration();
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
}