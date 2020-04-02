package cn.maxkot.echo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

public class PermissionRequest {

    private Activity mActivity = null;
    public PermissionRequest(Activity activity){
        this.mActivity = activity;
    }

    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private String[] overlayPermission = new String[]{
            Manifest.permission.SYSTEM_ALERT_WINDOW,
    };

    private static final int OPEN_SET_REQUEST_CODE = 100;
    private static final int OVERLAY_REQUEST_CODE = 101;

    public void initPermissions(){
        if (lacksPermission(permissions))
            initPermissions(permissions);
    }

    private void initPermissions(String[] permissions) {
        if (lacksPermission(permissions)) {
            mActivity.requestPermissions( permissions, OPEN_SET_REQUEST_CODE);
        } else {
        }
    }

    public void initOverlayPermission(){
        if (lackOverLayPermission()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mActivity.getPackageName()));
            mActivity.startActivityForResult(intent, OVERLAY_REQUEST_CODE);
        }
    }

    public boolean lacksPermission() {

        return lacksPermission(permissions);
    }

    private boolean lacksPermission(String[] permissions) {
        for (String permission : permissions) {

            if(mActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    public boolean lackOverLayPermission(){

        return  ! Settings.canDrawOverlays(mActivity.getApplicationContext());
    }

    public boolean handlePermissionRequest(int requestCode, String[] permissions, int[] grantResults){
        boolean bRet = true;

        switch (requestCode){
            case OPEN_SET_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            bRet = false;
                            break;
                        }
                        else{
                            ;
                        }
                    }
                } else {
                    bRet = false;
                }
                break;
        }

        return bRet;
    }

    public boolean handleOverlayPermission(){
        boolean bRet = true;

        if ( ! Settings.canDrawOverlays(mActivity.getApplicationContext()) ){
            bRet = false;
        }else{
            bRet = true;
        }

        return bRet;
    }
}
