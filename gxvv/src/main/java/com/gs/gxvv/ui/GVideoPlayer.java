package com.gs.gxvv.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.gs.gxvv.R;
import com.gs.gxvv.button.OnMediaListener;
import com.gs.gxvv.ijk.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * *************************
 * <p>
 * 项目名称：Trainee
 *
 * @Author gy
 * 创建时间：2021/5/26 17:01
 * 用途  纯净模式
 * <p>
 * *************************
 */


public class GVideoPlayer extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    //播放器
    private IjkVideoView mVideoView;
    //进度条
    private SeekBar videoSeekBar;
    //加载框
    private ProgressBar progressBar;
    //遮罩
    private View viewMask;
    //取消
    private ImageView imgClose;


    private IMediaPlayer iMediaPlayer;
    //自定义状态回调
    private OnMediaListener mediaListener;
    /**
     * 更新（播放进度等等）
     */
    private int oldDuration = -1111;
    private int videoMaxDuration = -11;
    private boolean _startPlay = false;
    String TAG = "GVideoPlayer";
    int count = 0;


    //更新UI
    private Handler mUiHandler;
    private Activity mActivity;

    /*开始计时更新UI*/
    private Runnable mUiRunnable = new Runnable() {
        @Override
        public void run() {
            count++;
            Log.e(TAG, "计数指数：：：" + count);
            updateUI();
        }
    };

    public GVideoPlayer(Context context) {
        this(context, null);

    }

    public GVideoPlayer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GVideoPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.gx_video_layout, this);
        mUiHandler = new Handler(Looper.getMainLooper());
        init();
        //准备监听
        mVideoView.setOnPreparedListener(preparedListener);
        imgClose.setOnClickListener(this);
        //完成监听
        mVideoView.setOnCompletionListener(completionListener);
        //发生错误
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                if (what == -10000) {
                    Toast.makeText(mContext, "网路未连接，请检查网络设置", Toast.LENGTH_SHORT).show();
                    pause();
                    return true;
                }
                return false;
            }
        });


    }

    /**
     * 获取media player
     */
    public IMediaPlayer getMediaPlayer() {
        return iMediaPlayer;
    }


    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void initPlayer(Activity activity, Uri uri) {
        mActivity = activity;
        mVideoView.setVideoURI(uri);
    }

    /**
     * 更新（播放进度等等）
     */
    private void updateUI() {
        int progress = mVideoView.getCurrentPosition();
        boolean playing = mVideoView.isPlaying();
        if (playing) {
            if (oldDuration == progress && videoMaxDuration != progress) {
                progressBar.setVisibility(View.VISIBLE);
                mUiHandler.postDelayed(mUiRunnable, 50);
                return;
            } else {
                if (progressBar.getVisibility() != INVISIBLE)
                    progressBar.setVisibility(View.INVISIBLE);
            }
        }
        oldDuration = progress;
        videoSeekBar.setProgress(progress);
        if (mediaListener != null) {
            mediaListener.onProgress(progress, videoMaxDuration);
        }
        if (playing) {
            if (mediaListener != null && !_startPlay) {
                mediaListener.onStart();
            }
            _startPlay = true;
        }
        if (playing || (!_startPlay && progress != videoMaxDuration))
            mUiHandler.postDelayed(mUiRunnable, 50);
    }


    /**
     * 开始播放
     */
    public void start() {
        start(0);
    }

    /**
     * 开始播放
     */
    public void start(int progress) {
        progressBar.setVisibility(VISIBLE);
        viewMask.setVisibility(VISIBLE);
        if (progress > 0) {
            mVideoView.seekTo(progress);
        }
        mVideoView.start();

        if (mUiHandler == null) {
            mUiHandler = new Handler(Looper.getMainLooper());
        }
        mUiHandler.postDelayed(mUiRunnable, 50);
        oldDuration = -111;
        _startPlay = false;
        Log.e("TAG", "数据初始化:" + System.currentTimeMillis());
    }

    /**
     * 暂停
     */
    public void pause() {
        mUiHandler.removeCallbacks(mUiRunnable);
        progressBar.setVisibility(INVISIBLE);
        mVideoView.pause();
        if (mediaListener != null) {
            mediaListener.onPause();
        }
    }

    public void stopPlayback() {
        mVideoView.stopPlayback();
        mVideoView.release(true);
        mVideoView.stopBackgroundPlay();
        /*移除线程*/
        if (mUiHandler != null) {
            mUiHandler.removeCallbacks(mUiRunnable);
            mUiHandler = null;
        }
        Log.e("TAG", "数据初始化:结束" + System.currentTimeMillis());
        IjkMediaPlayer.native_profileEnd();


    }

    /**
     * 初始化View
     */
    private void init() {
        mVideoView = findViewById(R.id.ijk_video_player);
        mVideoView.setHudView((TableLayout) findViewById(R.id.hud_view_two));
        videoSeekBar = findViewById(R.id.video_seekBar_two);
        progressBar = findViewById(R.id.video_progressBar);
        viewMask = findViewById(R.id.view_mask);
        imgClose = findViewById(R.id.img_video_close);
    }

    /**
     * 视频播放完成  回调
     */

    private IMediaPlayer.OnCompletionListener completionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            Log.e("TAG", "视频播放结束");
            if (mediaListener != null)
                mediaListener.onEndPlay();
        }
    };
    /**
     * 视频加载完成, 准备好播放视频的回调
     */

    private IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            Log.e("视频准备:2:", System.currentTimeMillis() + "");
            iMediaPlayer = mp;
            videoMaxDuration = mVideoView.getDuration();
            videoSeekBar.setMax(videoMaxDuration);
            progressBar.setVisibility(INVISIBLE);
            viewMask.setVisibility(GONE);

        }
    };

    /**
     * 视频播放状态
     */

    public boolean isPlaying() {
        if (mVideoView != null) {
            return mVideoView.isPlaying();
        }
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_video_close) {

        }

    }

    /**
     * 监听（播放，暂停，结束）
     */
    public void setOnMediaListener(OnMediaListener mediaListener) {
        this.mediaListener = mediaListener;
    }

}
