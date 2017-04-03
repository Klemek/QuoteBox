package fr.klemek.quotetube.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by klemek on 17/03/17.
 */

public abstract class ConnectionUtils {

    public static String getServerData(String sUrl, HashMap<String, String> dataParams, Context ctx) {

        String result = null;
        URL url;
        int responseCode = 0;

        if (isOnline(ctx)) {
            try {
                if(dataParams != null && !dataParams.isEmpty())
                    url = new URL(sUrl+"?"+getDataString(dataParams));
                else
                    url = new URL(sUrl);
                try {

                    Utils.debugLog(ConnectionUtils.class,"server get : "+url,0);

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setReadTimeout(10000);
                    httpURLConnection.setConnectTimeout(15000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(false);

                    responseCode = httpURLConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String line;
                        result = "";
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            result += line;
                        }
                        Utils.debugLog(ConnectionUtils.class,"server response : ("+responseCode+") "+result,0);
                    }else{
                        Utils.debugLog(ConnectionUtils.class,"server response : ("+responseCode+")",0);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            Utils.debugLog(ConnectionUtils.class,"not connected",0);
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
}
