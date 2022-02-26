package cn.maxkot.echo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;


public class MainActivity extends Activity{

    public static final String TAG_EXIT = "exit";
    private PermissionRequest mPermissionRequest = null;
    private boolean bRequestedPermission = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mPermissionRequest = new PermissionRequest(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean bPermitted = true;
        if (mPermissionRequest.lacksPermission() ) {
            if (! bRequestedPermission){
                mPermissionRequest.initPermissions();
                bRequestedPermission = true;
            }else{
                bPermitted = false;
                exitApp();
            }
        }else{
            bPermitted = true;
        }

        if (bPermitted){
            startEchoService();
        }
    }

    private void exitApp(){
        final Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.TAG_EXIT, true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 1500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (null != intent){
            boolean bExit = intent.getBooleanExtra(TAG_EXIT, false);
            if (bExit){
                finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ECHO", "onStop");
        bRequestedPermission = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( ! mPermissionRequest.handlePermissionRequest(requestCode, permissions, grantResults) ){
            Toast.makeText(getApplicationContext(), "获取权限失败", Toast.LENGTH_SHORT).show();
            exitApp();
        }else{
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (! mPermissionRequest.handleOverlayPermission()){
            Toast.makeText(getApplicationContext(), "悬浮权限获取失败", Toast.LENGTH_SHORT).show();
            exitApp();
        }else {
        }
    }

    void startEchoService(){
        Intent intent = new Intent(MainActivity.this, MainService.class);
        startService(intent);
        finish();
    }
}
