package com.zerocubed.zeromediaplayer.exo;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.util.Util;
import com.zerocubed.zeromediaplayer.ZeroMediaPlayer;
import com.zerocubed.zeromediaplayer.exo.simpleexoplayer.DashRendererBuilder;
import com.zerocubed.zeromediaplayer.exo.simpleexoplayer.ExtractorRendererBuilder;
import com.zerocubed.zeromediaplayer.exo.simpleexoplayer.HlsRendererBuilder;
import com.zerocubed.zeromediaplayer.exo.simpleexoplayer.SimpleExoPlayer;
import com.zerocubed.zeromediaplayer.exo.simpleexoplayer.SmoothStreamingRendererBuilder;
import com.zerocubed.zeromediaplayer.exo.simpleexoplayer.SmoothStreamingTestMediaDrmCallback;
import com.zerocubed.zeromediaplayer.exo.simpleexoplayer.WidevineTestMediaDrmCallback;

import java.io.IOException;

/**
 * Created by Zero
 * Created on 2017/12/22
 * ZeroExoPlayer implements ZeroMediaPlayer with ExoPlayer 1.X as player core
 */

public class ZeroExoPlayer implements ZeroMediaPlayer {
    private static final String TAG = ZeroExoPlayer.class.getSimpleName();
    private static final int CONTENT_TYPE_UNKNOWN = -1;
    private static final int MSG_UPDATE_BUFFERING_PERCENT = 0;
    private static final int UPDATE_BUFFERING_INTERVAL = 1000;

    private Context mContext;
    private SimpleExoPlayer mInternalMediaPlayer;
    private SimpleExoPlayerListener mSimpleExoPlayerListener;
    private String mDataSource;
    private Surface mSurface;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mContentType;

    private ZeroMediaPlayer.OnPreparedListener mOnPreparedListener;
    private ZeroMediaPlayer.OnCompletionListener mOnCompletionListener;
    private ZeroMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private ZeroMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private ZeroMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private ZeroMediaPlayer.OnErrorListener mOnErrorListener;
    private ZeroMediaPlayer.OnInfoListener mOnInfoListener;

    public ZeroExoPlayer(Context context) {
        this(context, CONTENT_TYPE_UNKNOWN);
    }

    public ZeroExoPlayer(Context context, int contentType) {
        mContext = context.getApplicationContext();
        mContentType = contentType;

        mInternalMediaPlayer = new SimpleExoPlayer();//removed the parameter in original constructor
        mSimpleExoPlayerListener = new SimpleExoPlayerListener();
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.setSurface(mSurface);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = uri.toString().trim();
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.setRendererBuilder(getRendererBuilder());
        }
    }

    @Override
    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(mContext, Uri.parse(path));
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.addListener(mSimpleExoPlayerListener);
            mInternalMediaPlayer.prepare();
            mInternalMediaPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.release();
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.seekTo(msec);

            //I'm not sure whether it is right to put this callback here
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(this);
            }
        }
    }

    @Override
    public void reset() {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.release();
            mInternalMediaPlayer.removeListener(mSimpleExoPlayerListener);
            mInternalMediaPlayer.setInfoListener(null);
            mInternalMediaPlayer.setInternalErrorListener(null);
        }

        stopBufferingUpdate();

        mSurface = null;
        mDataSource = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
        if (mSimpleExoPlayerListener != null) {
            mSimpleExoPlayerListener.reset();
        }
        resetListeners();
    }

    @Override
    public void release() {
        reset();
        mSimpleExoPlayerListener = null;
        mInternalMediaPlayer = null;
        mHandler = null;
    }

    @Override
    public int getBufferedPercentage() {
        if (mInternalMediaPlayer != null) {
            return mInternalMediaPlayer.getBufferedPercentage();
        }

        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mInternalMediaPlayer != null) {
            return (int) mInternalMediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    @Override
    public int getDuration() {
        if (mInternalMediaPlayer != null) {
            return (int) mInternalMediaPlayer.getDuration();
        }

        return 0;
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public boolean isPlaying() {
        if (mInternalMediaPlayer == null) {
            return false;
        }
        int state = mInternalMediaPlayer.getPlaybackState();
        switch (state) {
            case ExoPlayer.STATE_BUFFERING:
            case ExoPlayer.STATE_READY:
                return mInternalMediaPlayer.getPlayWhenReady();
            case ExoPlayer.STATE_IDLE:
            case ExoPlayer.STATE_PREPARING:
            case ExoPlayer.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    private static int inferContentType(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (!TextUtils.isEmpty(lastPathSegment) && lastPathSegment.equals("m3u8")) {
            return Util.TYPE_HLS;
        } else {
            return Util.inferContentType(lastPathSegment);
        }
    }

    private SimpleExoPlayer.RendererBuilder getRendererBuilder() {
        Uri contentUri = Uri.parse(mDataSource);
        String userAgent = Util.getUserAgent(mContext, ZeroExoPlayer.class.getSimpleName());
        int contentType = mContentType;
        if (contentType == CONTENT_TYPE_UNKNOWN) {
            contentType = inferContentType(contentUri);
        }
        switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(mContext, userAgent, contentUri
                        .toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(mContext, userAgent, contentUri.toString(),
                        new WidevineTestMediaDrmCallback("", ""));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(mContext, userAgent, contentUri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(mContext, userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    private void startBufferingUpdate() {
        mHandler.removeMessages(MSG_UPDATE_BUFFERING_PERCENT);
        mHandler.sendEmptyMessage(MSG_UPDATE_BUFFERING_PERCENT);
    }

    private void stopBufferingUpdate() {
        mHandler.removeMessages(MSG_UPDATE_BUFFERING_PERCENT);
    }

    private void resetListeners() {
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnBufferingUpdateListener = null;
        mOnSeekCompleteListener = null;
        mOnVideoSizeChangedListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
    }

    private class SimpleExoPlayerListener implements SimpleExoPlayer.Listener {
        private boolean mIsPreparing = false;
        private boolean mIsBuffering = false;

        void reset() {
            mIsPreparing = false;
            mIsBuffering = false;
        }

        public void onStateChanged(boolean playWhenReady, int playbackState) {
            if (mIsBuffering) {
                switch (playbackState) {
                    case ExoPlayer.STATE_ENDED:
                    case ExoPlayer.STATE_READY:
                        mIsBuffering = false;
                        if (mOnInfoListener != null) {
                            mOnInfoListener.onInfo(ZeroExoPlayer.this, MEDIA_INFO_BUFFERING_END,
                                    mInternalMediaPlayer.getBufferedPercentage());
                        }
                        break;
                }
            }

            if (mIsPreparing) {
                switch (playbackState) {
                    case ExoPlayer.STATE_READY:
                        mIsPreparing = false;
                        if (mOnPreparedListener != null) {
                            mOnPreparedListener.onPrepared(ZeroExoPlayer.this);
                        }
                        startBufferingUpdate();
                        break;
                }
            }

            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    break;
                case ExoPlayer.STATE_PREPARING:
                    mIsPreparing = true;
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    mIsBuffering = true;
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(ZeroExoPlayer.this, MEDIA_INFO_BUFFERING_START,
                                mInternalMediaPlayer.getBufferedPercentage());
                    }
                    break;
                case ExoPlayer.STATE_READY:
                    break;
                case ExoPlayer.STATE_ENDED:
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(ZeroExoPlayer.this);
                    }
                    break;
                default:
                    break;
            }
        }

        public void onError(Exception e) {
            if (mOnErrorListener != null) {
                //todo: more error info
                if (e.getCause() instanceof MediaCodecTrackRenderer
                        .DecoderInitializationException) {
                    Log.d(TAG, "onError-DecoderInitializationException");
                    mOnErrorListener.onError(ZeroExoPlayer.this, MEDIA_ERROR_UNKNOWN,
                            MEDIA_ERROR_UNKNOWN);
                } else {
                    mOnErrorListener.onError(ZeroExoPlayer.this, MEDIA_ERROR_UNKNOWN,
                            MEDIA_ERROR_UNKNOWN);
                }
            }
        }

        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float
                pixelWidthHeightRatio) {
            mVideoWidth = width;
            mVideoHeight = height;

            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(ZeroExoPlayer.this, width, height);
            }
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_BUFFERING_PERCENT:
                    if (mOnBufferingUpdateListener != null && mInternalMediaPlayer != null) {
                        mOnBufferingUpdateListener.onBufferingUpdate(ZeroExoPlayer.this,
                                mInternalMediaPlayer.getBufferedPercentage());
                    }
                    sendEmptyMessageDelayed(MSG_UPDATE_BUFFERING_PERCENT,
                            UPDATE_BUFFERING_INTERVAL);
                    break;
                default:
                    break;
            }
        }
    };
}