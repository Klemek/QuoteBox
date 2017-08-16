package fr.klemek.quotetube.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Created by klemek on 16/03/17 !
 */

public abstract class QPyUtils {

    public static int QPY_NOT_INSTALLED = 0;
    public static int QPY_INSTALLED = 1;
    public static int QPY_WRONG_VERSION = 2;

    public static int checkQPyInstalled(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(Constants.QPYTHON_PACKAGE, 0);
            Utils.debugLog(QPyUtils.class,  "QPython "+pInfo.versionName+" ("+pInfo.versionCode+") installed",0);
            if(pInfo.versionCode == Constants.QPYTHON_REQUIRED_VERSION)
                return QPY_INSTALLED;
            else
                return QPY_WRONG_VERSION;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.debugLog(QPyUtils.class,  "QPython not installed",0);
            return QPY_NOT_INSTALLED;
        }
    }

    public static void QPyExec(int requestCode, Activity a, String script, boolean addHeader) {

        Intent intent = new Intent();
        intent.setClassName(Constants.QPYTHON_PACKAGE, Constants.QPYTHON_CLASS);
        intent.setAction(Constants.QPYTHON_ACTION);

        Bundle mBundle = new Bundle();
        mBundle.putString("app", Constants.APP_ID);
        mBundle.putString("act", Constants.QPYTHON_BUNDLE_ACT);
        //mBundle.putString("param","");
        if(addHeader){
            script = Constants.QPYTHON_SCRIPT_HEADER+script;
        }

        mBundle.putString("pycode", script);

        intent.putExtras(mBundle);

        a.startActivityForResult(intent, requestCode);
    }

    public static boolean QPyExecFile(int requestCode, Activity a, String file, String code, boolean rewrite) {
        if(FileUtils.writeFile(file,code, rewrite)) {
            QPyUtils.QPyExecFile(requestCode, a, file);
            return true;
        }
        return false;
    }

    public static void QPyExecFile(int requestCode, Activity a, String file) {

        Intent intent = new Intent();
        intent.setClassName(Constants.QPYTHON_PACKAGE, Constants.QPYTHON_CLASS);
        intent.setAction(Constants.QPYTHON_ACTION);

        Bundle mBundle = new Bundle();
        mBundle.putString("app", Constants.APP_ID);
        mBundle.putString("act", Constants.QPYTHON_BUNDLE_ACT);
        mBundle.putString("flag", Constants.QPYTHON_BUNDLE_FLAG);

        mBundle.putString("param", "fileapi");//TODO constant

        mBundle.putString("pyfile", file);

        intent.putExtras(mBundle);

        a.startActivityForResult(intent, requestCode);
    }

    public static void QPyExec(int requestCode, Activity a, String script) {
        QPyExec(requestCode,a,script, true);
    }

    public static void getResult(int requestCode, OnQPyResultListener resultListener,Intent data){
        getResult(requestCode,resultListener,data,false);
    }


    public static void getResult(int requestCode, OnQPyResultListener resultListener,Intent data, boolean endFlag){
        ResultTask task = new ResultTask(requestCode, resultListener, endFlag);
        if (data!=null) {
            Bundle bundle = data.getExtras();
            String resultFile = bundle.getString("log");
            /*for(String key:bundle.keySet()){
                Utils.debugLog(QPyUtils.class,key+":"+bundle.get(key),0);
            }*/
            task.execute(resultFile);
        }else{
            Utils.debugLog(QPyUtils.class,"Null results, checking default log file",0);
            task.execute(Constants.QPYTHON_DEFAULT_LOG_FILE);
        }
    }

    public interface OnQPyResultListener {
        void onQPyResult(int requestCode, boolean success, String result);
    }

    private static class ResultTask extends AsyncTask<String, Void, String> {

        private OnQPyResultListener listener;
        private int requestCode;
        private boolean endFlag;

        ResultTask(int requestCode, OnQPyResultListener listener, boolean endFlag){
            this.listener = listener;
            this.requestCode = requestCode;
            this.endFlag = endFlag;
        }

        @Override
        protected String doInBackground(String... args) {
            Utils.debugLog(this,  "Request "+requestCode);
            if(endFlag)
                return FileUtils.readFileForce(args[0],new String[]{Constants.QPY_LOG_END_FLAG,Constants.QPY_LOG_ERROR_FLAG});
            else
                return FileUtils.readFileForce(args[0]);
        }

        protected void onPostExecute(String result){
            if(result != null && (!endFlag || !result.contains(Constants.QPY_LOG_ERROR_FLAG))){
                Utils.debugLog(this,  "Request "+requestCode+" success:"+result);
                listener.onQPyResult(requestCode,true,result);
            }else if(result == null){
                Utils.debugLog(this,  "Request "+requestCode+" error:timeout");
                listener.onQPyResult(requestCode,false,Constants.FILE_TIMEOUT_MSG);
            }else{
                Utils.debugLog(this,  "Request "+requestCode+" error:"+result);
                listener.onQPyResult(requestCode,false,result);
            }

            if(result != null)//TODO remove
                FileUtils.writeFile(Constants.DIR_BASE + "temp" + System.currentTimeMillis() + ".txt", result, false);

        }
    }

}
