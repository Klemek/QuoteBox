package fr.klemek.quotetube;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import fr.klemek.quotetube.quote.Quote;
import fr.klemek.quotetube.quote.QuoteAdapter;
import fr.klemek.quotetube.quote.QuoteList;
import fr.klemek.quotetube.utils.Constants;
import fr.klemek.quotetube.utils.DataManager;
import fr.klemek.quotetube.utils.FileUtils;
import fr.klemek.quotetube.utils.Utils;
import fr.klemek.quotetube.youtube.YoutubeSearchActivity;

import static fr.klemek.quotetube.utils.Utils.debugLog;

public class MainActivity extends AppCompatActivity{

    private QuoteList quotes;
    private QuoteAdapter adapter;

    private ArrayList<MediaPlayer> players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setTitle(R.string.title_activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, YoutubeSearchActivity.class);
                //Intent i = new Intent(MainActivity.this, QuoteEditorActivity.class);
                //i.putExtra(Constants.EXTRA_VIDEOID,"s5-nUCSXKac");
                startActivity(i);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        quotes = DataManager.getInstance(getApplicationContext()).getQuoteList();

        players = new ArrayList<>();
        for(int i = 0; i < quotes.size(); i++)
            players.add(null);

        if(!quotes.isEmpty())
            findViewById(R.id.quote_list_empty_tv).setVisibility(View.GONE);

        adapter = new QuoteAdapter(this,quotes);

        final GridView gridview = (GridView) findViewById(R.id.quotelist);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View v,
                                    int position, long id) {
                Quote q = quotes.get(position);
                MediaPlayer mp = players.get(position);
                debugLog(MainActivity.this,q.getName());
                if(mp == null) {
                    mp = FileUtils.loadSound(q.getFile());
                    if(mp != null){
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.stop();
                            ((ImageView)v.findViewById(R.id.quote_icon)).setImageDrawable(getDrawable(R.drawable.quotes));
                        }
                    });
                    players.set(position, mp);}
                }else{
                    if(mp.isPlaying())
                        mp.stop();

                }
                mp.prepareAsync();
                ((ImageView)v.findViewById(R.id.quote_icon)).setImageDrawable(getDrawable(R.drawable.play));
            }
        });
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Quote q = quotes.get(i);
                final int i2 = i;
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.delete_quote_title)
                        .content(getResources().getString(R.string.delete_quote_content,q.getName()))
                                .positiveText(R.string.dialog_yes)
                                .negativeText(R.string.dialog_no)
                                .cancelable(true)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        quotes.remove(i2);
                                        adapter.notifyDataSetChanged();
                                        FileUtils.tryDelete(q.getFile().getAbsolutePath());
                                        DataManager.getInstance(getApplicationContext()).saveList(getApplicationContext());
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
                                    }
                                }).show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.about_title)
                        .content(getResources().getString(R.string.about_content,
                                Constants.VERSION_ID))
                        .negativeText(R.string.dialog_close)
                        .cancelable(true)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(players != null)
            for(MediaPlayer mp:players)
                if(mp != null)
                    mp.release();
    }
}
