package fr.klemek.quotebox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import fr.klemek.quotebox.BuildConfig;
import fr.klemek.quotebox.R;

/**
 * Created by klemek on 16/03/17 !
 */

public abstract class Utils {

    /**
     * Show version/welcome message if updated or first time
     */
    public static void showWelcomeDialog(Activity a){
        Context ctx = a.getApplicationContext();
        SharedPreferences settings = ctx.getSharedPreferences(Constants.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();
        int version = settings.getInt(Constants.PREFS_VERSION, 0);
        Utils.debugLog(ConnectionUtils.class, "Last version : "+version + " " + "Running version : " + BuildConfig.VERSION_CODE);
        if(version == 0){
            Utils.debugLog(ConnectionUtils.class, "Welcome message");
            //Show welcome message
            new MaterialDialog.Builder(a)
                    .title(R.string.dialog_welcome_title)
                    .content(R.string.dialog_welcome_content)
                    .positiveText(R.string.dialog_welcome_positive)
                    .cancelable(true)
                    .show();
        }else if(version < BuildConfig.VERSION_CODE){
            editor.putBoolean(Constants.PREFS_SKIP_NEXT_UPDATE, false);
            Utils.debugLog(ConnectionUtils.class, "Changelog message");
            //Show update message
            new MaterialDialog.Builder(a)
                    .title(R.string.dialog_updated_title)
                    .content(R.string.dialog_updated_content)
                    .positiveText(R.string.dialog_close)
                    .cancelable(true)
                    .show();
        }
        editor.putInt(Constants.PREFS_VERSION, BuildConfig.VERSION_CODE);
        editor.apply();
    }

    public static void debugLog(AsyncTask a, String msg){
        debugLog(a.getClass(),msg);
    }

    public static void debugLog(Activity a, String msg){
        debugLog(a.getClass(),msg);
    }

    public static void debugLog(Class c,String msg){
        if(Constants.DEBUG)
            Log.d(Constants.LOG_ID+":"+c.getSimpleName(),"\t"+msg);
    }

    public static void errorLog(AsyncTask a,String msg, Throwable e){
        errorLog(a.getClass(),msg,e);
    }

    public static void errorLog(Activity a,String msg, Throwable e){
        errorLog(a.getClass(),msg,e);
    }

    private static void errorLog(Class c, String msg, Throwable e){
        Log.e(Constants.LOG_ID+":"+c.getSimpleName(),"\t"+msg+"\n"+e.getMessage());
    }

    public static String JSONgetString(JSONObject obj,String path) throws JSONException {
        String[] names = path.split("\\.");
        if(path.length()==1){
            return obj.getString(path);
        }else{
            JSONObject obj2 = obj.getJSONObject(names[0]);
            for(int i = 1; i < names.length-1; i++){
                obj2 = obj2.getJSONObject(names[i]);
            }
            return obj2.getString(names[names.length-1]);
        }

    }

    public static String formatDuration(long millis){
        return formatDuration(millis, false);
    }

    public static String formatDuration(long millis, boolean showMillis) {
        long seconds = millis/1000;
        if(seconds/3600>0){
            if(showMillis) {
                return String.format(Locale.getDefault(),
                        "%d:%02d:%02d.%03d",
                        seconds / 3600,
                        (seconds % 3600) / 60,
                        seconds % 60,
                        millis%1000);
            }else{
                return String.format(Locale.getDefault(),
                        "%d:%02d:%02d",
                        seconds / 3600,
                        (seconds % 3600) / 60,
                        seconds % 60);
            }
        }else{
            if(showMillis) {
                return String.format(Locale.getDefault(),
                        "%02d:%02d.%03d",
                        (seconds % 3600) / 60,
                        seconds % 60,
                        millis%1000);
            }else {
                return String.format(Locale.getDefault(),
                        "%02d:%02d",
                        seconds / 60,
                        seconds % 60);
            }
        }
    }

    public static String formatDurationInSeconds(long millis){
        long seconds = millis/1000;
        return String.format(Locale.getDefault(),
                "%02d.%03d",
                seconds,
                millis%1000);
    }

    public static int getLineCount(TextView tv, String text, int maxWidth){
        Rect bounds = new Rect();
        Paint textPaint = tv.getPaint();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        return (int) Math.ceil(bounds.width()/(double)maxWidth);
    }

    public static long getDuration(String sduration) {
        String time = sduration.substring(2);
        long duration = 0L;
        Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
        for (Object[] index1 : indexs) {
            int index = time.indexOf((String) index1[0]);
            if (index != -1) {
                String value = time.substring(0, index);
                duration += Integer.parseInt(value) * (int) index1[1] * 1000;
                time = time.substring(value.length() + 1);
            }
        }
        return duration;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(source);
        }
    }


}
