package com.ibmst.tilawatapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.ibmst.recitation.model.Index;
import com.ibmst.recitation.model.Page;
import com.ibmst.recitation.model.PageIndex;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Assets {

    public static void pageTest(Context context) {
        for(int i = 1; i <= 554; i++){
            getPage(context, i);
        }
    }

    //Todo Cache
    public class PageEntry{
        int pageNo;
        Page page;
    }
    PageEntry [] smallCache = new PageEntry[6];

    private static Gson gson = new Gson();

    public static String getFileData(Context context, String filename) {
        AssetManager manager = context.getAssets();
        byte[] formArray = null;
        InputStream file = null;

        try {
            file = manager.open(filename);
            formArray = new byte[file.available()];
            file.read(formArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (formArray != null) {
            try {
                return new String(formArray, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        return "";
    }


    public static String getPage(Context context,  int page)
    {
        String filename =  String.format("quran/pages/%03d.json", page);
        return getFileData(context, filename);
    }

    public static List<PageIndex> getIndex(Context context)
    {
        String json = getFileData(context, "quran/pages/index.json");
        Gson localGson = new Gson();
        Index index  = localGson.fromJson(json, Index.class);
        return index.getPageIndexList();
    }


    public static Page loadPage(Context context,  int page)
    {
        return gson.fromJson(getPage(context, page), Page.class);
    }

    public static void loadPageAsync(final Activity context, final int pageNo, final OnPageLoad callback)
    {


        AsyncTask task = new AsyncTask<Void, Void, Page>(){

            @Override
            protected Page doInBackground(Void... voids) {


                Page page= gson.fromJson(getPage(context, pageNo), Page.class);

                Log.d( "loadPageAsync", "Page  No " + pageNo );

                Log.d( "loadPageAsync", "Page  " + page );


                return page;
            }



            @Override
            protected void onPostExecute(Page page) {
                super.onPostExecute(page);

                if(!context.isFinishing()){
                    try {
                        callback.onPageLoad(page);
                    }catch (Exception ex){

                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface OnPageLoad{

        void onPageLoad(Page page);
    }

}
