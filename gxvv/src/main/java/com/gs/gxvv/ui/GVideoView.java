package com.gs.gxvv.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.gs.gxvv.R;
import com.gs.gxvv.button.OnMediaListener;


public class GVideoView extends LinearLayout {

    private GVideoPlayer mVideoView;
    private int mVideoViewHeight;
    private Activity mActivity;
    private OnMediaListener mediaListener;
    String TAG="GVideoView";
    public GVideoView(Context context) {
        this(context, null);
    }

    public GVideoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public GVideoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GwVideoView);
            mVideoViewHeight = (int) ta.getDimension(R.styleable.GwVideoView_video_view_height, ViewGroup.LayoutParams.MATCH_PARENT);
            ta.recycle();
        }
        initVideoView();
    }

    /**
     * 引入播放器
     */
    private void initVideoView() {
        mVideoView = new GVideoPlayer(getContext());
        setVideoViewHeight(mVideoViewHeight);
        addView(mVideoView);
    }

    /**
     * 设置高度
     */
    private void setVideoViewHeight(int h) {
        mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, h));
    }
    /**
     * 初始化播放器
     */
    public void setUp(Activity activity, String rul) {
        setUp(activity, Uri.parse(rul));
    }


    /**
     * 初始化播放器
     */
    public void setUp(Activity activity, Uri uri) {
        this.mActivity = activity;
        Log.e("视频准备:1:",System.currentTimeMillis()+"");
        mVideoView.initPlayer(activity, uri);
    }


    /**
     * 监听（播放，暂停）
     */
    public void setOnMediaListener(OnMediaListener mediaListener) {
        this.mediaListener = mediaListener;
        mVideoView.setOnMediaListener(mediaListener);
    }
    /**
     * 开始播放
     */
    public boolean videoIsPlaying() {
        return mVideoView.isPlaying();
    }

    /**
     * 开始播放
     */
    public void start(int msec) {
        mVideoView.start(msec);
    }

    /**
     * 开始播放
     */
    public void start() {
        mVideoView.start();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        _isPause = true;
        mVideoView.pause();
    }

    private boolean _isPause = false;

    /**
     * 继续播放
     */
    public void resume() {
        if (_isPause) {
            mVideoView.start();
            _isPause = false;
        }
    }

    /**
     * 禁止播放
     */
    public void stopPlay() {
        if(mVideoView!=null){
            mVideoView.stopPlayback();
        }

    }

}
