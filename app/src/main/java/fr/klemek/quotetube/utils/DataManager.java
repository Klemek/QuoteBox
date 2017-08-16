package fr.klemek.quotetube.utils;

import android.content.Context;

import com.google.gson.Gson;

import fr.klemek.quotetube.quote.QuoteList;

/**
 * Created by klemek on 30/03/17 !
 */

public class DataManager {

    private static DataManager instance;

    private QuoteList quoteList;
    
    private DataManager(Context c){
        loadList(c);
    }

    private void loadList(Context c){
        //SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        //String quotesjson = sharedPref.getString(c.getString(R.string.saved_quotes_key),null);
        String quotesjson = FileUtils.readFile(Constants.LIST_FILE);
        if(quotesjson.trim().length()==0){
            quoteList = new QuoteList();
        }else{
            quoteList = new Gson().fromJson(quotesjson,QuoteList.class);
            if(quoteList.getVersion() == Constants.LIST_VERSION){
                int i = 0; //Check missing files
                while(i < quoteList.size()){
                    if(!quoteList.get(i).getFile().exists()){
                        quoteList.remove(i);
                    }else{
                        i++;
                    }
                }
            }else{
                quoteList = new QuoteList();
            }
        }
    }

    public void saveList(Context c){
        String quotesjson = (new Gson().toJson(quoteList));
        FileUtils.writeFile(Constants.LIST_FILE,quotesjson,true);
        //SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        //SharedPreferences.Editor prefsEditor = sharedPref.edit();
        //prefsEditor.putString(c.getString(R.string.saved_quotes_key),quotesjson);
    }
    
    public static DataManager getInstance(Context c){
        if(instance == null)
            instance = new DataManager(c);
        return instance;
    }

    public QuoteList getQuoteList() {
        return quoteList;
    }
    

}
