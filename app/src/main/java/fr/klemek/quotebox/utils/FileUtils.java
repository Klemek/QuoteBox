package fr.klemek.quotebox.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static fr.klemek.quotebox.utils.Utils.debugLog;

/**
 * Created by klemek on 30/03/17 !
 */

@SuppressWarnings("SameParameterValue")
public abstract class FileUtils {

    static String readFile(String path){
        StringBuilder text = new StringBuilder();
        File f = new File(Constants.DIR_EXT_STORAGE +path.replace("/storage/emulated/0",""));
        try {
            text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            long t0 = System.currentTimeMillis();
            while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
            }
            br.close();
            long dt = System.currentTimeMillis() - t0;
            debugLog(FileUtils.class, f.getAbsolutePath() + " : " + f.length() + "B (" + dt + " ms read)");
        }
        catch (IOException e) {
            debugLog(FileUtils.class,"IOException:"+e.getMessage());
        }

        return text.toString();
    }

    public static boolean writeFile(String path,String content,boolean forcereplace){
        File f = new File(Constants.DIR_EXT_STORAGE +path.replace("/storage/emulated/0",""));
        if(f.exists() && !forcereplace && content.equals(readFile(path))){
            return true;
        }
        try {
            if(f.exists() || f.createNewFile()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(content);
                bw.close();
                return true;
            }else{
                debugLog(FileUtils.class,"Couldnt create file :"+path);
            }
        } catch (IOException e) {
            debugLog(FileUtils.class,"IOException:"+e.getMessage());
        }
        return false;
    }

    static String readFileForce(String path, String[] begin){
        return readFileForce(path, begin, null, false);
    }

    static String readFileForce(String path, String[] begin, String[] end, boolean timeout){
        StringBuilder text = new StringBuilder();
        File f = new File(Constants.DIR_EXT_STORAGE +path.replace("/storage/emulated/0",""));
        if(!f.exists())
            return null;
        debugLog(FileUtils.class,"Accessing : " + f.getAbsolutePath() + " ...");

        boolean beg = begin == null;
        String last = "";

        long t0;

        try {
            do{
                text = new StringBuilder();
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String line;
                t0 = System.currentTimeMillis();
                while ((line = br.readLine()) != null || (end != null)) {
                    if(!beg && timeout && System.currentTimeMillis() - t0 > Constants.MAX_QPY_WAIT)
                        return null;
                    if(line != null) {
                        if(begin != null){
                            for (String begs : begin){
                                if (begs != null && line.contains(begs)) {
                                    beg = true;
                                }
                            }
                        }
                        if(!beg){
                            if(!line.equals(last))
                                debugLog(FileUtils.class,"=> wrong start : "+line);
                            last = line;
                            break;
                        }else{
                            text.append(line);
                            text.append('\n');
                            if (beg && end != null && end.length>0) {
                                boolean quit = false;
                                for (String ends : end){
                                    if (line.contains(ends)) {
                                        quit = true;
                                        break;
                                    }
                                }
                                if(quit)
                                    break;
                            }
                        }
                    }
                    while (!br.ready()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                br.close();
            }while(!beg);
            long dt = System.currentTimeMillis() - t0;
            debugLog(FileUtils.class, f.getAbsolutePath() + " : " + f.length() + "B (" + dt + " ms read)");
        }
        catch (IOException e) {
            debugLog(FileUtils.class,"IOException:"+e.getMessage());
        }

        return text.toString();
    }

    public static MediaPlayer loadSound(File soundFile){
        if(soundFile.exists()){
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(soundFile.getAbsolutePath());
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                        //debugLog(FileUtils.class,"playing",0);
                        /*
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                //mediaPlayer.release();
                                mediaPlayer.stop();
                            }
                        });
                         */
                    }
                });
                //mediaPlayer.prepareAsync();
                return mediaPlayer;
            } catch (IOException e) {
                debugLog(FileUtils.class,"IOException:"+e.getMessage());
            }
        }else{
            debugLog(FileUtils.class,"File "+soundFile.getAbsolutePath()+" doesn't exist");
        }
        return null;
    }

    public static String generateFileName(String base, String ext){
        int i = -1;
        File f;
        do{
            i++;
            f = new File(Constants.DIR_QUOTES+base+i+ext);
        }while(f.exists());
        return base+i+ext;
    }

    public static boolean tryDelete(String path){
        File f = new File(path);
        return !f.exists() || f.delete();
    }
}
