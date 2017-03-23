package cn.iclass.webapp.qudu.util;

/**
 * 作者：Administrator on 2016/8/8 11:09
 * 邮箱：763522252@qq.com
 */
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import java.io.IOException;

public class AudioPlayUtil {

    private static AudioPlayUtil instance;
    private AnimationDrawable animationDrawable;
    private TextView voiceTextVew;
    private MediaPlayer mPlayer;

    public static AudioPlayUtil getInstance() {
        if (instance == null) {
            instance = new AudioPlayUtil();
        }
        return instance;
    }

    public boolean isPlayer() {
        return mPlayer.isPlaying();
    }

    public AudioPlayUtil() {
        mPlayer = new MediaPlayer();
    }

    public void startPlay(final String audioPath, final TextView tv_message_voice, final boolean isSendByMe,OnCompletionListener onCompletionListener) {
        startPlay(audioPath, tv_message_voice, isSendByMe, null,onCompletionListener);
    }

    public void startPlay(final String audioPath, final TextView tv_message_voice, final boolean isSendByMe, final View view, final OnCompletionListener onCompletionListener) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            if (animationDrawable != null) {
                animationDrawable.stop();
                animationDrawable.selectDrawable(3);
            }
        }
        try {
            mPlayer.reset();
            mPlayer.setDataSource(audioPath);
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (tv_message_voice != null) {
                        animationDrawable = (AnimationDrawable) tv_message_voice.getCompoundDrawables()[isSendByMe ? 2 : 0];
                        animationDrawable.start();
                        voiceTextVew = tv_message_voice;
                    }
                }
            });
            mPlayer.start();
        } catch (IOException e) {
            Log.e("AudioPlayer", "mPlayer start() failed", e);
        }
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (voiceTextVew != null) {
                    animationDrawable.stop();
                    animationDrawable.selectDrawable(3);
                    if (view != null)
                        view.setVisibility(View.GONE);
                }
                if (onCompletionListener != null) {
                    onCompletionListener.onCompletion();
                }
            }
        });
    }

    public void release() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        if (animationDrawable != null) {
            animationDrawable.stop();
        }
        mPlayer.release();
        mPlayer = null;
        instance = null;
    }

    public interface OnCompletionListener {
        void onCompletion();
    }

}


