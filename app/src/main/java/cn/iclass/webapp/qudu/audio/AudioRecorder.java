package cn.iclass.webapp.qudu.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import java.io.File;
import java.io.IOException;


public class AudioRecorder extends BaseRecorder {
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_SAMPLING_RATE = 8000;//模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final int ERROR_TYPE = 22;

    private AudioRecord mAudioRecord = null;
    private DataEncodeThread mEncodeThread;
    private File mRecordFile;
    private Handler errorHandler;

    private int mBufferSize;
    private byte[] mPCMBuffer;
    private boolean mIsRecording = false;
    private boolean mSendError;

    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     *
     * @param recordFile target file
     */
    public AudioRecorder(File recordFile) {
        mRecordFile = recordFile;
    }

    /**
     * Start recording. Create an encoding thread. Start record from this
     * thread.
     *
     * @throws IOException initAudioRecorder throws
     */
    public void start() throws IOException {
        if (mIsRecording) {
            return;
        }
        mIsRecording = true; // 提早，防止init或startRecording被多次调用
        initAudioRecorder();
        try {
            mAudioRecord.startRecording();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new Thread() {
            boolean isError = false;

            @Override
            public void run() {
                //设置线程权限
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                while (mIsRecording) {
                    int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                    if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                        if (errorHandler != null && !mSendError) {
                            mSendError = true;
                            errorHandler.sendEmptyMessage(ERROR_TYPE);
                            mIsRecording = false;
                            isError = true;
                        }
                    } else {
                        if (readSize > 0) {
                            int mShortArrayLenght = readSize / 2;
                            short[] short_buffer = byteArray2ShortArray(mPCMBuffer, mShortArrayLenght);
                            mEncodeThread.addTask(mPCMBuffer, readSize);
                            calculateRealVolume(short_buffer, mShortArrayLenght);
                        } else {
                            if (errorHandler != null && !mSendError) {
                                mSendError = true;
                                errorHandler.sendEmptyMessage(ERROR_TYPE);
                                mIsRecording = false;
                                isError = true;
                            }
                        }
                    }
                }
                try {
                    // release and finalize audioRecord
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // stop the encoding thread and try to wait
                // until the thread finishes its job
                if (isError) {
                    mEncodeThread.sendErrorMessage();
                } else {
                    mEncodeThread.sendStopMessage();
                }
            }

        }.start();
    }

    public short[] byteArray2ShortArray(byte[] data, int items) {
        short[] retVal = new short[items];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);
        return retVal;
    }

    /**
     * 获取真实的音量。 [算法来自三星]
     *
     * @return 真实音量
     */
    @Override
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     *
     * @return 音量
     */
    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return MAX_VOLUME;
        }
        return mVolume;
    }

    public int getAmplitudeLevel() {
        if (!isRecording() || mAudioRecord == null) {
            return 0;
        } else {
            int volume = getVolume();
            if (volume > 0) {
                return (volume / 400) + 1;
            } else {
                return 0;
            }
        }
    }

    private static final int MAX_VOLUME = 1600;

    /**
     * 根据资料假定的最大值。 实测时有时超过此值。
     *
     * @return 最大音量值。
     */
    public int getMaxVolume() {
        return MAX_VOLUME;
    }

    public void stop() {
        mIsRecording = false;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * Initialize audio recorder
     */
    private void initAudioRecorder() throws IOException {
        mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT,
                mBufferSize);
        mPCMBuffer = new byte[mBufferSize];

        mEncodeThread = new DataEncodeThread(mRecordFile, mBufferSize);
        mEncodeThread.start();
    }


    /**
     * 设置错误回调
     *
     * @param errorHandler 错误通知
     */
    public void setErrorHandler(Handler errorHandler) {
        this.errorHandler = errorHandler;
    }


    public File getmRecordFile() {
        return mRecordFile;
    }

}
