package fr.klemek.quotebox.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import fr.klemek.quotebox.R;

/**
 * Created by klemek on 17/03/17 !
 */

@SuppressWarnings("SameParameterValue")
public abstract class ConnectionUtils {

    /**
     * Check if there is a newer version and show version/welcome message if updated
     */
    public static void checkVersion(final Activity a){
        final Context ctx = a.getApplicationContext();
        SharedPreferences settings = ctx.getSharedPreferences(Constants.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();
        final boolean skipUpdate = settings.getBoolean(Constants.PREFS_SKIP_NEXT_UPDATE, false);
        if(isOnline(ctx) && !skipUpdate){
            AsyncGet task = new AsyncGet(ctx, new AsyncGetListener() {
                @Override
                public void taskFinished(String app_info) {
                    try {
                        JSONObject app_info_json = new JSONObject(app_info);
                        int lastVersion = app_info_json.getInt(Constants.JSON_VERSION);
                        if(lastVersion > Constants.VERSION){
                            Utils.debugLog(ConnectionUtils.class, "Update available");
                            //update available
                            final String updateUrl = app_info_json.getString(Constants.JSON_UPDATE_URL);
                            new MaterialDialog.Builder(a)
                                    .title(R.string.dialog_update_title)
                                    .content(R.string.dialog_update_content)
                                    .positiveText(R.string.dialog_yes)
                                    .neutralText(R.string.dialog_no)
                                    .negativeText(R.string.dialog_update_negative)
                                    .cancelable(true)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Uri uLink = Uri.parse(updateUrl);
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uLink);
                                            a.startActivity(intent);
                                            a.finish();
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            editor.putBoolean(Constants.PREFS_SKIP_NEXT_UPDATE, true);
                                            editor.apply();
                                        }
                                    })
                                    .show();
                        }else{
                            Utils.showWelcomeDialog(a);
                        }
                    } catch (JSONException e) {
                        Utils.debugLog(ConnectionUtils.class, "Error in reading app_info.json");
                    }
                }
            });
            task.execute(Constants.APP_INFO_URL);
        }else{
            Utils.showWelcomeDialog(a);
        }
    }

    public static String getServerData(String sUrl, HashMap<String, String> dataParams, Context ctx) {

        String result = null;
        URL url;
        int responseCode;

        if (isOnline(ctx)) {
            try {
                if(dataParams != null && !dataParams.isEmpty())
                    url = new URL(sUrl+"?"+getDataString(dataParams));
                else
                    url = new URL(sUrl);
                try {

                    Utils.debugLog(ConnectionUtils.class,"server get : "+url);

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    httpURLConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                    httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
                    httpURLConnection.setReadTimeout(10000);
                    httpURLConnection.setConnectTimeout(15000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(false);

                    responseCode = httpURLConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String line;
                        result = "";
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                        while ((line = br.readLine()) != null) {
                            result += line;
                        }
                        Utils.debugLog(ConnectionUtils.class,"server response : ("+responseCode+") "+ result);
                    }else{
                        Utils.debugLog(ConnectionUtils.class,"server response : ("+responseCode+")");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            Utils.debugLog(ConnectionUtils.class,"not connected");
        }
        return result;
    }

    private static String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void loadImage(Context ctx, String url, int placeholder, int error, ImageView view) {
        if (url != null && !url.equals("")) {
            Picasso.with(ctx).load(url).placeholder(placeholder).error(error).into(view);
        } else {
            Picasso.with(ctx).load(error).into(view);
        }
    }

    public static boolean downloadFile(String strUrl, String path){
        int count;
        try {
            Utils.debugLog(ConnectionUtils.class,"Downloading file from "+strUrl);

            File f = new File(path);
            if(!f.exists()) {

                URL url = new URL(strUrl);
                URLConnection conection = url.openConnection();
                conection.connect();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(path);

                byte data[] = new byte[1024];

                while ((count = input.read(data)) != -1) {

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                Utils.debugLog(ConnectionUtils.class,"File successfully downloaded");
            }else{
                Utils.debugLog(ConnectionUtils.class,"File already downloaded");
            }
            return true;
        } catch (Exception e) {
            Utils.debugLog(ConnectionUtils.class,e.getMessage());
            return false;
        }
    }

    public interface AsyncGetListener{
        void taskFinished(String result);
    }

    public static class AsyncGet extends AsyncTask<String, Void, String> {

        private final AsyncGetListener l;
        private final HashMap<String, String> params;
        private final Context ctx;

        public AsyncGet(Context ctx, AsyncGetListener l){
            this(ctx, null, l);
        }

        public AsyncGet(Context ctx, HashMap<String, String> params, AsyncGetListener l){
            this.l = l;
            this.params = params;
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... strings) {
            return getServerData(strings[0],params,ctx);
        }

        @Override
        protected void onPostExecute(String result) {
            l.taskFinished(result);
        }
    }
}
