package fr.klemek.quotetube.quote;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import java.util.Random;

import fr.klemek.quotetube.R;
import fr.klemek.quotetube.utils.Constants;
import fr.klemek.quotetube.utils.Utils;

public class QuoteEditorActivity extends AppCompatActivity implements ColorPickerDialogListener, YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener, YouTubePlayer.OnInitializedListener {

    private YouTubePlayer yp;
    private int tquoteStart, tquoteStop, tquoteDuration;
    private TextView quote_start, quote_stop, quote_duration, quote_current;

    private String videoId;

    private boolean ready;

    private final Handler handler = new Handler();


    private int quote_color;

    private boolean trying;

    private static final int QUOTE_CREATION_RESULT= 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_editor);

        setTitle(R.string.title_activity_quote_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        videoId = getIntent().getStringExtra(Constants.EXTRA_VIDEOID);

        tquoteStart = 0;
        tquoteDuration = 1000;
        tquoteStop = tquoteStart + tquoteDuration;

        quote_start = (TextView) findViewById(R.id.quote_start);
        quote_stop = (TextView) findViewById(R.id.quote_stop);
        quote_duration = (TextView) findViewById(R.id.quote_duration);
        quote_current = (TextView) findViewById(R.id.quote_current);

        update();

        Random r = new Random();
        quote_color = Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
        ((ImageView) findViewById(R.id.quote_image)).setColorFilter(quote_color);

        findViewById(R.id.button_quote_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    tquoteStart = yp.getCurrentTimeMillis();
                    if (tquoteStart > tquoteStop)
                        tquoteStop = Math.min(tquoteStart + tquoteDuration, yp.getDurationMillis());
                    else
                        tquoteDuration = tquoteStop - tquoteStart;
                    update();
                }
            }
        });

        findViewById(R.id.button_quote_tostart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekToMillis(tquoteStart);
                }
            }
        });

        findViewById(R.id.button_quote_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    tquoteStop = yp.getCurrentTimeMillis();
                    if (tquoteStart > tquoteStop)
                        tquoteStart = Math.max(tquoteStop - tquoteDuration, 0);
                    else
                        tquoteDuration = tquoteStop - tquoteStart;
                    update();
                }
            }
        });

        findViewById(R.id.button_quote_tostop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekToMillis(tquoteStop);
                }
            }
        });

        findViewById(R.id.button_back_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekRelativeMillis(-1000);
                    //yp.seekToMillis(Math.max(yp.getCurrentTimeMillis()-1000,0));
                }
            }
        });

        findViewById(R.id.button_back_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekRelativeMillis(-500);
                    //yp.seekToMillis(Math.max(yp.getCurrentTimeMillis()-1000,0));
                }
            }
        });

        findViewById(R.id.button_back_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekRelativeMillis(-200);
                    //yp.seekToMillis(Math.max(yp.getCurrentTimeMillis()-500,0));
                }
            }
        });

        findViewById(R.id.button_forw_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekRelativeMillis(200);
                    //yp.seekToMillis(Math.min(yp.getCurrentTimeMillis()+500,yp.getDurationMillis()));
                }
            }
        });

        findViewById(R.id.button_forw_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekRelativeMillis(500);
                    //yp.seekToMillis(Math.min(yp.getCurrentTimeMillis()+1000,yp.getDurationMillis()));
                }
            }
        });

        findViewById(R.id.button_forw_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    yp.seekRelativeMillis(1000);
                    //yp.seekToMillis(Math.min(yp.getCurrentTimeMillis()+1000,yp.getDurationMillis()));
                }
            }
        });

        findViewById(R.id.button_quote_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    new AsyncTry().execute();
                }else if(trying){
                    trying = false;
                }
            }
        });

        findViewById(R.id.button_quote_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ready && !trying) {
                    if (tquoteDuration <= Constants.MAX_QUOTE_DURATION) {
                        String quotename = ((EditText)findViewById(R.id.quote_name_preview)).getText().toString();
                        if(!quotename.equals(getString(R.string.default_quote_name))) {
                            Intent i = new Intent(QuoteEditorActivity.this, QuoteCreationActivity.class);
                            i.putExtra(Constants.EXTRA_VIDEOID, videoId);

                            i.putExtra(Constants.EXTRA_QUOTENAME, quotename);
                            i.putExtra(Constants.EXTRA_QUOTECOLOR, quote_color);
                            i.putExtra(Constants.EXTRA_QUOTESTART, tquoteStart);
                            i.putExtra(Constants.EXTRA_QUOTESTOP, tquoteStop);
                            i.putExtra(Constants.EXTRA_QUOTETIME, tquoteDuration);
                            i.putExtra(Constants.EXTRA_QUOTEFADEOUT, ((CheckBox)findViewById(R.id.quote_fade_out)).isChecked());
                            startActivityForResult(i, QUOTE_CREATION_RESULT);
                        }else{
                            Toast.makeText(getApplicationContext(), R.string.error_quote_name, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_quote_duration,(Constants.MAX_QUOTE_DURATION / 1000f)), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        findViewById(R.id.quote_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

                ColorPickerDialog.newBuilder().setColor(quote_color).show(QuoteEditorActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        YouTubePlayerSupportFragment frag =
                (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        frag.initialize(Constants.GOOGLE_API_KEY, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            getSupportActionBar().hide();
        }
        else{
            getSupportActionBar().show();
        }

    }

    private void update() {
        quote_start.setText(Utils.formatDuration(tquoteStart, true));
        quote_stop.setText(Utils.formatDuration(tquoteStop, true));
        quote_duration.setText(Utils.formatDuration(tquoteDuration, true));
        if (ready)
            quote_current.setText(Utils.formatDuration(yp.getCurrentTimeMillis(), true));
        else
            quote_current.setText(Utils.formatDuration(0, true));
        if (tquoteDuration <= 0 || tquoteDuration > Constants.MAX_QUOTE_DURATION) {
            quote_duration.setTextColor(getResources().getColor(R.color.textRed));
        } else {
            quote_duration.setTextColor(getResources().getColor(R.color.textGreen));
        }

    }

    @Override
    public void onLoading() {
    }

    @Override
    public void onLoaded(String s) {
        if(yp != null) {
            yp.pause();
            ready = true;
        }
    }

    @Override
    public void onAdStarted() {
    }

    @Override
    public void onVideoStarted() {
    }

    @Override
    public void onVideoEnded() {
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        Utils.debugLog(this, "YoutubeError:" + errorReason.name());
        new MaterialDialog.Builder(this)
                .title(R.string.error_generic_title)
                .content(getResources().getString(R.string.error_generic_content,
                        Constants.ERROR_YTFRAG,
                        errorReason.name()))
                .positiveText(R.string.dialog_ok)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        finish();
                    }
                }).show();
    }

    @Override
    public void onPlaying() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                quote_current.setText(Utils.formatDuration(yp.getCurrentTimeMillis(), true));
                if (yp.isPlaying()) {
                    handler.postDelayed(this, 200);
                } else {
                    handler.removeCallbacks(this);
                }
            }
        }, 200);
    }

    @Override
    public void onPaused() {
    }

    @Override
    public void onStopped() {
        ready = false;
    }

    @Override
    public void onBuffering(boolean b) {
    }

    @Override
    public void onSeekTo(int newPositionMillis) {
        quote_current.setText(Utils.formatDuration(newPositionMillis, true));
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        yp = youTubePlayer;
        if (videoId != null) {
            if (!wasRestored) {
                yp.loadVideo(videoId);
                yp.setPlaybackEventListener(this);
                yp.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                    @Override
                    public void onFullscreen(boolean b) {
                        yp.pause();
                    }
                });

                yp.setPlayerStateChangeListener(this);
            }
            yp.pause();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if(youTubeInitializationResult.isUserRecoverableError()){
            YouTubePlayerSupportFragment frag =
                    (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
            frag.initialize(Constants.GOOGLE_API_KEY, this);
        }else{
            new MaterialDialog.Builder(this)
                    .title(R.string.error_generic_title)
                    .content(getResources().getString(R.string.error_generic_content,
                            Constants.ERROR_YTFRAG_INIT,
                            ""))
                    .positiveText(R.string.dialog_ok)
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            finish();
                        }
                    }).show();
        }
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        quote_color = color;
        ((ImageView) findViewById(R.id.quote_image)).setColorFilter(quote_color);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == QUOTE_CREATION_RESULT && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        ready = false;
    }

    private class AsyncTry extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            yp.play();
            while (ready && yp.getCurrentTimeMillis() + 400 - tquoteStop < 0 && trying) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(!ready)
                return null;

            yp.pause();

            if(trying) {

                int delta = yp.getCurrentTimeMillis() - tquoteStop;

                if (Math.abs(delta) >= 100)
                    Utils.debugLog(this, "Not accurate stop : delta=" + delta + "ms");

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            if (yp.isPlaying()) {
                yp.pause();
            }
            trying = true;
            yp.seekToMillis(tquoteStart);
            ((Button)findViewById(R.id.button_quote_try)).setText(R.string.quote_try_2);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            trying = false;
            if(ready) {
                yp.seekToMillis(tquoteStart);
                ((Button)findViewById(R.id.button_quote_try)).setText(R.string.quote_try);
            }
        }
    }
}
