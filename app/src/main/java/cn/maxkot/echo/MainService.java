package cn.maxkot.echo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.MotionEvent.ACTION_MOVE;

public class MainService extends Service {

    private static final String TAG = "MainService";
    private int mLastX, mLastY;
    private boolean mMoved = false;
    private Map<STAT, STAT> mStatMap;
    private STAT mStat = STAT.STAT_IDLE;
    private Record mRecord = null;
    private Replay mPlay = null;
    private String mPath = null;
    private String mFile = null;
    private int mWidth = 200;
    private int mHeight = 124;
    private long clickStamp = 0;

    private enum STAT {
        STAT_IDLE,
        STAT_RECORD,
        STAT_REPLAY
    };

    LinearLayout touchLayout;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    int statusBarHeight = -1;

    ImageButton imageButton;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPath = Environment.getExternalStorageDirectory() + "/test/";
        mFile = "echo.m4a";

        mRecord = new Record(mPath, mFile);
        mPlay = new Replay(mPath, mFile);
        mPlay.setReplayListener(completeCallback);

        initStatMap();
        createToucher();
        changeButtonStat(mStat);
    }

    private void createToucher(){

        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        //设置效果为背景透明.x
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = mWidth;
        params.height = mHeight;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        touchLayout = (LinearLayout) inflater.inflate(R.layout.main_layout,null);
        //添加toucherlayout
        windowManager.addView(touchLayout, params);

        Log.i(TAG,"toucherlayout-->left:" + touchLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + touchLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + touchLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + touchLayout.getBottom());

        //主动计算出当前View的宽高信息.
        touchLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        imageButton = touchLayout.findViewById(R.id.btn_echo);

        imageButton.setOnTouchListener(touchListener);
        imageButton.setOnClickListener(clickListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeViewImmediate(touchLayout);
        //Log.d("click", "onDestroy.");
        mRecord.destroy();
        mPlay.destroy();
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            boolean bRet = false;
            switch (event.getAction()){
                case ACTION_DOWN:
                    mMoved = false;
                    mLastX = (int)event.getRawX();
                    mLastY = (int)event.getRawY();
                    clickStamp = System.currentTimeMillis();
                    break;
                case ACTION_UP:

                    long now = System.currentTimeMillis();

                    Log.d("click", "up" + mStat + "" + now + " stamp " + clickStamp + "move " +mMoved);
                    if (!mMoved && STAT.STAT_IDLE == mStat){

                        if ( now - clickStamp > 2500){
                            stopSelf();
                            bRet = true;
                        }else{

                        }
                    }else{

                    }
                    break;
                case ACTION_MOVE:
                    int newX = (int)event.getRawX();
                    int newY = (int)event.getRawY();
                    int distanceX = newX - mLastX;
                    int distanceY = newY - mLastY;
                    if (Math.abs(distanceX) >40 || Math.abs(distanceY) > 30){
                        mMoved = true;
                        params.x = newX - mWidth/2;
                        params.y = newY - mHeight/2 - statusBarHeight;
                        windowManager.updateViewLayout(touchLayout, params);
                        mLastX = (int)event.getRawX();
                        mLastY = (int)event.getRawY();
                    }
                    break;
            }
            return bRet;
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if ( !mMoved ){

                handleClick();
            }
        }
    };

    void initStatMap(){

        mStatMap = new HashMap<>();
        mStatMap.put(STAT.STAT_IDLE, STAT.STAT_RECORD);          //--> record
        mStatMap.put(STAT.STAT_RECORD, STAT.STAT_REPLAY);        //--> play
        mStatMap.put(STAT.STAT_REPLAY, STAT.STAT_IDLE);          //--> stop
    }

    void handleClick(){

        STAT newStat = mStatMap.get(mStat);
        mStat = newStat;
        handleStat();
    }

    Replay.ReplayCompleteCallback completeCallback = new Replay.ReplayCompleteCallback() {
        @Override
        public void onComplete() {

            if (mStat == STAT.STAT_REPLAY){
                STAT newStat = mStatMap.get(mStat);
                mStat = newStat;
                handleStat();
            }
        }
    };

    void handleStat(){

        switch (mStat){
            case STAT_IDLE:
                mPlay.stopPlay();
                break;
            case STAT_RECORD:
                mRecord.startRecord();
                break;
            case STAT_REPLAY:
                mRecord.stopRecord();
                mPlay.startPlay();
                break;
            default:
                break;
        }
        changeButtonStat(mStat);
    }

    void changeButtonStat(STAT stat){

        switch (stat){
            case STAT_IDLE:
                imageButton.setImageResource(R.drawable.ic_microphone_idle);
                imageButton.setBackgroundResource(R.drawable.ic_idle_background);
                break;
            case STAT_RECORD:
                imageButton.setImageResource(R.drawable.ic_microphone);
                imageButton.setBackgroundResource(R.drawable.btn_mic_animation);
                AnimationDrawable animationRecord = (AnimationDrawable)imageButton.getBackground();
                animationRecord.start();
                break;
            case STAT_REPLAY:
                imageButton.setImageResource(R.drawable.ic_sound);
                imageButton.setBackgroundResource(R.drawable.btn_sound_animation);
                AnimationDrawable animationPlay = (AnimationDrawable)imageButton.getBackground();
                animationPlay.start();
                break;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
