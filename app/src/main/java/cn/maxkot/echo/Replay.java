package cn.maxkot.echo;

import android.media.MediaPlayer;

public class Replay {

    public interface ReplayCompleteCallback{

        void onComplete();
    }

    private MediaPlayer mPlayer = null;
    private String mFile = null;
    private ReplayCompleteCallback completeCallback = null;

    public Replay(String path, String file){

        mFile = path + file;
    }

    private MediaPlayer.OnPreparedListener mPrepareListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {

            mPlayer.start();
        }
    };

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            if (null != completeCallback){
                completeCallback.onComplete();
            }
        }
    };

    void setReplayListener(ReplayCompleteCallback callback){

        completeCallback = callback;
    }

    public void startPlay(){
        if (null == mPlayer)
            mPlayer = new MediaPlayer();

        try{
            mPlayer.setOnCompletionListener(completionListener);
            mPlayer.setDataSource(mFile);
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(mPrepareListener);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void stopPlay(){

        if (null != mPlayer) {

            try {
                mPlayer.stop();
                mPlayer.reset();
            } catch (RuntimeException e) {

                mPlayer.release();
                mPlayer = null;
            }
        }
    }

    void destroy(){

        if (null != mPlayer) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
