package fr.klemek.quotebox.youtube;

import android.content.Intent;
import android.database.MatrixCursor;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import fr.klemek.quotebox.R;
import fr.klemek.quotebox.quote.QuoteCreationActivity;
import fr.klemek.quotebox.utils.ConnectionUtils;
import fr.klemek.quotebox.utils.Constants;
import fr.klemek.quotebox.utils.Utils;

public class YoutubeSearchActivity extends AppCompatActivity {

    private YoutubeSearchAdapter adapter;
    private ArrayList<YoutubeElement> elements;
    private ProgressBar progress;
    private TextView hint;

    private boolean openSearch;

    private String channelId, channelTitle;

    private String queryText;

    private String[] suggestions;
    private SimpleCursorAdapter mAdapter;
    private ConnectionUtils.AsyncGet suggestTask;

    private static final int QUOTE_CREATION_RESULT= 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setTitle(R.string.title_activity_search);

        elements = new ArrayList<>();

        adapter = new YoutubeSearchAdapter(getApplicationContext(), elements, new YoutubeSearchAdapter.LoadRequestListener() {
            @Override
            public void onLoadRequest() {
                new AsyncLoad().execute(adapter.getNextPageToken());
            }
        });

        ListView listview = (ListView) findViewById(R.id.youtube_video_list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i<elements.size()) {
                    YoutubeElement e = elements.get(i);
                    Intent intent = null;
                    switch(e.getType()){
                        case VIDEO:
                            intent = new Intent(YoutubeSearchActivity.this, QuoteCreationActivity.class);
                            intent.putExtra(Constants.EXTRA_VIDEOID,((YoutubeVideo)e).getVideoId());
                            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                            String[] videoInfo = new String[]{((YoutubeVideo) e).getVideoId(),
                                    ((YoutubeVideo)e).getVideoTitle(),
                                    e.getChannelTitle(),
                                    dateFormat.format(((YoutubeVideo)e).getPublishedAt())};
                            intent.putExtra(Constants.EXTRA_VIDEOINFO, videoInfo);
                            break;
                        case CHANNEL:
                            intent = new Intent(YoutubeSearchActivity.this, YoutubeSearchActivity.class);
                            intent.putExtra(Constants.EXTRA_CHANNELID,e.getChannelId());
                            intent.putExtra(Constants.EXTRA_CHANNELTITLE,e.getChannelTitle());
                            break;
                    }
                    startActivityForResult(intent,QUOTE_CREATION_RESULT);
                }

            }
        });

        progress = (ProgressBar)findViewById(R.id.search_progress);
        progress.setIndeterminate(true);
        progress.setVisibility(View.GONE);
        hint = (TextView)findViewById(R.id.search_hint);

        queryText = "";

        if(getIntent().hasExtra(Constants.EXTRA_CHANNELID)){
            channelId = getIntent().getStringExtra(Constants.EXTRA_CHANNELID);
            channelTitle = getIntent().getStringExtra(Constants.EXTRA_CHANNELTITLE);
            setTitle(channelTitle);
            openSearch = false;
            progress.setVisibility(View.VISIBLE);
            hint.setVisibility(View.GONE);
            new AsyncLoad().execute();
        }else{
            openSearch = true;
        }

        final String[] from = new String[] {"suggestion"};
        final int[] to = new int[] {R.id.text_entry};
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.simple_text_item,
                null,
                from,
                to,
                CursorAdapter.NO_SELECTION);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == QUOTE_CREATION_RESULT && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.youtube_search_actions, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        if(openSearch)
            searchItem.expandActionView();
        final SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(queryText,false);
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {
                if(suggestions != null && position < suggestions.length){
                    searchView.setQuery(suggestions[position], true);
                    hint.setVisibility(View.GONE);
                }
                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }
        });

        searchView.setSuggestionsAdapter(mAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && query.length() > 0) {
                    progress.setVisibility(View.VISIBLE);
                    hint.setText("");
                    hint.setVisibility(View.GONE);
                    elements.clear();
                    adapter.notifyDataSetChanged();
                    adapter.setNextPageToken(null);
                    queryText = query;
                    new AsyncLoad().execute();
                    if(channelTitle != null)
                        setTitle(channelTitle+" : "+queryText);
                    else
                        setTitle(queryText);
                    searchItem.collapseActionView();
                } else {
                    progress.setVisibility(View.GONE);
                    hint.setVisibility(View.VISIBLE);
                    hint.setText(R.string.youtube_search_hint1);
                    if(channelTitle != null)
                        setTitle(channelTitle);
                    else
                        setTitle(R.string.title_activity_search);
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.changeCursor(null);
                if(suggestTask != null && suggestTask.getStatus() == AsyncTask.Status.RUNNING){
                    suggestTask.cancel(true);
                    Utils.debugLog(YoutubeSearchActivity.this, "Suggestion get canceled before result");
                }
                if(newText.length()>0){
                    HashMap<String, String> params = Constants.GET_SUGGEST_PARAMS();
                    params.put(Constants.PARAM_QUERY,newText);
                    suggestTask = new ConnectionUtils.AsyncGet(getApplicationContext(), params, new ConnectionUtils.AsyncGetListener() {
                        @Override
                        public void taskFinished(String result) {
                            try {
                                JSONArray jarray = new JSONArray(result).getJSONArray(1);
                                final MatrixCursor c = new MatrixCursor(new String[]{ BaseColumns._ID, "suggestion" });
                                suggestions = new String[Math.min(jarray.length(), Constants.MAX_SUGGESTIONS_COUNT)];
                                for(int i = 0; i  < suggestions.length; i++){
                                    suggestions[i] = Html.fromHtml(jarray.getString(i)).toString();
                                    c.addRow(new Object[] {i, suggestions[i]});
                                }
                                mAdapter.changeCursor(c);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    suggestTask.execute(Constants.GET_SUGGEST_URL);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private class AsyncLoad extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {

            HashMap<String,String> getParams = Constants.GET_SEARCH_PARAMS();

            if(strings.length>0 && strings[0] != null && strings[0].length()>0)
                getParams.put(Constants.PARAM_PAGETOKEN,strings[0]);

            if(channelId != null && channelId.length()>0) {
                getParams.put(Constants.PARAM_CHANNEL, channelId);
                getParams.put(Constants.PARAM_ORDER,Constants.PARAM_ORDER_CHANNEL);
            }

            if(queryText != null && queryText.length()>0)
                getParams.put(Constants.PARAM_Q,queryText);

            String result = ConnectionUtils.getServerData(Constants.GET_SEARCH_URL,getParams,getApplicationContext());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            if(result != null){
                try {
                    JSONObject obj = new JSONObject(result);
                    JSONArray items = obj.getJSONArray(Constants.JSON_ITEMS);
                    for(int i = 0; i < items.length(); i++){
                        JSONObject item = items.getJSONObject(i);
                        String kind = Utils.JSONgetString(item,Constants.JSON_KIND);
                        YoutubeElement ye = null;
                        if(kind.equals(Constants.JSON_KIND_VIDEO)) {
                            ye = new YoutubeVideo(Utils.JSONgetString(item, Constants.JSON_VIDEOID));
                            ((YoutubeVideo)ye).setVideoTitle(Utils.JSONgetString(item, Constants.JSON_VIDEOTITLE));
                            ((YoutubeVideo)ye).setPublishedAt(sdf.parse(Utils.JSONgetString(item, Constants.JSON_PUBLISHEDAT)));
                            HashMap<String,String> getParams2 = Constants.GET_VIDEO_PARAMS();
                            getParams2.put(Constants.PARAM_ID,((YoutubeVideo) ye).getVideoId());
                            String result2 = ConnectionUtils.getServerData(Constants.GET_VIDEO_URL,getParams2,getApplicationContext());
                            if(result2 != null){
                                JSONObject item2 = new JSONObject(result2).getJSONArray("items").getJSONObject(0);
                                ((YoutubeVideo) ye).setViews(Integer.parseInt(Utils.JSONgetString(item2,Constants.JSON_VIEWCOUNT)));
                                ((YoutubeVideo) ye).setUpvotes(Integer.parseInt(Utils.JSONgetString(item2,Constants.JSON_LIKECOUNT)));
                                ((YoutubeVideo) ye).setDownvotes(Integer.parseInt(Utils.JSONgetString(item2,Constants.JSON_DISLIKECOUNT)));
                                ((YoutubeVideo) ye).setDuration(Utils.getDuration(Utils.JSONgetString(item2,Constants.JSON_DURATION)));
                            }
                        }else if(kind.equals(Constants.JSON_KIND_CHANNEL)){
                            ye = new YoutubeChannel(Utils.JSONgetString(item, Constants.JSON_CHANNELID));
                        }

                        assert ye != null;
                        ye.setChannelId(Utils.JSONgetString(item, Constants.JSON_CHANNELID));
                        ye.setChannelTitle(Utils.JSONgetString(item, Constants.JSON_CHANNELTITLE));
                        ye.setThumbURL(Utils.JSONgetString(item, Constants.JSON_THUMBURL));
                        ye.setDescription(Utils.JSONgetString(item, Constants.JSON_DESCRIPTION));

                        elements.add(ye);
                    }
                    if(obj.has(Constants.JSON_NEXTPAGE)){
                        adapter.setNextPageToken(obj.getString(Constants.JSON_NEXTPAGE));
                    }else{
                        adapter.setNextPageToken(null);
                    }

                    return true;
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progress.setVisibility(View.GONE);
            if(result){
                if(elements.isEmpty()){
                    hint.setVisibility(View.VISIBLE);
                    hint.setText(R.string.youtube_search_hint3);
                }else{
                    hint.setVisibility(View.GONE);
                    hint.setText("");
                }
            }else{
                hint.setVisibility(View.VISIBLE);
                hint.setText(R.string.youtube_search_hint2);
            }
            adapter.notifyDataSetChanged();
        }
    }

}
