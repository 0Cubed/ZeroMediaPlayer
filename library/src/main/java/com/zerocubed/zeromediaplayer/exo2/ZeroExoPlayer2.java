package com.zerocubed.zeromediaplayer.exo2;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.zerocubed.zeromediaplayer.ZeroMediaPlayer;

import java.io.IOException;

/**
 * Created by Zero
 * Created on 2017/12/22
 * ZeroExoPlayer2 implements ZeroMediaPlayer with ExoPlayer 2.X as player core
 */

public class ZeroExoPlayer2 implements ZeroMediaPlayer {
    private static final String TAG = ZeroExoPlayer2.class.getSimpleName();
    private static final int CONTENT_TYPE_UNKNOWN = -1;
    private static final int MSG_UPDATE_BUFFERING_PERCENT = 0;
    private static final int UPDATE_BUFFERING_INTERVAL = 1000;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private int mContentType;
    private int mVideoWidth;
    private int mVideoHeight;
    private String mUserAgent;
    private String mDataSource;
    private Context mContext;
    private Surface mSurface;
    private Handler mMainHandler;
    private MediaSource mMediaSource;
    private SimpleExoPlayer mInternalMediaPlayer;
    private DataSource.Factory mMediaDataSourceFactory;
    private MappingTrackSelector mTrackSelector;
    private SimpleExoPlayerListener mInternalMediaPlayerListener;

    private ZeroMediaPlayer.OnPreparedListener mOnPreparedListener;
    private ZeroMediaPlayer.OnCompletionListener mOnCompletionListener;
    private ZeroMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private ZeroMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private ZeroMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private ZeroMediaPlayer.OnErrorListener mOnErrorListener;
    private ZeroMediaPlayer.OnInfoListener mOnInfoListener;

    public ZeroExoPlayer2(Context context) {
        this(context, CONTENT_TYPE_UNKNOWN);
    }

    public ZeroExoPlayer2(Context context, int contentType) {
        mContext = context.getApplicationContext();
        mContentType = contentType;

        mUserAgent = Util.getUserAgent(mContext, ZeroExoPlayer2.class.getSimpleName());
        mMediaDataSourceFactory = buildDataSourceFactory(true);

        mMainHandler = new Handler();
        mTrackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory
                (BANDWIDTH_METER));
        mInternalMediaPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);

        mInternalMediaPlayerListener = new SimpleExoPlayerListener();
    }


    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.setVideoSurface(mSurface);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = uri.toString().trim();
        mMediaSource = buildMediaSource();
    }

    @Override
    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(mContext, Uri.parse(path));
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.addListener(mInternalMediaPlayerListener);
            mInternalMediaPlayer.setVideoDebugListener(mInternalMediaPlayerListener);
            mInternalMediaPlayer.prepare(mMediaSource);
            if (mInternalMediaPlayerListener != null) {
                mInternalMediaPlayerListener.setIsPreparing();
            }
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
        }
    }

    @Override
    public void reset() {
        if (mInternalMediaPlayer != null) {
            mInternalMediaPlayer.release();
            mInternalMediaPlayer.removeListener(mInternalMediaPlayerListener);
            mInternalMediaPlayer.setVideoDebugListener(null);
        }

        stopBufferingUpdate();

        mSurface = null;
        mDataSource = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
        if (mInternalMediaPlayerListener != null) {
            mInternalMediaPlayerListener.reset();
        }
        resetListeners();
    }

    @Override
    public void release() {
        reset();
        mInternalMediaPlayerListener = null;
        mHandler = null;
        mInternalMediaPlayer = null;
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
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mInternalMediaPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
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

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter
                                                                      bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(mUserAgent, bandwidthMeter);
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter defaultBandwidthMeter = (useBandwidthMeter ? BANDWIDTH_METER : null);
        return new DefaultDataSourceFactory(mContext, defaultBandwidthMeter,
                buildHttpDataSourceFactory(defaultBandwidthMeter));
    }

    private int inferContentType(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (!TextUtils.isEmpty(lastPathSegment) && lastPathSegment.equals("m3u8")) {
            return C.TYPE_HLS;
        } else {
            return Util.inferContentType(lastPathSegment);
        }
    }

    private MediaSource buildMediaSource() {
        Uri contentUri = Uri.parse(mDataSource);
        int contentType = mContentType;
        if (contentType == CONTENT_TYPE_UNKNOWN) {
            contentType = inferContentType(contentUri);
        }
        switch (contentType) {
            case C.TYPE_SS:
                return new SsMediaSource(contentUri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mMediaDataSourceFactory),
                        mMainHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource(contentUri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mMediaDataSourceFactory),
                        mMainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(contentUri, mMediaDataSourceFactory, mMainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(contentUri, mMediaDataSourceFactory,
                        new DefaultExtractorsFactory(), mMainHandler, null);
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

    private class SimpleExoPlayerListener implements VideoRendererEventListener, Player
            .EventListener {
        private boolean mIsBuffering = false;
        private boolean mIsPreparing = false;

        void setIsPreparing() {
            mIsPreparing = true;
        }

        void reset() {
            mIsPreparing = false;
            mIsBuffering = false;
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray
                trackSelectionArray) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mTrackSelector
                    .getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                        == MappingTrackSelector.MappedTrackInfo
                        .RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    if (mOnErrorListener != null) {
                        android.util.Log.d(TAG, "hasOnlyUnplayableTracks");
                        mOnErrorListener.onError(ZeroExoPlayer2.this, MEDIA_ERROR_UNKNOWN,
                                MEDIA_ERROR_UNKNOWN);
                    }
                }
                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                        == MappingTrackSelector.MappedTrackInfo
                        .RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    if (mOnErrorListener != null) {
                        mOnErrorListener.onError(ZeroExoPlayer2.this, MEDIA_ERROR_UNKNOWN,
                                MEDIA_ERROR_UNKNOWN);
                    }
                }
            }
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int state) {
            if (mIsBuffering) {
                switch (state) {
                    case Player.STATE_ENDED:
                    case Player.STATE_READY:
                        mIsBuffering = false;
                        if (mOnInfoListener != null) {
                            mOnInfoListener.onInfo(ZeroExoPlayer2.this, MEDIA_INFO_BUFFERING_END,
                                    mInternalMediaPlayer.getBufferedPercentage());
                        }
                        break;
                }
            }

            if (mIsPreparing) {
                switch (state) {
                    case Player.STATE_READY:
                        mIsPreparing = false;
                        if (mOnPreparedListener != null) {
                            mOnPreparedListener.onPrepared(ZeroExoPlayer2.this);
                        }
                        startBufferingUpdate();
                        break;
                }
            }

            switch (state) {
                case Player.STATE_IDLE:
                    break;
                case Player.STATE_BUFFERING:
                    mIsBuffering = true;
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(ZeroExoPlayer2.this, MEDIA_INFO_BUFFERING_START,
                                mInternalMediaPlayer.getBufferedPercentage());
                    }
                    break;
                case Player.STATE_READY:
                    break;
                case Player.STATE_ENDED:
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(ZeroExoPlayer2.this);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(@Player.RepeatMode int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(ZeroExoPlayer2.this, MEDIA_ERROR_UNKNOWN,
                        MEDIA_ERROR_UNKNOWN);
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(ZeroExoPlayer2.this);
            }
        }

        @Override
        public void onVideoEnabled(DecoderCounters decoderCounters) {

        }

        @Override
        public void onVideoDecoderInitialized(String s, long l, long l1) {

        }

        @Override
        public void onVideoInputFormatChanged(Format format) {

        }

        @Override
        public void onDroppedFrames(int i, long l) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float
                pixelWidthHeightRatio) {
            mVideoWidth = width;
            mVideoHeight = height;

            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(ZeroExoPlayer2.this, width, height);
            }
        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {

        }

        @Override
        public void onVideoDisabled(DecoderCounters decoderCounters) {

        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_BUFFERING_PERCENT:
                    if (mOnBufferingUpdateListener != null && mInternalMediaPlayer != null) {
                        mOnBufferingUpdateListener.onBufferingUpdate(ZeroExoPlayer2.this,
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