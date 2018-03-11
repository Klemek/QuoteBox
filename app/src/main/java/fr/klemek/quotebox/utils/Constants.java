package fr.klemek.quotebox.utils;

import android.os.Environment;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by klemek on 14/03/17 !
 */

public abstract class Constants {

    public static final String APP_ID = "fr.klemek.quotebox";
    public static final String VERSION_ID = "Beta 1.5.3";
    public static final int VERSION = 5;
    public static final int LIST_VERSION = 2;
    public static final String APP_INFO_URL = "https://www.klemek.fr/quotebox/app_info.json";
    public static final String JSON_VERSION = "version";
    public static final String JSON_UPDATE_URL = "update_url";
    public static final String PREFS_NAME = "fr.klemek.quotebox.prefs";
    public static final String PREFS_VERSION = "fr.klemek.quotebox.prefs.version";
    public static final String PREFS_SKIP_NEXT_UPDATE = "fr.klemek.quotebox.prefs.skip_update";
    public static final long MAX_QUOTE_DURATION = 20000L;
    public static final DecimalFormat numFormatter = new DecimalFormat("###,###,###,###,##0");
    public static final String LOG_ID = "QuoteBox";
    //Folders
    public static final String DIR_EXT_STORAGE = Environment.getExternalStorageDirectory().getPath();
    public static final String DIR_BASE = DIR_EXT_STORAGE +"/quotebox/";
    public static final String DIR_QUOTES = DIR_BASE+"quotes/";
    public static final String DIR_SCRIPTS = DIR_BASE+"scripts/";
    public static final String DIR_LOGS = DIR_BASE+"logs/";
    public static final String LIST_FILE = DIR_QUOTES+"list.json";
    public static final String GOOGLE_API_KEY = "AIzaSyDnJ7wPRMUMnW4kVmqZS3wEv23HXMbKOP4";
    //POST params
    public static final String GET_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    public static final String PARAM_PAGETOKEN = "pageToken";
    public static final String PARAM_Q = "q";
    public static final String PARAM_CHANNEL = "channelId";
    public static final String PARAM_ORDER = "order";
    public static final String PARAM_ORDER_CHANNEL = "date";
    public static final String GET_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos";
    public static final String PARAM_ID = "id";
    public static final String GET_SUGGEST_URL = "http://suggestqueries.google.com/complete/search";
    public static final String PARAM_QUERY = "q";
    public static final int MAX_SUGGESTIONS_COUNT = 5;
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
    public static final long MAX_QPY_WAIT = 5 * 1000; //5 sec timeout
    public static final String QPYTHON_MARKET = "market://details?id=org.qpython.qpy";
    public static final String QPYTHON_WEBSITE = "http://qpy.io/";
    public final static String QPYTHON_PACKAGE = "org.qpython.qpy";

    //QPython API
    public static final String QPYTHON_DEFAULT_LOG_FILE = DIR_EXT_STORAGE + "/log/last.log";//"/qpython/.run/.run.log";
    public final static String QPYTHON_CLASS = "org.qpython.qpylib.MPyApi";
    public final static String QPYTHON_ACTION = QPYTHON_PACKAGE + ".action.MPyApi";
    public final static String QPYTHON_BUNDLE_ACT = "onPyApi";
    public final static String QPYTHON_BUNDLE_FLAG = "onQPyExec";
    //public final static String QPYTHON_SCRIPT_HEADER = "#qpy:qpyapp\n";
    public static final String QPY_LOG_END_FLAG = "itisdone";
    public static final String QPY_LOG_ERROR_FLAG = "itiserror";
    public static final String FILE_TIMEOUT_MSG = "timeout";
    public static final String QPY_SCRIPT_YTDL_CHECK_PATH = DIR_SCRIPTS+"ytdl_check.py";
    public static final String QPY_SCRIPT_YTDL_CHECK = "" +
            "#qpy:qpyapp\n" +
            "print('script %REQUEST_CODE%')\n"+
            "try:\n" +
            "    print('checking youtube-dl...')\n" +
            "    import youtube_dl\n"+
            "    print('"+QPY_LOG_END_FLAG+"')\n" +
            "except:\n" +
            "    import sys\n" +
            "    print(sys.exc_info()[0])\n" +
            "    print('"+QPY_LOG_ERROR_FLAG+"')\n" +
            "exit()";
    public static final String SCRIPT_YTDL_UPGR_PATH = DIR_SCRIPTS+"ytdl_upgrade.py";
    public static final String QPY_SCRIPT_YTDL_UPGR = "" +
            "#qpy:qpyapp\n" +
            "print('script %REQUEST_CODE%')\n"+
            "try:\n" +
            "    print('upgrading youtube-dl...')\n" +
            "    import pip\n" +
            "    pip.main(['install','--upgrade','youtube_dl'])\n" +
            "    print('"+QPY_LOG_END_FLAG+"')\n" +
            "except:\n" +
            "    import sys\n" +
            "    print(sys.exc_info()[0])\n" +
            "    print('"+QPY_LOG_ERROR_FLAG+"')\n" +
            "exit()";

    //Python scripts

    /*
    https://github.com/rg3/youtube-dl/blob/master/README.md#embedding-youtube-dl
     */
    public static final String SCRIPT_YTDL_INST_PATH = DIR_SCRIPTS+"ytdl_install.py";
    public static final String QPY_SCRIPT_YTDL_INST = "" +
            "#qpy:qpyapp\n" +
            "print('script %REQUEST_CODE%')\n"+
            "try:\n" +
            "    print('installing youtube-dl...')\n" +
            "    import pip\n" +
            "    pip.main(['install','youtube_dl'])\n" +
            "    print('"+QPY_LOG_END_FLAG+"')\n" +
            "except:\n" +
            "    import sys\n" +
            "    print(sys.exc_info()[0])\n" +
            "    print('"+QPY_LOG_ERROR_FLAG+"')\n" +
            "exit()";
    public static final String QPY_TEMP_SCRIPT_PATH = DIR_SCRIPTS+"temp.py";
    public static final String QPY_SCRIPT_YTDL_VIDEO_PATH = DIR_SCRIPTS+"ytdl.py";
    public static final String QPY_SCRIPT_YTDL_VIDEO = "" +
            "from __future__ import unicode_literals\n" +
            "#qpy:qpyapp\n" +
            "print('script %REQUEST_CODE%')\n"+
            "try:\n" +
            "    print('downloading video...')\n" +
            "    import youtube_dl\n" +
            "    ydl_opts = {\n" +
            "        'format': 'bestaudio/best',\n" +
            "        'outtmpl' : '"+DIR_QUOTES+"temp.%(ext)s',\n" +
            "        'nocheckcertificate':'True',\n" +
            "        'no-continue':'True',\n" +
            "        'prefer_insecure':'True',\n" +
            "    }\n" +
            "    with youtube_dl.YoutubeDL(ydl_opts) as ydl:\n" +
            "        ydl.download(['http://www.youtube.com/watch?v=%VIDEOID%'])\n" +
            "    print('"+QPY_LOG_END_FLAG+"')\n" +
            "except:\n" +
            "    import sys\n" +
            "    print(sys.exc_info()[0])\n" +
            "    print('"+QPY_LOG_ERROR_FLAG+"')\n" +
            "exit()";
    public static final String QPY_SCRIPT_TAG_VIDEOID = "%VIDEOID%";
    public static final String QPY_SCRIPT_TAG_REQUESTCODE = "%REQUEST_CODE%";
    public static final String FFMPEG_DEFAULT_EXT = ".webm";
    public static final String FFMPEG_CONVERT = "-y -i %s %s";
    public static final String FFMPEG_CUT = "-y -i %s -ss %s -t %s -strict -2%s %s";
    public static final String FFMPEG_FADE_OUT = " -af afade=t=out:st=%s:d=1";

    //FFmpeg
    public static final int ERROR_FFMPEG_CUT = 11;
    public static final int ERROR_FFMPEG_EXTRACT = 12;
    public static final int ERROR_FFMPEG_RUNNING = 13;
    public static final int ERROR_YTDL = 21;

    //Errors
    public static final int ERROR_YTFRAG_INIT = 31;
    public static final int ERROR_YTFRAG = 32;
    public static final int ERROR_QPY_TIMEOUT = 41;
    static final boolean DEBUG = true;

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

    public static HashMap<String,String> GET_VIDEO_PARAMS(){
        HashMap<String,String> out = new HashMap<>();
        out.put("part","contentDetails,statistics");
        out.put("maxResults","1");
        out.put("key",GOOGLE_API_KEY);
        return out;
    }

    public static HashMap<String,String> GET_SUGGEST_PARAMS(){
        HashMap<String,String> out = new HashMap<>();
        out.put("client","firefox");
        out.put("ds","yt");
        return out;
    }

}
