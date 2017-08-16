package fr.klemek.quotetube.quote;

import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import net.bohush.geometricprogressview.GeometricProgressView;

import java.util.Timer;
import java.util.TimerTask;

import fr.klemek.quotetube.R;
import fr.klemek.quotetube.utils.ConnectionUtils;
import fr.klemek.quotetube.utils.Constants;
import fr.klemek.quotetube.utils.DataManager;
import fr.klemek.quotetube.utils.FileUtils;
import fr.klemek.quotetube.utils.QPyUtils;
import fr.klemek.quotetube.utils.Utils;

/**
 * Created by klemek on ? !
 */

public class QuoteCreationActivity extends AppCompatActivity implements QPyUtils.OnQPyResultListener{

    private int tquoteStart, tquoteStop, tquoteDuration, quotecolor;
    private boolean quotefadeout;
    private String videoid, quotename, dlext, soundfile;
    private WorkTask task;
    private FFmpeg ffmpeg;
    private int nangles = 12;
    private Timer timeout;

    private static final int YTDL_SCRIPT_RESULT= 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_creation);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        videoid = getIntent().getStringExtra(Constants.EXTRA_VIDEOID);
        quotename = getIntent().getStringExtra(Constants.EXTRA_QUOTENAME);
        ((TextView)findViewById(R.id.quote_name)).setText(quotename);
        quotecolor = getIntent().getIntExtra(Constants.EXTRA_QUOTECOLOR,getResources().getColor(R.color.colorPrimary));
        ((ImageView) findViewById(R.id.quote_image)).setColorFilter(quotecolor);
        tquoteStart = getIntent().getIntExtra(Constants.EXTRA_QUOTESTART,0);
        tquoteStop = getIntent().getIntExtra(Constants.EXTRA_QUOTESTOP,1);
        quotefadeout =getIntent().getBooleanExtra(Constants.EXTRA_QUOTEFADEOUT,false);
        tquoteDuration = getIntent().getIntExtra(Constants.EXTRA_QUOTETIME,1000);

        final GeometricProgressView progressView = (GeometricProgressView) findViewById(R.id.creation_progress);
        progressView.setType(GeometricProgressView.TYPE.TRIANGLE);
        progressView.setNumberOfAngles(nangles);
        progressView.setColor(quotecolor);
        progressView.setDuration(1000);
        progressView.setFigurePadding(getResources().getDimensionPixelOffset(R.dimen.figure_padding));

        findViewById(R.id.creation_quote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nangles++;
                progressView.setNumberOfAngles(nangles);
            }
        });

        ffmpeg =  FFmpeg.getInstance(getApplicationContext());

        soundfile = FileUtils.generateFileName(videoid,".mp3");

        timeout = new Timer();

        task = new WorkTask(WorkState.START);
        task.execute();
    }

    private enum WorkState {
        DONE(null,R.string.creation_done),
        CLEAN(DONE,R.string.creation_clean),
        CONVERT(CLEAN,R.string.creation_extract),
        CUT(CONVERT,R.string.creation_cutting),
        DOWNLOAD(CUT,R.string.creation_dl),
        START(DOWNLOAD,R.string.creation_start);

        WorkState next;
        int idtxt;

        WorkState(WorkState next, int txtid){
            this.next = next;
            this.idtxt = txtid;
        }
    }

    private class WorkTask extends AsyncTask<Void, Void, Boolean> {

        private WorkState state;

        WorkTask(WorkState state){
            this.state = state;
        }

        @Override
        protected void onPreExecute(){
            ((TextView)findViewById(R.id.creating_info)).setText(state.idtxt);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            switch (state) {
                case START:
                    return true;
                case DOWNLOAD:
                    FileUtils.tryDelete(Constants.DIR_QUOTES+"temp"+Constants.FFMPEG_DEFAULT_EXT);
                    if(ConnectionUtils.isOnline(getApplicationContext())) {
                        String script = Constants.QPY_SCRIPT_DL_VIDEO.replace(Constants.QPY_SCRIPT_TAG_VIDEOID,videoid);
                        Utils.debugLog(this,"Creating script file");
                        if(FileUtils.writeFile(Constants.SCRIPT_YTDL_PATH,script,true)) {
                            Utils.debugLog(this, "Downloading "+videoid+" ...");
                            QPyUtils.QPyExecFile(YTDL_SCRIPT_RESULT, QuoteCreationActivity.this, Constants.SCRIPT_YTDL_PATH);

                            timeout.schedule(new TimerTask(){
                                @Override
                                public void run() {
                                    new MaterialDialog.Builder(QuoteCreationActivity.this)
                                            .title(R.string.error_generic_title)
                                            .content(getResources().getString(R.string.error_generic_content,
                                                    Constants.ERROR_QPY_TIMEOUT,
                                                    ""))
                                            .positiveText(R.string.dialog_ok)
                                            .cancelable(false)
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    finish();
                                                }
                                            }).show();
                                }
                            }, Constants.MAX_QPY_WAIT);
                            return true;
                        }else{
                            Utils.debugLog(this,"Could not create script file");
                        }
                    }
                    return false;
                case CUT:
                    try {
                        FileUtils.tryDelete(Constants.DIR_QUOTES+"temp2"+Constants.FFMPEG_DEFAULT_EXT);
                        String fade = "";
                        if(quotefadeout)
                            fade+=String.format(Constants.FFMPEG_FADE_OUT,
                                    Utils.formatDurationInSeconds(tquoteStop-1000));
                        String cmd = String.format(Constants.FFMPEG_CUT,
                                Constants.DIR_QUOTES+"temp"+dlext,
                                Utils.formatDurationInSeconds(tquoteStart),
                                Utils.formatDurationInSeconds(tquoteDuration),
                                fade,
                                Constants.DIR_QUOTES+"temp2"+dlext);

                        Utils.debugLog(this,"Cutting : ffmpeg "+cmd);
                        ffmpeg.execute(cmd.split(" "), new ExecuteBinaryResponseHandler() {

                            @Override
                            public void onStart() {}

                            @Override
                            public void onProgress(String message) {}

                            @Override
                            public void onFailure(String message) {
                                Utils.debugLog(this,"Cut failed : "+message);
                                FileUtils.tryDelete(Constants.DIR_QUOTES+"temp"+dlext);
                                FileUtils.tryDelete(Constants.DIR_QUOTES+"temp2"+dlext);
                                new MaterialDialog.Builder(QuoteCreationActivity.this)
                                        .title(R.string.error_generic_title)
                                        .content(getResources().getString(R.string.error_generic_content,
                                                                Constants.ERROR_FFMPEG_CUT,
                                                                message))
                                        .positiveText(R.string.dialog_ok)
                                        .cancelable(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                finish();
                                            }
                                        }).show();
                            }

                            @Override
                            public void onSuccess(String message) {
                                Utils.debugLog(this,"Cut success");
                                task = new WorkTask(state.next);
                                task.execute();
                            }

                            @Override
                            public void onFinish() {}
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        Utils.debugLog(this,"Already running");
                        return false;
                    }
                    return true;
                case CONVERT:
                    try {
                        String cmd = String.format(Constants.FFMPEG_CONVERT,
                                Constants.DIR_QUOTES+"temp2"+dlext,
                                Constants.DIR_QUOTES+soundfile);
                        Utils.debugLog(this,"Converting : ffmpeg "+cmd);
                        ffmpeg.execute(cmd.split(" "), new ExecuteBinaryResponseHandler() {

                            @Override
                            public void onStart() {}

                            @Override
                            public void onProgress(String message) {}

                            @Override
                            public void onFailure(String message) {
                                Utils.debugLog(this,"Conversion failed : "+message);
                                FileUtils.tryDelete(Constants.DIR_QUOTES+"temp"+dlext);
                                FileUtils.tryDelete(Constants.DIR_QUOTES+"temp2"+dlext);
                                FileUtils.tryDelete(Constants.DIR_QUOTES+soundfile);
                                new MaterialDialog.Builder(QuoteCreationActivity.this)
                                        .title(R.string.error_generic_title)
                                        .content(getResources().getString(R.string.error_generic_content,
                                                Constants.ERROR_FFMPEG_EXTRACT,
                                                message))
                                        .positiveText(R.string.dialog_ok)
                                        .cancelable(false)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                finish();
                                            }
                                        }).show();
                            }

                            @Override
                            public void onSuccess(String message) {
                                Utils.debugLog(this,"Conversion success");
                                task = new WorkTask(state.next);
                                task.execute();
                            }

                            @Override
                            public void onFinish() {}
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        Utils.debugLog(this,"Already running");
                        return false;
                    }
                    return true;
                case CLEAN:
                    Utils.debugLog(this,"Cleaning...");
                    if(!FileUtils.tryDelete(Constants.DIR_QUOTES+ "temp"+dlext))
                        Utils.debugLog(this,"Couldn't delete temp"+ dlext);
                    if(!FileUtils.tryDelete(Constants.DIR_QUOTES+ "temp2"+dlext))
                        Utils.debugLog(this,"Couldn't delete temp2"+ dlext);
                    return true;
                case DONE:
                    DataManager dm = DataManager.getInstance(getApplicationContext());
                    Quote q = new Quote(quotecolor,quotename,soundfile);
                    dm.getQuoteList().add(q);
                    dm.saveList(getApplicationContext());
                    return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            switch (state) {
                case START:
                    task = new WorkTask(state.next);
                    task.execute();
                    break;
                case CLEAN:
                    task = new WorkTask(state.next);
                    task.execute();
                    break;
                case DOWNLOAD:
                    if(!result){
                        new MaterialDialog.Builder(QuoteCreationActivity.this)
                                .title(R.string.error_nointernet_title)
                                .content(R.string.error_nointernet_content2)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        finish();
                                    }
                                }).show();
                    }
                    break;
                case DONE:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case CUT:
                case CONVERT:
                    if(!result)
                        new MaterialDialog.Builder(QuoteCreationActivity.this)
                                .title(R.string.error_generic_title)
                                .content(getResources().getString(R.string.error_generic_content,
                                        Constants.ERROR_FFMPEG_RUNNING,
                                        ""))
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        finish();
                                    }
                                }).show();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case YTDL_SCRIPT_RESULT:
                QPyUtils.getResult(YTDL_SCRIPT_RESULT,this,data,true);
                timeout.cancel();
                break;
        }
    }

    @Override
    public void onQPyResult(int requestCode, boolean success, String result) {
        switch (requestCode) {
            case YTDL_SCRIPT_RESULT:
                if(success) {
                    //[download] Destination: /storage/emulated/0/quotetube/quotes/temp.webm
                    dlext = result.split("quotetube/quotes/temp", 2)[1].split(" ",2)[0].split("\n", 2)[0].trim();
                    Utils.debugLog(this,"Donwloaded to temp"+ dlext);
                    task = new WorkTask(WorkState.DOWNLOAD.next);
                    task.execute();
                }else{
                    new MaterialDialog.Builder(QuoteCreationActivity.this)
                            .title(R.string.error_generic_title)
                            .content(getResources().getString(R.string.error_generic_content,
                                    Constants.ERROR_YTDL,
                                    result))
                            .positiveText(R.string.dialog_ok)
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    finish();
                                }
                            }).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() { //disable cancel
    }

}
