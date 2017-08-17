package fr.klemek.quotetube.utils;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by klemek on 14/03/17 !
 */

public abstract class Constants {

    static final boolean DEBUG = true;

    public static final String APP_ID = "fr.klemek.quotetube";
    public static final String VERSION_ID = "Beta 1.4";
    public static final int VERSION = 1;
    public static final int LIST_VERSION = 2;

    public static final long MAX_QUOTE_DURATION = 20000L;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yy");

    public static final DecimalFormat numFormatter = new DecimalFormat("###,###,###,###,##0");

    public static final String LOG_ID = "QuoteTube";

    //Folders
    public static final String DIR_EXT_STORAGE = Environment.getExternalStorageDirectory().getPath();
    public static final String DIR_BASE = DIR_EXT_STORAGE +"/quotetube/";
    public static final String DIR_QUOTES = DIR_BASE+"quotes/";
    public static final String DIR_SCRIPTS = DIR_BASE+"scripts/";
    public static final String DIR_LOGS = DIR_BASE+"logs/";

    public static final String LIST_FILE = DIR_QUOTES+"list.json";

    public static final String GOOGLE_API_KEY = "AIzaSyDnJ7wPRMUMnW4kVmqZS3wEv23HXMbKOP4";

    //POST params
    public static final String GET_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    public static HashMap<String,String> GET_SEARCH_PARAMS(){
        HashMap<String,String> out = new HashMap<>();
        out.put("part","snippet");
        out.put("type","channel,video");
        out.put("order","relevance");
        out.put("relevanceLanguage", Locale.getDefault().getLanguage());
        out.put("maxResults","10");
        out.put("key",GOOGLE_API_KEY);
        return out;
    }

    public static final String PARAM_PAGETOKEN = "pageToken";
    public static final String PARAM_Q = "q";
    public static final String PARAM_CHANNEL = "channelId";
    public static final String PARAM_ORDER = "order";
    public static final String PARAM_ORDER_CHANNEL = "date";

    public static final String GET_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";

    public static HashMap<String,String> GET_VIDEO_PARAMS(){
        HashMap<String,String> out = new HashMap<>();
        out.put("part","contentDetails,statistics");
        out.put("maxResults","1");
        out.put("key",GOOGLE_API_KEY);
        return out;
    }

    public static final String PARAM_ID = "id";

    //Extras keys
    public static final String EXTRA_VIDEOID = "videoid";
    public static final String EXTRA_VIDEOINFO = "videoinfo";
    public static final String EXTRA_CHANNELID = "channelid";
    public static final String EXTRA_CHANNELTITLE = "channeltitle";
    public static final String EXTRA_QUOTECOLOR = "quotecolor";
    public static final String EXTRA_QUOTESTART = "quotestart";
    public static final String EXTRA_QUOTESTOP = "quotestop";
    public static final String EXTRA_QUOTEFADEOUT = "quotefadeout";
    public static final String EXTRA_QUOTETIME = "quotetime";
    public static final String EXTRA_QUOTENAME = "quotename";
    public static final String EXTRA_QUOTEID = "quoteid";

    //JSON Keys search
    public static final String JSON_NEXTPAGE = "nextPageToken";
    public static final String JSON_ITEMS = "items";

    //JSON Keys video
    public static final String JSON_KIND = "id.kind";
    public static final String JSON_KIND_VIDEO = "youtube#video";
    public static final String JSON_KIND_CHANNEL = "youtube#channel";
    public static final String JSON_VIDEOID = "id.videoId";
    public static final String JSON_VIDEOTITLE = "snippet.title";
    public static final String JSON_DURATION = "contentDetails.duration";
    public static final String JSON_VIEWCOUNT = "statistics.viewCount";
    public static final String JSON_LIKECOUNT = "statistics.likeCount";
    public static final String JSON_DISLIKECOUNT = "statistics.dislikeCount";
    public static final String JSON_CHANNELID = "snippet.channelId";
    public static final String JSON_CHANNELTITLE = "snippet.channelTitle";
    public static final String JSON_DESCRIPTION = "snippet.description";
    public static final String JSON_THUMBURL = "snippet.thumbnails.medium.url";
    public static final String JSON_PUBLISHEDAT = "snippet.publishedAt";

    //QPython API

    public static final String QPYTHON_DL_URL = "https://klemek.fr/quotetube/qpython1.2.5.apk";
    public static final String QPYTHON_DL_PATH = DIR_BASE + "/qpython1.2.5.apk";

    public static final long MAX_QPY_WAIT = 10 * 1000; //20 sec timeout

    public final static String QPYTHON_PACKAGE = "com.hipipal.qpyplus";//"org.qpython.qpy";
    public static final int QPYTHON_REQUIRED_VERSION = 127;

    public static final String QPYTHON_DEFAULT_LOG_FILE = DIR_EXT_STORAGE + "/" + QPYTHON_PACKAGE + "/.run/.run.log";//"/qpython/.run/.run.log";

    public final static String QPYTHON_CLASS = "com.hipipal.qpyplus.MPyApi";//"org.qpython.qpylib.MPyApi";
    public final static String QPYTHON_ACTION = QPYTHON_PACKAGE + ".action.MPyApi";
    public final static String QPYTHON_BUNDLE_ACT = "onPyApi";
    public final static String QPYTHON_BUNDLE_FLAG = "onQPyExec";
    public final static String QPYTHON_SCRIPT_HEADER = "#qpy:qpyapp\n";
    public static final String QPY_LOG_END_FLAG = "itisdone";
    public static final String QPY_LOG_ERROR_FLAG = "itiserror";
    public static final String FILE_TIMEOUT_MSG = "timeout";

    //Python scripts

    /*
    https://github.com/rg3/youtube-dl/blob/master/README.md#embedding-youtube-dl
     */

    public static final String QPY_SCRIPT_YTDL_CHECK_PATH = DIR_SCRIPTS+"ytdl_check.py";
    public static final String QPY_SCRIPT_YTDL_CHECK = "" +
            "#qpy:qpyapp\n" +
            "try:\n" +
            "    import youtube_dl\n"+
            "    print('done')\n" +
            "except:\n" +
            "    print('not found')";

    public static final String SCRIPT_YTDL_UPGR_PATH = DIR_SCRIPTS+"ytdl_upgrade.py";
    public static final String QPY_SCRIPT_YTDL_UPGR = "" +
            "#qpy:qpyapp\n" +
            "try:\n" +
            "    import pip\n" +
            "    pip.main(['install','--upgrade','youtube_dl'])\n" +
            "    print('itisdone')\n" +
            "except:\n" +
            "    print('itiserror')\n";

    public static final String SCRIPT_YTDL_INST_PATH = DIR_SCRIPTS+"ytdl_install.py";
    public static final String QPY_SCRIPT_YTDL_INST = "" +
            "#qpy:qpyapp\n" +
            "try:\n" +
            "    import pip\n" +
            "    pip.main(['install','youtube_dl'])\n" +
            "    print('itisdone')\n" +
            "except:\n" +
            "    print('itiserror')\n";

    public static final String QPY_TEMP_SCRIPT_PATH = DIR_SCRIPTS+"temp.py";
    public static final String QPY_FIRST_SCRIPT = "" +
            "#qpy:console";
    public final static String QPY_INIT_SCRIPT = "" +
            "#qpy:qpyapp\n" +
            "print('itisdone')";

    public static final String QPY_SCRIPT_YTDL_VIDEO_PATH = DIR_SCRIPTS+"ytdl.py";
    public static final String QPY_SCRIPT_YTDL_VIDEO = "" +
            "from __future__ import unicode_literals\n" +
            "#qpy:qpyapp\n" +
            "try:\n" +
            "    import youtube_dl\n" +
            "    ydl_opts = {\n" +
            "        'format': 'bestaudio/best',\n" +
            "        'outtmpl' : '"+DIR_QUOTES+"temp.%(ext)s',\n" +
            "        'no-continue':'',\n" +
            "    }\n" +
            "    with youtube_dl.YoutubeDL(ydl_opts) as ydl:\n" +
            "        ydl.download(['https://www.youtube.com/watch?v=%VIDEOID%'])\n" +
            "    print('itisdone')\n" +
            "except:\n" +
            "    print('itiserror')\n";

    public static final String QPY_SCRIPT_TAG_VIDEOID = "%VIDEOID%";

    //Links
    public static final String QPYTHON_MARKET = "market://details?id=org.qpython.qpy";
    public static final String QPYTHON_WEBSITE = "http://qpython.com";

    //FFmpeg

    public static final String FFMPEG_DEFAULT_EXT = ".webm";
    public static final String FFMPEG_CONVERT = "-y -i %s %s";
    public static final String FFMPEG_CUT = "-y -i %s -ss %s -t %s -strict -2%s %s";
    public static final String FFMPEG_FADE_OUT = " -af afade=t=out:st=%s:d=1";

    //Errors

    public static final int ERROR_FFMPEG_CUT = 11;
    public static final int ERROR_FFMPEG_EXTRACT = 12;
    public static final int ERROR_FFMPEG_RUNNING = 13;
    public static final int ERROR_YTDL = 21;
    public static final int ERROR_YTFRAG_INIT = 31;
    public static final int ERROR_YTFRAG = 32;
    public static final int ERROR_QPY_TIMEOUT = 41;

}
