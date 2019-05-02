package fr.klemek.quotebox.quote;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import fr.klemek.quotebox.R;
import fr.klemek.quotebox.utils.Constants;
import fr.klemek.quotebox.utils.DataManager;
import fr.klemek.quotebox.utils.FileUtils;
import fr.klemek.quotebox.utils.Utils;

public class QuoteEditionActivity extends AppCompatActivity  implements ColorPickerDialogListener {

    private Quote quote;
    private QuoteList quotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_edition);

        setTitle(R.string.title_activity_quote_edition);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final int quoteId = getIntent().getIntExtra(Constants.EXTRA_QUOTEID,-1);
        quotes = DataManager.getInstance().getQuoteList();
        quote = quotes.get(quoteId);
        if(quote == null){
            finish();
        }else{
            ((ImageView) findViewById(R.id.quote_image)).setColorFilter(quote.getColor());
            ((TextView)findViewById(R.id.quote_name_preview)).setText(quote.getName());
            final String[] videoInfo = quote.getVideoInfo();
            ((TextView)findViewById(R.id.video_info)).setText(Utils.fromHtml(getResources().getString(R.string.video_info,videoInfo[1],videoInfo[2],videoInfo[3])));

            findViewById(R.id.button_quote_delete).setOnClickListener(view -> new MaterialDialog.Builder(QuoteEditionActivity.this)
                .title(R.string.dialog_delete_quote_title)
                .content(getResources().getString(R.string.dialog_delete_quote_content,quote.getName()))
                .positiveText(R.string.dialog_yes)
                .negativeText(R.string.dialog_no)
                .cancelable(true)
                .onPositive((dialog, which) -> {
                    quotes.remove(quoteId);
                    FileUtils.tryDelete(quote.getFile().getAbsolutePath());
                    DataManager.getInstance().saveList();
                    finish();
                })
                .show());

            findViewById(R.id.button_quote_save).setOnClickListener(view -> {

                String quotename = ((EditText)findViewById(R.id.quote_name_preview)).getText().toString();
                if(!quotename.equals("")) {
                    quote.setName(quotename);
                    DataManager.getInstance().saveList();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.error_quote_name, Toast.LENGTH_SHORT).show();
                }
            });

            findViewById(R.id.quote_preview).setOnClickListener(view -> {

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

                ColorPickerDialog.newBuilder().setColor(quote.getColor()).show(QuoteEditionActivity.this);
            });

            findViewById(R.id.button_video_goto).setOnClickListener(view -> {
                if(videoInfo[0] != null && videoInfo[0].length()>0){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoInfo[0]));
                    startActivity(browserIntent);
                }else{
                    Toast.makeText(getApplicationContext(),R.string.error_no_videoinfo,Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        quote.setColor(color);
        ((ImageView) findViewById(R.id.quote_image)).setColorFilter(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}
