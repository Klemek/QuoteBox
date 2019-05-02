package fr.klemek.quotebox.quote;

import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import net.bohush.geometricprogressview.GeometricProgressView;

import java.util.Timer;

import fr.klemek.quotebox.R;
import fr.klemek.quotebox.utils.ConnectionUtils;
import fr.klemek.quotebox.utils.Constants;
import fr.klemek.quotebox.utils.DataManager;
import fr.klemek.quotebox.utils.FileUtils;
import fr.klemek.quotebox.utils.Utils;

/**
 * Created by klemek on ? !
 */

public class QuoteFactoryActivity extends AppCompatActivity {

    private static final int YTDL_SCRIPT_RESULT = 2;
    private int tquoteStart, tquoteStop, tquoteDuration, quotecolor;
    private boolean quotefadeout;
    private String videoid, quotename, soundfile;
    private WorkTask task;
    private FFmpeg ffmpeg;
    private int nangles = 12;
    private String[] videoInfo;
    private String tmpError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_factory);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        videoid = getIntent().getStringExtra(Constants.EXTRA_VIDEOID);
        videoInfo = getIntent().getStringArrayExtra(Constants.EXTRA_VIDEOINFO);
        quotename = getIntent().getStringExtra(Constants.EXTRA_QUOTENAME);
        ((TextView) findViewById(R.id.quote_name)).setText(quotename);
        //noinspection deprecation
        quotecolor = getIntent().getIntExtra(Constants.EXTRA_QUOTECOLOR, getResources().getColor(R.color.colorPrimary));
        ((ImageView) findViewById(R.id.quote_image)).setColorFilter(quotecolor);
        tquoteStart = getIntent().getIntExtra(Constants.EXTRA_QUOTESTART, 0);
        tquoteStop = getIntent().getIntExtra(Constants.EXTRA_QUOTESTOP, 1);
        quotefadeout = getIntent().getBooleanExtra(Constants.EXTRA_QUOTEFADEOUT, false);
        tquoteDuration = getIntent().getIntExtra(Constants.EXTRA_QUOTETIME, 1000);

        final GeometricProgressView progressView = findViewById(R.id.creation_progress);
        progressView.setType(GeometricProgressView.TYPE.TRIANGLE);
        progressView.setNumberOfAngles(nangles);
        progressView.setColor(quotecolor);
        progressView.setDuration(1000);
        progressView.setFigurePadding(getResources().getDimensionPixelOffset(R.dimen.figure_padding));

        findViewById(R.id.creation_quote).setOnClickListener(view -> {
            nangles++;
            progressView.setNumberOfAngles(nangles);
        });

        ffmpeg = FFmpeg.getInstance(getApplicationContext());

        soundfile = FileUtils.generateFileName(videoid, ".mp3");

        Timer timeout = new Timer();

        task = new WorkTask(WorkState.START);
        task.execute();
    }

    @Override
    public void onBackPressed() { //disable cancel
    }

    private enum WorkState {
        DONE(null, R.string.factory_done),
        CLEAN(DONE, R.string.factory_clean),
        CONVERT(CLEAN, R.string.factory_extract),
        CUT(CONVERT, R.string.factory_cutting),
        DOWNLOAD(CUT, R.string.factory_dl),
        START(DOWNLOAD, R.string.factory_start);

        final WorkState next;
        final int idtxt;

        WorkState(WorkState next, int txtid) {
            this.next = next;
            this.idtxt = txtid;
        }
    }

    private class WorkTask extends AsyncTask<Void, Integer, Integer> {

        private final WorkState state;

        WorkTask(WorkState state) {
            this.state = state;
        }

        @Override
        protected void onPreExecute() {
            ((TextView) findViewById(R.id.creating_info)).setText(state.idtxt);
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            if(values.length >= 1)
                ((TextView) findViewById(R.id.creating_info)).setText(getResources().getString(R.string.factory_dl2, values[0]));
        }

        @Override
        protected Integer doInBackground(Void... params) {
            switch (state) {
                case START:
                    return 0;
                case DOWNLOAD:
                    FileUtils.tryDelete(Constants.DIR_QUOTES + "temp");
                    if (ConnectionUtils.isOnline(getApplicationContext())) {
                        YoutubeDLRequest request = new YoutubeDLRequest("https://youtu.be/"+videoid);

                        request.setOption("--output", Constants.DIR_QUOTES + "temp");
                        request.setOption("--format", "bestaudio/best");
                        request.setOption("--no-continue");

                        try {
                            YoutubeDL.getInstance().execute(request, (progress, etaInSeconds) -> {
                                Utils.debugLog(this, "Downloading " + videoid + " ... " + progress + "% (ETA " + etaInSeconds + " seconds)");
                                publishProgress(Math.round(progress));
                            });
                            return 0;
                        } catch (YoutubeDLException e) {
                            Utils.errorLog(this, "Error Downloading " + videoid, e);
                            tmpError = e.getMessage();
                            return 2;
                        }
                    }
                    return 1;
                case CUT:
                    try {
                        FileUtils.tryDelete(Constants.DIR_QUOTES + "temp2.mp4");
                        String fade = "";
                        if (quotefadeout)
                            fade += String.format(Constants.FFMPEG_FADE_OUT,
                                    Utils.formatDurationInSeconds(tquoteStop - 1000));
                        String cmd = String.format(Constants.FFMPEG_CUT,
                                Constants.DIR_QUOTES + "temp",
                                Utils.formatDurationInSeconds(tquoteStart),
                                Utils.formatDurationInSeconds(tquoteDuration),
                                fade,
                                Constants.DIR_QUOTES + "temp2.mp4");

                        Utils.debugLog(this, "Cutting : ffmpeg " + cmd);
                        ffmpeg.execute(cmd.split(" "), new ExecuteBinaryResponseHandler() {

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onProgress(String message) {
                            }

                            @Override
                            public void onFailure(String message) {
                                Utils.debugLog(QuoteFactoryActivity.this, "Cut failed : " + message);
                                FileUtils.tryDelete(Constants.DIR_QUOTES + "temp");
                                FileUtils.tryDelete(Constants.DIR_QUOTES + "temp2.mp4");
                                new MaterialDialog.Builder(QuoteFactoryActivity.this)
                                        .title(R.string.error_generic_title)
                                        .content(getResources().getString(R.string.error_generic_content,
                                                Constants.ERROR_FFMPEG_CUT,
                                                message))
                                        .positiveText(R.string.dialog_ok)
                                        .cancelable(false)
                                        .onPositive((dialog, which) -> finish()).show();
                            }

                            @Override
                            public void onSuccess(String message) {
                                Utils.debugLog(QuoteFactoryActivity.this, "Cut success");
                                task = new WorkTask(state.next);
                                task.execute();
                            }

                            @Override
                            public void onFinish() {
                            }
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        Utils.debugLog(this, "Already running");
                        return 1;
                    }
                    return 0;
                case CONVERT:
                    try {
                        String cmd = String.format(Constants.FFMPEG_CONVERT,
                                Constants.DIR_QUOTES + "temp2.mp4",
                                Constants.DIR_QUOTES + soundfile);
                        Utils.debugLog(this, "Converting : ffmpeg " + cmd);
                        ffmpeg.execute(cmd.split(" "), new ExecuteBinaryResponseHandler() {

                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onProgress(String message) {
                            }

                            @Override
                            public void onFailure(String message) {
                                Utils.debugLog(QuoteFactoryActivity.this, "Conversion failed : " + message);
                                FileUtils.tryDelete(Constants.DIR_QUOTES + "temp");
                                FileUtils.tryDelete(Constants.DIR_QUOTES + "temp2.mp4");
                                FileUtils.tryDelete(Constants.DIR_QUOTES + soundfile);
                                new MaterialDialog.Builder(QuoteFactoryActivity.this)
                                        .title(R.string.error_generic_title)
                                        .content(getResources().getString(R.string.error_generic_content,
                                                Constants.ERROR_FFMPEG_EXTRACT,
                                                message))
                                        .positiveText(R.string.dialog_ok)
                                        .cancelable(false)
                                        .onPositive((dialog, which) -> finish()).show();
                            }

                            @Override
                            public void onSuccess(String message) {
                                Utils.debugLog(QuoteFactoryActivity.this, "Conversion success");
                                task = new WorkTask(state.next);
                                task.execute();
                            }

                            @Override
                            public void onFinish() {
                            }
                        });
                    } catch (FFmpegCommandAlreadyRunningException e) {
                        Utils.debugLog(this, "Already running");
                        return 1;
                    }
                    return 0;
                case CLEAN:
                    Utils.debugLog(this, "Cleaning...");
                    if (!FileUtils.tryDelete(Constants.DIR_QUOTES + "temp"))
                        Utils.debugLog(this, "Couldn't delete temp");
                    if (!FileUtils.tryDelete(Constants.DIR_QUOTES + "temp2.mp4"))
                        Utils.debugLog(this, "Couldn't delete temp2.mp4");
                    return 0;
                case DONE:
                    DataManager dm = DataManager.getInstance();
                    Quote q = new Quote(quotecolor, quotename, soundfile, videoInfo);
                    dm.getQuoteList().add(q);
                    dm.saveList();
                    return 0;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
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
                    if (result == 1) {
                        new MaterialDialog.Builder(QuoteFactoryActivity.this)
                                .title(R.string.error_nointernet_title)
                                .content(R.string.error_nointernet_content2)
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive((dialog, which) -> finish()).show();
                    }else if(result == 2){
                        new MaterialDialog.Builder(QuoteFactoryActivity.this)
                                .title(R.string.error_generic_title)
                                .content(getResources().getString(R.string.error_generic_content,
                                        Constants.ERROR_YOUTUBEDL_EXCEPTION,
                                        tmpError))
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive((dialog, which) -> finish()).show();
                    }else{
                        task = new WorkTask(state.next);
                        task.execute();
                    }
                    break;
                case DONE:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case CUT:
                case CONVERT:
                    if (result != 0)
                        new MaterialDialog.Builder(QuoteFactoryActivity.this)
                                .title(R.string.error_generic_title)
                                .content(getResources().getString(R.string.error_generic_content,
                                        Constants.ERROR_FFMPEG_RUNNING,
                                        ""))
                                .positiveText(R.string.dialog_ok)
                                .cancelable(false)
                                .onPositive((dialog, which) -> finish()).show();
                    break;
            }
        }
    }

}
