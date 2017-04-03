package fr.klemek.quotetube;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import fr.klemek.quotetube.quote.QuoteEditorActivity;
import fr.klemek.quotetube.utils.ConnectionUtils;
import fr.klemek.quotetube.utils.Constants;
import fr.klemek.quotetube.utils.DataManager;
import fr.klemek.quotetube.utils.QPyUtils;
import fr.klemek.quotetube.utils.Utils;

public class SplashActivity extends AppCompatActivity implements QPyUtils.OnQPyResultListener {
    
    private TextView info;
    private ProgressBar progress;
    private SplashTask task;

    private static final int PERMISSION_REQUEST_READWRITE_STORAGE = 1;

    private static final int FIRST_SCRIPT_RESULT= 2;
    private static final int CHECK_YTDL_RESULT= 3;
    private static final int INST_YTDL_RESULT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        info = (TextView) findViewById(R.id.splash_info);
        progress = (ProgressBar) findViewById(R.id.splash_progress);
        progress.setMax(100);
        task = new SplashTask(LoadState.START);
        task.execute();
    }

    private enum LoadState {
        DONE(100,R.string.splash_loading_done,null),
        DATA_LOAD(95,R.string.splash_loading_data,DONE),
        DIR_CHECK(90,R.string.splash_loading_dir,DATA_LOAD),
        YTDL_INST(70,R.string.splash_loading_ytdl2,DIR_CHECK),
        YTDL_CHECK(50,R.string.splash_loading_ytdl,DIR_CHECK,YTDL_INST),
        QPY_CHECK(30,R.string.splash_loading_qpy,YTDL_CHECK),
        FFMPEG_LOAD(10,R.string.splash_loading_ffmpeg,QPY_CHECK,DIR_CHECK),
        PERM_CHECK(0,R.string.splash_loading_perm,FFMPEG_LOAD),
        START(-1,R.string.splash_loading_start,PERM_CHECK);

        int percent, idtext;
        LoadState next,jump;

        LoadState(int percent,int idtext, LoadState next, LoadState jump){
            this.percent = percent;
            this.next = next;
            this.jump = jump;
            this.idtext = idtext;
        }

        LoadState(int percent,int idtext, LoadState next){
            this.percent = percent;
            this.next = next;
            this.idtext = idtext;
        }


    }

    private class SplashTask extends AsyncTask<Void, Void, Boolean>{

        private LoadState state;

        SplashTask(LoadState state){
            this.state = state;
        }

        @Override
        protected void onPreExecute(){
            info.setText(state.idtext);
            if(state.percent >= 0) {
                progress.setIndeterminate(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress.setProgress(state.percent, true);
                } else {
                    progress.setProgress(state.percent);
                }
            }else
                progress.setIndeterminate(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            switch(state){
                case START:
                    /*try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    return true;
                case PERM_CHECK:
                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(SplashActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(SplashActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        Utils.debugLog(this,"Requesting access to permissions");
                        ActivityCompat.requestPermissions(SplashActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_READWRITE_STORAGE);
                        return false;
                    }
                    Utils.debugLog(this,"Permission access already granted");
                    return true;
                case FFMPEG_LOAD:
                    FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
                    try {
                        ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                            @Override
                            public void onStart() {}

                            @Override
                            public void onFailure() {
                                Utils.debugLog(this,"FFmpeg load failed");
                                new MaterialDialog.Builder(SplashActivity.this)
                                        .title(R.string.error_noffmpeg_title)
                                        .content(R.string.error_noffmpeg_content)
                                        .positiveText(R.string.dialog_ok)
                                        .cancelable(false)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                super.onPositive(dialog);
                                                task = new SplashTask(state.jump);
                                                task.execute();
                                            }
                                        }).show();
                            }

                            @Override
                            public void onSuccess() {
                                Utils.debugLog(this,"FFmpeg load success");
                                task = new SplashTask(state.next);
                                task.execute();
                            }

                            @Override
                            public void onFinish() {}
                        });
                        return true;
                    } catch (FFmpegNotSupportedException e) {
                        Utils.debugLog(this,"FFmpeg load error");
                        return false;
                    }
                case QPY_CHECK:
                    return QPyUtils.checkQPyInstalled(getApplicationContext());
                case YTDL_CHECK:
                    QPyUtils.QPyExec(CHECK_YTDL_RESULT,SplashActivity.this, Constants.QPY_SCRIPT_YTDL_CHECK);
                    return true;
                case YTDL_INST:
                    if(ConnectionUtils.isOnline(getApplicationContext())) {
                        QPyUtils.QPyExec(INST_YTDL_RESULT, SplashActivity.this, Constants.QPY_SCRIPT_YTDL_INST);
                        return true;
                    }else{
                        return false;
                    }
                case DIR_CHECK:
                    File fbase = new File(Constants.DIR_BASE);
                    Utils.debugLog(this, "Checking "+fbase.getAbsolutePath());
                    if(!fbase.exists()) {
                        if (!fbase.mkdir()) {
                            Utils.debugLog(this, "Couldn't create base folder");
                            return false;
                        } else
                            Utils.debugLog(this, "Base folder created");
                    }
                    File fnomedia = new File(Constants.DIR_BASE+".nomedia");
                    Utils.debugLog(this, "Checking "+fnomedia.getAbsolutePath());
                    if(!fnomedia.exists()) {
                        try {
                            if (!fnomedia.createNewFile()) {
                                Utils.debugLog(this, "Couldn't create .nomedia");
                                return false;
                            } else
                                Utils.debugLog(this, ".nomedia created");
                        } catch (IOException e) {
                            Utils.debugLog(this, "Couldn't create .nomedia");
                            return false;
                        }
                    }
                    File fquotes = new File(Constants.DIR_QUOTES);
                    Utils.debugLog(this, "Checking "+fquotes.getAbsolutePath());
                    if(!fquotes.exists()) {
                        if (!fquotes.mkdir()) {
                            Utils.debugLog(this, "Couldn't create quotes folder");
                            return false;
                        } else
                            Utils.debugLog(this, "Quotes folder created");
                    }
                    File fscripts = new File(Constants.DIR_SCRIPTS);
                    Utils.debugLog(this, "Checking "+fscripts.getAbsolutePath());
                    if(!fscripts.exists()) {
                        if (!fscripts.mkdir()) {
                            Utils.debugLog(this, "Couldn't create scripts folder");
                            return false;
                        } else
                            Utils.debugLog(this, "Scripts folder created");
                    }
                    return true;
                case DATA_LOAD:
                    DataManager.getInstance(getApplicationContext());
                    return true;
                case DONE:
                    /*try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            switch(state){
                case START:
                case DATA_LOAD:
                case PERM_CHECK:
                    if(result) {
                        task = new SplashTask(state.next);
                        task.execute();
                    }
                    break;
                case FFMPEG_LOAD:
                    if(!result) {
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_noffmpeg_title)
                                .content(R.string.error_noffmpeg_content)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        task = new SplashTask(state.jump);
                                        task.execute();
                                    }
                                }).show();
                    }
                    break;
                case QPY_CHECK:
                    if(!result) {
                        info.setText(R.string.splash_loading_qpy2);
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_noqpy_title)
                                .content(R.string.error_noqpy_content)
                                .negativeText(R.string.dialog_cancel)
                                .positiveText(R.string.error_noqpy_positive)
                                .cancelable(false)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                        System.exit(0);
                                    }

                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        try {
                                            Uri uLink = Uri.parse(Constants.QPYTHON_MARKET);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uLink);
                                            startActivity(intent);
                                            System.exit(0);
                                        } catch (Exception e) {
                                            Uri uLink = Uri.parse(Constants.QPYTHON_WEBSITE);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uLink);
                                            startActivity(intent);
                                            System.exit(0);
                                        }
                                    }
                                }).show();
                    }else{
                        task = new SplashTask(state.next);
                        task.execute();
                    }
                    break;
                case YTDL_CHECK:
                    break;
                case YTDL_INST:
                    if(!result) {
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_nointernet_title)
                                .content(R.string.error_nointernet_content)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        System.exit(0);
                                    }
                                }).show();
                    }
                    break;
                case DIR_CHECK:
                    if(result) {
                        task = new SplashTask(state.next);
                        task.execute();
                    }else{
                        //TODO error message
                        System.exit(0);
                    }
                    break;
                case DONE:
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    /*Intent i = new Intent(SplashActivity.this, QuoteEditorActivity.class);
                    i.putExtra(Constants.EXTRA_VIDEOID,"s5-nUCSXKac");*/
                    startActivity(i);
                    finish();
                    break;
            }
        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READWRITE_STORAGE: {
                boolean ok = true;
                for(int g = 0; g < grantResults.length; g++)
                    if(grantResults[g] != PackageManager.PERMISSION_GRANTED)
                        ok = false;
                if(grantResults.length > 0 && ok){
                    Utils.debugLog(this,"Permissions granted by user");
                    task = new SplashTask(LoadState.PERM_CHECK.next);
                    task.execute();
                } else {
                    Utils.debugLog(this,"Permissions denied by user");
                    System.exit(0);
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case FIRST_SCRIPT_RESULT:
                task = new SplashTask(LoadState.YTDL_CHECK);
                task.execute();
                break;
            case CHECK_YTDL_RESULT:
                QPyUtils.getResult(CHECK_YTDL_RESULT,this,data);
                break;
            case INST_YTDL_RESULT:
                QPyUtils.getResult(INST_YTDL_RESULT,this,data,true);
                break;
        }
    }

    @Override
    public void onQPyResult(int requestCode, boolean success, String result) {
        switch(requestCode){
            case CHECK_YTDL_RESULT:
                if(success && result.contains("done")){
                    Utils.debugLog(this,"Youtube-dl installed");
                    task = new SplashTask(LoadState.YTDL_CHECK.next);
                    task.execute();
                }else if(result.contains("not found")){
                    Utils.debugLog(this,"Youtube-dl not installed");
                    task = new SplashTask(LoadState.YTDL_CHECK.jump);
                    task.execute();
                }else{ //Contains 'ReferenceTable' : python libs not initialized, launching console once
                    Utils.debugLog(this,"Python libs not initialized");
                    QPyUtils.QPyExec(FIRST_SCRIPT_RESULT,SplashActivity.this,Constants.QPY_SCRIPT_CONSOLE,false);
                }
                break;
            case INST_YTDL_RESULT:
                if(success && result.contains("Successfully installed youtube-dl")){
                    Utils.debugLog(this,"Youtube-dl installation finished");
                    task = new SplashTask(LoadState.YTDL_CHECK);
                    task.execute();
                    /*Intent mStartActivity = new Intent(getApplicationContext(), SplashActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);*/
                }else{
                    Utils.debugLog(this,"Youtube-dl installation failed");
                    new MaterialDialog.Builder(SplashActivity.this)
                            .title(R.string.error_ytdlfail_title)
                            .content(R.string.error_ytdlfail_content)
                            .positiveText(R.string.dialog_ok)
                            .cancelable(false)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {;
                                    System.exit(0);
                                }
                            }).show();
                }
                break;
        }
    }


}
