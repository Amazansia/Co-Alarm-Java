package com.example.mapsactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

class PreferenceManager {

    private static final String PREFERENCES_NAME = "PreferenceData";
    private static final String TAG = "PreferenceManager";

    private static final String KEY = "mStore";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    //SP에서 mStore를 불러오는 메소드
    //Gson 사용
    //즐겨찾기 버튼 클릭할 때마다 실행
    static List<FStore> getmStoreFromSP(Context context){
        SharedPreferences prefs = PreferenceManager.getPreferences(context);
        Gson gson = new GsonBuilder().create();
        String str = prefs.getString(KEY, null);
        List<FStore> temp;
        if(str == null) return null;
        Log.e(TAG,str);
        temp = gson.fromJson(str, new TypeToken<List<FStore>>(){}.getType());
        Log.e(TAG,gson.toJson(temp));
        return temp;
    }

    //mStore를 SP에 쓰는 메소드
    //Gson 사용
    static void setmStoretoSP(Context context, List<FStore> temp){
        SharedPreferences prefs = PreferenceManager.getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new GsonBuilder().create();
        String str = gson.toJson(temp);
        editor.putString(KEY, str);
        editor.commit();
    }
}