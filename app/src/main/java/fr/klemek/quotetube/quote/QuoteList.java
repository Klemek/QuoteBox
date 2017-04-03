package fr.klemek.quotetube.quote;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import fr.klemek.quotetube.utils.Constants;

/**
 * Created by klemek on 16/03/17.
 */

public class QuoteList implements Serializable{

    private ArrayList<Quote> list;
    private int version;

    public QuoteList() {
        this.list = new ArrayList<>();
        this.version = Constants.LIST_VERSION;
    }

    public void add(Quote q){
        list.add(q);
    }

    public void remove(int i){
        list.remove(i);
    }

    public Quote get(int i){
        return list.get(i);
    }

    public int size(){
        return list.size();
    }

    public boolean isEmpty(){
        return list.isEmpty();
    }

    public int getVersion() {
        return version;
    }
}
