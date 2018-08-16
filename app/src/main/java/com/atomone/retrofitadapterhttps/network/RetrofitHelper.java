package com.atomone.retrofitadapterhttps.network;


import com.atomone.retrofitadapterhttps.AppConfig;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

/**
 * @author atomOne
 * @data 2018/8/16/016
 * @describe TODO
 * @email 1299854942@qq.com
 */
public class RetrofitHelper {

    private static Retrofit         mRetrofit = null;
    private static Retrofit.Builder mBuilder  = new Retrofit.Builder()
            .baseUrl(AppConfig.API_BASE_URL)
            .addConverterFactory(FastJsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

    private RetrofitHelper() {
    }

    public static Retrofit getRetrofit() {
        if (mRetrofit == null) {
            mBuilder.client(OkHttpHelper.getInstance().getDefaultHttpClient());
            mRetrofit = mBuilder.build();
        }
        return mRetrofit;
    }

}
