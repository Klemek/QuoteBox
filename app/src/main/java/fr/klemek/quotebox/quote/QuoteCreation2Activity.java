package fr.klemek.quotebox.quote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import java.util.Random;

import fr.klemek.quotebox.R;
import fr.klemek.quotebox.utils.Constants;

/**
 * Created by klemek on ? !
 */

public class QuoteCreation2Activity extends AppCompatActivity implements ColorPickerDialogListener{

    private int tquoteStart, tquoteStop, tquoteDuration, quote_color;
    private String videoId;
    private String[] videoInfo;

    private static final int QUOTE_CREATION_RESULT= 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_creation2);

        setTitle(R.string.title_activity_quote_creation2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        videoId = getIntent().getStringExtra(Constants.EXTRA_VIDEOID);
        videoInfo = getIntent().getStringArrayExtra(Constants.EXTRA_VIDEOINFO);
        tquoteStart = getIntent().getIntExtra(Constants.EXTRA_QUOTESTART,0);
        tquoteStop = getIntent().getIntExtra(Constants.EXTRA_QUOTESTOP,1);
        tquoteDuration = getIntent().getIntExtra(Constants.EXTRA_QUOTETIME,1000);

        Random r = new Random();
        quote_color = Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
        ((ImageView) findViewById(R.id.quote_image)).setColorFilter(quote_color);

        findViewById(R.id.button_quote_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quotename = ((EditText)findViewById(R.id.quote_name_preview)).getText().toString();
                if(!quotename.equals("")) {
                    Intent i = new Intent(QuoteCreation2Activity.this, QuoteFactoryActivity.class);
                    i.putExtra(Constants.EXTRA_VIDEOID, videoId);
                    i.putExtra(Constants.EXTRA_VIDEOINFO, videoInfo);
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
            }
        });

        findViewById(R.id.quote_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

                ColorPickerDialog.newBuilder().setColor(quote_color).show(QuoteCreation2Activity.this);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
}
