package fr.klemek.quotebox.utils;

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

@SuppressWarnings("SameParameterValue")
public abstract class QPyUtils {

    public static boolean checkQPyInstalled(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(Constants.QPYTHON_PACKAGE, 0);
            Utils.debugLog(QPyUtils.class,  "QPython "+pInfo.versionName+" ("+pInfo.versionCode+") installed");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.debugLog(QPyUtils.class,  "QPython not installed");
            return false;
        }
    }

    private static void QPyExec(int requestCode, Activity a, String code) {
        Intent intent = new Intent();
        intent.setClassName(Constants.QPYTHON_PACKAGE, Constants.QPYTHON_CLASS);
        intent.setAction(Constants.QPYTHON_ACTION);

        Bundle mBundle = new Bundle();
        mBundle.putString("app", Constants.APP_ID);
        mBundle.putString("act", Constants.QPYTHON_BUNDLE_ACT);
        mBundle.putString("flag", Constants.QPYTHON_BUNDLE_FLAG);

        mBundle.putString("pycode", code);

        intent.putExtras(mBundle);

        a.startActivityForResult(intent, requestCode);
    }

    public static boolean QPyExecFile(int requestCode, Activity a, String file, String code, boolean rewrite) {
        if(FileUtils.writeFile(file,code.replace(Constants.QPY_SCRIPT_TAG_REQUESTCODE, ""+requestCode), rewrite)) {
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

    /*public static void QPyExec(int requestCode, Activity a, String script) {
        QPyExec(requestCode,a,script, true);
    }*/

    public static String getResultNoWait(Intent data){
        Bundle bundle = data.getExtras();
        String result = bundle.getString("result");
        Utils.debugLog(QPyUtils.class,  "Result:"+result);
        return result;
    }

    public static void getResult(int requestCode, OnQPyResultListener resultListener,Intent data){
        getResult(requestCode,resultListener,data,false);
    }

    public static void getResult(int requestCode, OnQPyResultListener resultListener,Intent data, boolean endFlag){
        getResult(requestCode,resultListener,data,endFlag, null, false);
    }

    public static void getResult(int requestCode, OnQPyResultListener resultListener,Intent data, boolean endFlag, String customBegin, boolean timeout){
        ResultTask task = new ResultTask(requestCode, resultListener, endFlag, customBegin, timeout);
        if (data!=null) {
            Bundle bundle = data.getExtras();
            String resultFile = bundle.getString("log");
            task.execute(resultFile);
        }else{
            Utils.debugLog(QPyUtils.class,"Null results, checking default log file");
            task.execute(Constants.QPYTHON_DEFAULT_LOG_FILE);
        }
    }

    public interface OnQPyResultListener {
        void onQPyResult(int requestCode, boolean success, String result);
    }

    private static class ResultTask extends AsyncTask<String, Void, String> {

        private final OnQPyResultListener listener;
        private final int requestCode;
        private final boolean endFlag;
        private final String customBegin;
        private final boolean timeout;

        ResultTask(int requestCode, OnQPyResultListener listener, boolean endFlag, String customBegin, boolean timeout){
            this.listener = listener;
            this.requestCode = requestCode;
            this.endFlag = endFlag;
            this.customBegin = customBegin;
            this.timeout = timeout;
        }

        @Override
        protected String doInBackground(String... args) {
            Utils.debugLog(this,  "Request "+requestCode);
            if(endFlag)
                return FileUtils.readFileForce(args[0], new String[]{"script "+requestCode, customBegin}, new String[]{Constants.QPY_LOG_END_FLAG,Constants.QPY_LOG_ERROR_FLAG}, timeout);
            else
                return FileUtils.readFileForce(args[0], new String[]{"script "+requestCode, customBegin});
        }

        protected void onPostExecute(String result){
            if(result != null && (!endFlag || (!result.contains(Constants.QPY_LOG_ERROR_FLAG) && !result.contains("Traceback (most recent call last)")))){
                Utils.debugLog(this,  "Request "+requestCode+" success:\n"+result);
                listener.onQPyResult(requestCode,true,result);
            }else if(result == null){
                Utils.debugLog(this,  "Request "+requestCode+" error:timeout");
                listener.onQPyResult(requestCode,false,Constants.FILE_TIMEOUT_MSG);
            }else{
                Utils.debugLog(this,  "Request "+requestCode+" error:"+result);
                listener.onQPyResult(requestCode,false,result);
                FileUtils.writeFile(Constants.DIR_LOGS + "errorlog" + System.currentTimeMillis() + ".txt", result, false);
            }
        }
    }

}
