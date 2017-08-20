package fr.klemek.quotetube.quote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.klemek.quotetube.utils.Constants;

/**
 * Created by klemek on 16/03/17 !
 */

public class QuoteList implements Serializable{

    private final ArrayList<Quote> list;
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

    public List<Quote> getAll(){
        return list;
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

    public void setVersion(int version){ this.version = version;}
}
