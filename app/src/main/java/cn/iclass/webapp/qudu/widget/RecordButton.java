package cn.iclass.webapp.qudu.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.iclass.webapp.qudu.R;
import cn.iclass.webapp.qudu.audio.AmrEncoder;
import cn.iclass.webapp.qudu.audio.AudioRecorder;
import cn.iclass.webapp.qudu.util.FileUtil;

public class RecordButton extends Button {

    private static final int MIN_INTERVAL_LONGPRESS_TIME = 500; // 长按超过500毫秒开始录音
    private static final int MIN_INTERVAL_TIME = 1000; // 录音最短时间
    private static final int MAX_INTERVAL_TIME = 60000; // 录音最长时间

    private Dialog mRecordDialog;
    private long touchStartTime;// 触摸开始时间
    private long touchEndTime;// 触摸结束时间

    private long recordStartTime;// 录音开始时间
    private long recordEndTime;// 录音结束时间


    private OnFinishedRecordListener mFinishedListerer;
    private SoundPool sp;

    private ImageView iv_record_amplitude;
    private TextView tv_record_tips;
    private int[] voiceAnimRes;

    private Context context;

    private AudioRecorder audioRecorder;

    private boolean isOverSwipeUp = false;
    private boolean inRemainedTime = false;

    public RecordButton(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        voiceAnimRes = getVoiceAnimRes();
        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    }

    private int[] getVoiceAnimRes() {
        int[] voiceAnimRes = new int[]{R.drawable.chat_icon_voice1, R.drawable.chat_icon_voice2, R.drawable.chat_icon_voice3, R.drawable.chat_icon_voice4, R.drawable.chat_icon_voice5, R.drawable.chat_icon_voice6};
        return voiceAnimRes;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        if (!FileUtil.checkSdCard()) {
            Toast.makeText(context, "发送语音需要sdcard支持！", Toast.LENGTH_SHORT).show();
            return false;
        }

        String noRecordTip = (String) getTag();
        if (!TextUtils.isEmpty(noRecordTip)) {
            Toast.makeText(context, noRecordTip, Toast.LENGTH_SHORT).show();
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initlization();
                break;
            case MotionEvent.ACTION_UP:
                touchEndTime = System.currentTimeMillis();
                if (audioRecorder != null && audioRecorder.isRecording()) {
                    if (event.getY() < -50) {
                        cancelRecord();
                    } else {
                        finishRecord();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (tv_record_tips != null) {
                    if (event.getY() < -50) {
                        iv_record_amplitude.setImageResource(R.drawable.chat_icon_voice_cancel);
                        tv_record_tips.setText(R.string.voice_cancel_tips);
                        tv_record_tips.setBackgroundColor(Color.RED);
                        isOverSwipeUp = true;
                    } else if (!inRemainedTime) {
                        iv_record_amplitude.setImageResource(R.drawable.chat_icon_voice1);
                        tv_record_tips.setText(R.string.voice_up_tips);
                        tv_record_tips.setBackgroundColor(Color.TRANSPARENT);
                        isOverSwipeUp = false;
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 初始化 dialog和录音器
     */
    private void initlization() {
        isOverSwipeUp = false;
        inRemainedTime = false;
        touchStartTime = System.currentTimeMillis();
        sp.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);

                long intervalTime = touchEndTime - touchStartTime;
                if (intervalTime > 0 && intervalTime < MIN_INTERVAL_LONGPRESS_TIME) {
                    return;
                }
                if (mRecordDialog == null) {
                    mRecordDialog = new Dialog(getContext(), R.style.CustomDialog);
                    mRecordDialog.setCanceledOnTouchOutside(false);
                    mRecordDialog.setContentView(R.layout.dialog_record);
                    iv_record_amplitude = (ImageView) mRecordDialog.findViewById(R.id.iv_record_amplitude);
                    tv_record_tips = (TextView) mRecordDialog.findViewById(R.id.tv_record_tips);
                }
                tv_record_tips.setText(R.string.voice_up_tips);
                tv_record_tips.setBackgroundResource(R.color.black);
                mRecordDialog.show();
                startRecording();
            }
        });
        sp.load(getContext(), R.raw.a, 1);
        sp.play(1, 1, 1, 0, 0, 1);
    }

    /**
     * 录音完成（达到最长时间或用户决定录音完成）
     */
    private void finishRecord() {
        mRecordDialog.dismiss();
        stopRecording();
        long intervalTime = System.currentTimeMillis() - touchStartTime;
        if (intervalTime < MIN_INTERVAL_TIME || (recordEndTime - recordStartTime) < MIN_INTERVAL_TIME) {
            showShortToast();
            FileUtil.deleteFile(audioRecorder.getmRecordFile().getAbsolutePath());
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AmrEncoder.pcm2Amr(audioRecorder.getmRecordFile().getAbsolutePath(), audioRecorder.getmRecordFile().getAbsolutePath().replace(".pcm", ".amr"));
                FileUtil.deleteFile(audioRecorder.getmRecordFile().getAbsolutePath());
                if (mFinishedListerer != null) {
                    mFinishedListerer.onFinishedRecord(audioRecorder.getmRecordFile().getAbsolutePath().replace(".pcm", ".amr"), (recordEndTime - recordStartTime));
                }
            }
        }, 800);


    }

    private void showShortToast() {
        Toast toast = new Toast(this.getContext());
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.inc_voice_record_short, null);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(10);
        toast.show();
    }

    // 用户手动取消录音
    private void cancelRecord() {
        stopRecording();
        mRecordDialog.dismiss();
    }

    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        return str;
    }

    private void startRecording() {
        if (audioRecorder != null && audioRecorder.isRecording()) {
            audioRecorder.stop();
        }
        String filePath = new File(Environment.getExternalStorageDirectory() + "/QuDu/voice") + "/" + getCurrentDate() + ".pcm";
        audioRecorder = new AudioRecorder(new File(filePath));
        audioRecorder.setErrorHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == AudioRecorder.ERROR_TYPE) {
                    Toast.makeText(context, "没有麦克风权限", Toast.LENGTH_SHORT).show();
                    mRecordDialog.dismiss();
                    if (audioRecorder != null && audioRecorder.isRecording()) {
                        audioRecorder.stop();
                    }
                }
            }
        });
        try {
            audioRecorder.start();
            recordStartTime = System.currentTimeMillis();
            ShowVolumeHandler.postDelayed(recordThread, 150);
        } catch (IOException e) {
            mRecordDialog.dismiss();
            e.printStackTrace();
        }

    }

    public void stopRecording() {
        ShowVolumeHandler.removeCallbacks(recordThread);
        if (audioRecorder != null && audioRecorder.isRecording()) {
            audioRecorder.stop();
        }
        recordEndTime = System.currentTimeMillis();
    }

    private Runnable recordThread = new Runnable() {

        @Override
        public void run() {
            if (audioRecorder.isRecording()) {
                long intervalTime = System.currentTimeMillis() - touchStartTime;
                if (intervalTime > MAX_INTERVAL_TIME) {
                    finishRecord();
                } else if (intervalTime > MAX_INTERVAL_TIME - 10000) {
                    inRemainedTime = true;
                    int remainedSeconds = (int) ((MAX_INTERVAL_TIME - intervalTime) / 1000);
                    String remainedSecondsTip = getResources().getString(R.string.voice_seconds_tips);
                    tv_record_tips.setText(String.format(remainedSecondsTip, remainedSeconds + 1));
                }
                if (audioRecorder != null && !isOverSwipeUp) {
                    int amplitudeLevel = audioRecorder.getAmplitudeLevel();
                    ShowVolumeHandler.sendEmptyMessage(amplitudeLevel);
                }

                ShowVolumeHandler.postDelayed(recordThread, 150);
            }
        }
    };


    private Handler ShowVolumeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what != -1) {
                iv_record_amplitude.setImageResource(voiceAnimRes[msg.what]);
            }
        }
    };

    public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
        this.mFinishedListerer = listener;
    }

    public interface OnFinishedRecordListener {
        /**
         * 录音完成
         */
        void onFinishedRecord(String audioPath, long recordTime);
    }
}
