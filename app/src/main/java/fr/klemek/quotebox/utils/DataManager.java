package fr.klemek.quotebox.utils;

import com.google.gson.Gson;

import fr.klemek.quotebox.quote.Quote;
import fr.klemek.quotebox.quote.QuoteList;

/**
 * Created by klemek on 30/03/17 !
 */

public class DataManager {

    private static DataManager instance;

    private QuoteList quoteList;
    
    private DataManager(){
        loadList();
    }

    private void loadList(){
        //SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        //String quotesjson = sharedPref.getString(c.getString(R.string.saved_quotes_key),null);
        String quotesjson = FileUtils.readFile(Constants.LIST_FILE);
        if(quotesjson.trim().length()==0){
            quoteList = new QuoteList();
        }else{
            quoteList = new Gson().fromJson(quotesjson,QuoteList.class);
            if(quoteList.getVersion() != Constants.LIST_VERSION){
                //Update from v1 -> v2
                if (quoteList.getVersion() == 1) {
                    Utils.debugLog(DataManager.class, "Updating quote liste : v1 -> v2");
                    for (Quote q : quoteList.getAll())
                        q.setVideoInfo(new String[]{
                                null, "Unknown", "Unknown", "Unknown"
                        });
                    quoteList.setVersion(2);
                } else {//Clean old quote list
                    for (Quote q : quoteList.getAll())
                        FileUtils.tryDelete(q.getFile().getAbsolutePath());
                    quoteList = new QuoteList();
                }

            }

            int i = 0; //Check missing files
            while(i < quoteList.size()){
                if(!quoteList.get(i).getFile().exists()){
                    quoteList.remove(i);
                }else{
                    i++;
                }
            }
        }
    }

    public void saveList(){
        String quotesjson = (new Gson().toJson(quoteList));
        FileUtils.writeFile(Constants.LIST_FILE,quotesjson,true);
        //SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        //SharedPreferences.Editor prefsEditor = sharedPref.edit();
        //prefsEditor.putString(c.getString(R.string.saved_quotes_key),quotesjson);
    }
    
    public static DataManager getInstance(){
        if(instance == null)
            instance = new DataManager();
        return instance;
    }

    public QuoteList getQuoteList() {
        return quoteList;
    }
    

}
