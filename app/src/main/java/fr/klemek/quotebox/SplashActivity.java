package fr.klemek.quotebox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import java.io.File;
import java.io.IOException;

import fr.klemek.quotebox.utils.ConnectionUtils;
import fr.klemek.quotebox.utils.Constants;
import fr.klemek.quotebox.utils.DataManager;
import fr.klemek.quotebox.utils.Utils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by klemek on ? !
 */

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READWRITE_STORAGE = 1;
    private TextView info;
    private ProgressBar progress;
    private SplashTask task;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String tmpError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.debugLog(this, "QuoteBox " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") runnning on Android " + Build.VERSION.SDK_INT);
        setContentView(R.layout.activity_splash);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        info = findViewById(R.id.splash_info);
        progress = findViewById(R.id.splash_progress);
        progress.setMax(100);
        task = new SplashTask(LoadState.START);
        task.execute();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (task.getStatus() == AsyncTask.Status.PENDING)
            task.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READWRITE_STORAGE) {
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
                            .onPositive((dialog, which) -> System.exit(0))
                            .show();
                } else {
                    Utils.debugLog(this, "Permissions denied by user");
                    new MaterialDialog.Builder(SplashActivity.this)
                            .title(R.string.error_permission_title)
                            .content(R.string.error_permission_content)
                            .positiveText(R.string.error_permission_positive)
                            .negativeText(R.string.error_permission_negative)
                            .cancelable(false)
                            .onPositive((dialog, which) -> {
                                task = new SplashTask(LoadState.PERM_CHECK);
                                task.execute();
                            })
                            .onNegative((dialog, which) -> System.exit(0))
                            .show();
                }
            }
        }
    }

    private enum LoadState {
        DONE(100, R.string.splash_loading_done, null),
        DATA_LOAD(95, R.string.splash_loading_data, DONE),
        YTDL_UPDATE(70, R.string.splash_loading_ytdl2, DATA_LOAD),
        YTDL_INIT(60, R.string.splash_loading_ytdl, YTDL_UPDATE),
        FFMPEG_LOAD(10, R.string.splash_loading_ffmpeg, YTDL_INIT, DATA_LOAD),
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
                                        .onPositive((dialog, which) -> {
                                            task = new SplashTask(state.jump);
                                            task.execute();
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
                case YTDL_INIT:
                    try {
                        YoutubeDL.getInstance().init(getApplication());
                        return true;
                    } catch (YoutubeDLException e) {
                        Utils.errorLog(this, "failed to initialize youtubedl-android", e);
                        tmpError = e.getMessage();
                        return false;
                    }
                case YTDL_UPDATE:
                    if (!ConnectionUtils.isOnline(getApplicationContext())) {
                        task = new SplashTask(state.next);
                        task.execute();
                        return true;
                    }
                    Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(getApplication()))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(status -> {
                                Utils.debugLog(SplashActivity.this, "Youtube-DL update status : " + status.toString());
                                task = new SplashTask(state.next);
                                task.execute();
                            }, e -> {
                                Utils.errorLog(SplashActivity.this, "Youtube-DL update failed",e);
                                new MaterialDialog.Builder(SplashActivity.this)
                                        .title(R.string.error_ytdlfail_title)
                                        .content(R.string.error_ytdlfail_content)
                                        .positiveText(R.string.dialog_ok)
                                        .cancelable(false)
                                        .onPositive((dialog, which) -> System.exit(0))
                                        .show();
                            });
                    compositeDisposable.add(disposable);
                    return true;
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
                                .onPositive((dialog, which) -> {
                                    task = new SplashTask(state.jump);
                                    task.execute();
                                })
                                .show();
                        return;
                    }
                    break;
                case YTDL_INIT:
                    if (result) {
                        task = new SplashTask(state.next);
                        task.execute();
                    }
                    break;
                case YTDL_UPDATE:
                    return;
                case DIR_CHECK:
                    if (result) {
                        task = new SplashTask(state.next);
                        task.execute();
                    }else{
                        new MaterialDialog.Builder(SplashActivity.this)
                                .title(R.string.error_generic_title)
                                .content(getResources().getString(R.string.error_generic_content,
                                        Constants.ERROR_YOUTUBEDL_EXCEPTION,
                                        tmpError))
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive((dialog, which) -> System.exit(0)).show();
                        return;
                    }
                    break;
                case DONE:
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
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
                        .onPositive((dialog, which) -> System.exit(0))
                        .show();
            }
        }

    }
}
