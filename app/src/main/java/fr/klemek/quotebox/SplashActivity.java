package fr.klemek.quotebox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import fr.klemek.quotebox.utils.ConnectionUtils;
import fr.klemek.quotebox.utils.Constants;
import fr.klemek.quotebox.utils.DataManager;
import fr.klemek.quotebox.utils.QPyUtils;
import fr.klemek.quotebox.utils.Utils;

/**
 * Created by klemek on ? !
 */

public class SplashActivity extends AppCompatActivity implements QPyUtils.OnQPyResultListener {

    private static final int PERMISSION_REQUEST_READWRITE_STORAGE = 1;
    private static final int FIRST_SCRIPT_RESULT = 2;
    private static final int CHECK_YTDL_RESULT = 3;
    private static final int INST_YTDL_RESULT = 4;
    private static final int UPGR_YTDL_RESULT = 5;
    private static final int QPY_INIT_RESULT = 6;
    private TextView info;
    private ProgressBar progress;
    private SplashTask task;
    private Timer timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.debugLog(this, "QuoteBox " + Constants.VERSION_ID + " (" + Constants.VERSION + ") runnning on Android " + Build.VERSION.SDK_INT);
        setContentView(R.layout.activity_splash);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        info = findViewById(R.id.splash_info);
        progress = findViewById(R.id.splash_progress);
        progress.setMax(100);
        task = new SplashTask(LoadState.START);
        task.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (task.getStatus() == AsyncTask.Status.PENDING)
            task.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READWRITE_STORAGE: {
                boolean ok = true;
                for (int grantResult : grantResults)
                    if (grantResult != PackageManager.PERMISSION_GRANTED)
                        ok = false;
                if (grantResults.length > 0 && ok) {
                    Utils.debugLog(this, "Permissions granted by user");
                    task = new SplashTask(LoadState.PERM_CHECK.next);
                    task.execute();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale(permissions[0])) {
                        Utils.debugLog(this, "Permissions strongly denied by user");
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_permission_title)
                                .content(R.string.error_permission_content2)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        System.exit(0);
                                    }
                                })
                                .show();
                    } else {
                        Utils.debugLog(this, "Permissions denied by user");
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_permission_title)
                                .content(R.string.error_permission_content)
                                .positiveText(R.string.error_permission_positive)
                                .negativeText(R.string.error_permission_negative)
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        task = new SplashTask(LoadState.PERM_CHECK);
                                        task.execute();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        System.exit(0);
                                    }
                                })
                                .show();
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (timeout != null)
            timeout.cancel();
        switch (requestCode) {
            case FIRST_SCRIPT_RESULT:
                task = new SplashTask(LoadState.YTDL_CHECK);
                task.execute();
                break;
            case QPY_INIT_RESULT:
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                task = new SplashTask(LoadState.YTDL_CHECK);
                task.execute();
                break;
            case CHECK_YTDL_RESULT:
                QPyUtils.getResult(CHECK_YTDL_RESULT, this, data, true, null, true);
                break;
            case INST_YTDL_RESULT:
                QPyUtils.getResult(INST_YTDL_RESULT, this, data, true, "Downloading", false);
                break;
            case UPGR_YTDL_RESULT:
                QPyUtils.getResult(UPGR_YTDL_RESULT, this, data, true);
                break;
        }
    }

    @Override
    public void onQPyResult(int requestCode, boolean success, String result) {
        switch (requestCode) {
            case CHECK_YTDL_RESULT:
                if (success && result.contains(Constants.QPY_LOG_END_FLAG)) {
                    Utils.debugLog(this, "Youtube-dl installed");
                    task = new SplashTask(LoadState.YTDL_CHECK.next);
                    task.execute();
                } else if (result != null && result.contains(Constants.QPY_LOG_ERROR_FLAG)) {
                    Utils.debugLog(this, "Youtube-dl not installed");
                    task = new SplashTask(LoadState.YTDL_INST);
                    task.execute();
                } else if (result != null && result.equals(Constants.FILE_TIMEOUT_MSG)) {
                    task = new SplashTask(LoadState.YTDL_CHECK);
                    task.execute();
                } else { //Contains 'ReferenceTable' : python libs not initialized, launching console once
                    Utils.debugLog(this, "Python libs not initialized");
                    //QPyUtils.QPyExec(FIRST_SCRIPT_RESULT,SplashActivity.this,Constants.QPY_FIRST_SCRIPT,false);
                    if (!QPyUtils.QPyExecFile(FIRST_SCRIPT_RESULT, SplashActivity.this, Constants.QPY_TEMP_SCRIPT_PATH, Constants.QPY_FIRST_SCRIPT, true))
                        Utils.debugLog(this, "Could not create first script file");
                }
                break;
            case UPGR_YTDL_RESULT:
                if (success) {
                    Utils.debugLog(this, "Youtube-dl upgrade finished");
                    task = new SplashTask(LoadState.YTDL_UPGR.next);
                    task.execute();
                } else {
                    Utils.debugLog(this, "Youtube-dl upgrade failed");
                    new MaterialDialog.Builder(SplashActivity.this)
                            .title(R.string.error_ytdlfail_title)
                            .content(R.string.error_ytdlfail_content)
                            .positiveText(R.string.dialog_ok)
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    task = new SplashTask(LoadState.YTDL_UPGR.next);
                                    task.execute();
                                }
                            }).show();
                }
                break;
            case INST_YTDL_RESULT:
                if (success && result.contains("Successfully installed youtube-dl")) {
                    Utils.debugLog(this, "Youtube-dl installation finished");
                    task = new SplashTask(LoadState.YTDL_CHECK);
                    task.execute();
                } else {
                    Utils.debugLog(this, "Youtube-dl installation failed");
                    new MaterialDialog.Builder(SplashActivity.this)
                            .title(R.string.error_ytdlfail_title)
                            .content(R.string.error_ytdlfail_content)
                            .positiveText(R.string.dialog_ok)
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    System.exit(0);
                                }
                            }).show();
                }
                break;
        }
    }

    private enum LoadState {
        DONE(100, R.string.splash_loading_done, null),
        DATA_LOAD(95, R.string.splash_loading_data, DONE),
        YTDL_INST(75, R.string.splash_loading_ytdl2, DATA_LOAD),
        YTDL_UPGR(75, R.string.splash_loading_ytdl3, DATA_LOAD),
        YTDL_CHECK(55, R.string.splash_loading_ytdl, DATA_LOAD, YTDL_UPGR),
        QPY_INIT(50, R.string.splash_loading_qpy5, YTDL_CHECK),
        QPY_CHECK(15, R.string.splash_loading_qpy, QPY_INIT),
        FFMPEG_LOAD(10, R.string.splash_loading_ffmpeg, QPY_CHECK, DATA_LOAD),
        DIR_CHECK(5, R.string.splash_loading_dir, FFMPEG_LOAD),
        PERM_CHECK(0, R.string.splash_loading_perm, DIR_CHECK),
        START(-1, R.string.splash_loading_start, PERM_CHECK);

        final int percent;
        final int idtext;
        final LoadState next;
        LoadState jump;

        LoadState(int percent, int idtext, LoadState next, LoadState jump) {
            this.percent = percent;
            this.next = next;
            this.jump = jump;
            this.idtext = idtext;
        }

        LoadState(int percent, int idtext, LoadState next) {
            this.percent = percent;
            this.next = next;
            this.idtext = idtext;
        }


    }

    private class SplashTask extends AsyncTask<Void, Void, Boolean> {

        private final LoadState state;

        SplashTask(LoadState state) {
            this.state = state;
        }

        @Override
        protected void onPreExecute() {
            info.setText(state.idtext);
            Utils.debugLog(this, getResources().getString(state.idtext));
            if (state.percent >= 0) {
                progress.setIndeterminate(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress.setProgress(state.percent, true);
                } else {
                    progress.setProgress(state.percent);
                }
            } else
                progress.setIndeterminate(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            switch (state) {
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
                        Utils.debugLog(this, "Requesting access to permissions");
                        ActivityCompat.requestPermissions(SplashActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_READWRITE_STORAGE);
                        return false;
                    }
                    Utils.debugLog(this, "Permission access already granted");
                    return true;
                case FFMPEG_LOAD:
                    FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
                    try {
                        ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onFailure() {
                                Utils.debugLog(SplashTask.this, "FFmpeg load failed");
                                new MaterialDialog.Builder(SplashActivity.this)
                                        .title(R.string.error_noffmpeg_title)
                                        .content(R.string.error_noffmpeg_content)
                                        .positiveText(R.string.dialog_ok)
                                        .cancelable(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                task = new SplashTask(state.jump);
                                                task.execute();
                                            }
                                        }).show();
                            }

                            @Override
                            public void onSuccess() {
                                Utils.debugLog(SplashTask.this, "FFmpeg load success");
                                task = new SplashTask(state.next);
                                task.execute();
                            }

                            @Override
                            public void onFinish() {
                            }
                        });
                        return true;
                    } catch (FFmpegNotSupportedException e) {
                        Utils.debugLog(this, "FFmpeg load error");
                        return false;
                    }
                case QPY_CHECK:
                    return QPyUtils.checkQPyInstalled(getApplicationContext());
                case QPY_INIT:
                    if (QPyUtils.QPyExecFile(QPY_INIT_RESULT, SplashActivity.this, Constants.QPY_TEMP_SCRIPT_PATH, Constants.QPY_INIT_SCRIPT, true)) {
                        timeout = new Timer();
                        timeout.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                task = new SplashTask(state);
                                task.execute();
                            }
                        }, Constants.MAX_QPY_WAIT);
                        return true;
                    } else {
                        Utils.debugLog(this, "Could not create qpy init script file");
                        return false;
                    }
                case YTDL_CHECK:
                    if (QPyUtils.QPyExecFile(CHECK_YTDL_RESULT, SplashActivity.this, Constants.QPY_SCRIPT_YTDL_CHECK_PATH, Constants.QPY_SCRIPT_YTDL_CHECK, false)) {
                        timeout = new Timer();
                        timeout.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                task = new SplashTask(state);
                                task.execute();
                            }
                        }, Constants.MAX_QPY_WAIT);
                        return true;
                    } else {
                        Utils.debugLog(this, "Could not create ytdl check script file");
                        return false;
                    }
                    //QPyUtils.QPyExec(CHECK_YTDL_RESULT,SplashActivity.this, Constants.QPY_SCRIPT_YTDL_CHECK);
                    //return true;
                case YTDL_UPGR:
                    if (ConnectionUtils.isOnline(getApplicationContext())) {
                        if (QPyUtils.QPyExecFile(UPGR_YTDL_RESULT, SplashActivity.this, Constants.SCRIPT_YTDL_UPGR_PATH, Constants.QPY_SCRIPT_YTDL_UPGR, false)) {
                            timeout = new Timer();
                            timeout.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    task = new SplashTask(state);
                                    task.execute();
                                }
                            }, Constants.MAX_QPY_WAIT);
                            return true;
                        } else {
                            Utils.debugLog(this, "Could not create ytdl upgr script file");
                            return false;
                        }
                    }
                    return true;
                case YTDL_INST:
                    if (ConnectionUtils.isOnline(getApplicationContext())) {
                        if (QPyUtils.QPyExecFile(INST_YTDL_RESULT, SplashActivity.this, Constants.SCRIPT_YTDL_INST_PATH, Constants.QPY_SCRIPT_YTDL_INST, false)) {
                            timeout = new Timer();
                            timeout.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    task = new SplashTask(state);
                                    task.execute();
                                }
                            }, Constants.MAX_QPY_WAIT);
                            return true;
                        } else {
                            Utils.debugLog(this, "Could not create ytdl inst script file");
                            return false;
                        }
                        //QPyUtils.QPyExec(INST_YTDL_RESULT, SplashActivity.this, Constants.QPY_SCRIPT_YTDL_INST);
                        //return true;
                    } else {
                        return false;
                    }
                case DIR_CHECK:
                    File fbase = new File(Constants.DIR_BASE);
                    Utils.debugLog(this, "Checking " + fbase.getAbsolutePath());
                    if (!fbase.exists()) {
                        if (!fbase.mkdir()) {
                            Utils.debugLog(this, "Couldn't create base folder");
                            return false;
                        } else
                            Utils.debugLog(this, "Base folder created");
                    }
                    File fnomedia = new File(Constants.DIR_BASE + ".nomedia");
                    Utils.debugLog(this, "Checking " + fnomedia.getAbsolutePath());
                    if (!fnomedia.exists()) {
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
                    Utils.debugLog(this, "Checking " + fquotes.getAbsolutePath());
                    if (!fquotes.exists()) {
                        if (!fquotes.mkdir()) {
                            Utils.debugLog(this, "Couldn't create quotes folder");
                            return false;
                        } else
                            Utils.debugLog(this, "Quotes folder created");
                    }
                    File fscripts = new File(Constants.DIR_SCRIPTS);
                    Utils.debugLog(this, "Checking " + fscripts.getAbsolutePath());
                    if (!fscripts.exists()) {
                        if (!fscripts.mkdir()) {
                            Utils.debugLog(this, "Couldn't create scripts folder");
                            return false;
                        } else
                            Utils.debugLog(this, "Scripts folder created");
                    }
                    File flogs = new File(Constants.DIR_LOGS);
                    Utils.debugLog(this, "Checking " + flogs.getAbsolutePath());
                    if (!flogs.exists()) {
                        if (!flogs.mkdir()) {
                            Utils.debugLog(this, "Couldn't create logs folder");
                            return false;
                        } else
                            Utils.debugLog(this, "Logs folder created");
                    }
                    return true;
                case DATA_LOAD:
                    DataManager.getInstance();
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
        protected void onPostExecute(Boolean result) {
            switch (state) {
                case START:
                case DATA_LOAD:
                case PERM_CHECK:
                    if (result) {
                        task = new SplashTask(state.next);
                        task.execute();
                    } else {
                        return;
                    }
                case FFMPEG_LOAD:
                    if (!result) {
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_noffmpeg_title)
                                .content(R.string.error_noffmpeg_content)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        task = new SplashTask(state.jump);
                                        task.execute();
                                    }
                                })
                                .show();
                        return;
                    }
                    break;
                case QPY_CHECK:
                    if (!result) {
                        info.setText(R.string.splash_loading_qpy2);
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_noqpy_title)
                                .content(R.string.error_noqpy_content)
                                .negativeText(R.string.dialog_cancel)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        System.exit(0);
                                    }
                                })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
                        return;
                    } else {
                        task = new SplashTask(state.next);
                        task.execute();
                    }
                    break;
                case QPY_INIT:
                    return;
                case YTDL_CHECK:
                    return;
                case YTDL_UPGR:
                    return;
                case YTDL_INST:
                    if (!result) {
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_nointernet_title)
                                .content(R.string.error_nointernet_content)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        System.exit(0);
                                    }
                                })
                                .show();
                        return;
                    }
                    break;
                case DIR_CHECK:
                    if (result) {
                        task = new SplashTask(state.next);
                        task.execute();
                    }
                    break;
                case DONE:
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    /*i = new Intent(SplashActivity.this, QuoteCreationActivity.class);
                    i.putExtra(Constants.EXTRA_VIDEOID,"s5-nUCSXKac");*/
                    startActivity(i);
                    finish();
                    break;
            }
            if (!result) {
                new MaterialDialog.Builder(SplashActivity.this)
                        .title(R.string.error_unknown_title)
                        .content(getResources().getString(R.string.error_unkown_content, state + " has failed"))
                        .positiveText(R.string.dialog_ok)
                        .cancelable(false)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                System.exit(0);
                            }
                        })
                        .show();
            }
        }

    }
}
