package cn.maxkot.echo;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Record {

    private MediaRecorder mMediaRecorder = null;
    private String mFileName = null;
    private String mFilePath = null;

    public Record(String filePath, String fileName){
        mFilePath = filePath;
        mFileName = mFilePath + fileName;
    }


    public void startRecord() {

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        try {

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            File destDir = new File(mFilePath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            mMediaRecorder.setOutputFile(mFileName);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            Log.i("failed!", e.getMessage());
        } catch (IOException e) {
            Log.i("failed!", e.getMessage());
        }
    }

    public void stopRecord() {
        try {
            mMediaRecorder.stop();
        } catch (RuntimeException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;

            File file = new File(mFileName);
            if (file.exists())
                file.delete();
        }
    }

    public void destroy(){

        if (null != mMediaRecorder) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
