package com.atomone.retrofitadapterhttps.network;


import com.atomone.retrofitadapterhttps.App;
import com.atomone.retrofitadapterhttps.AppConfig;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.adapter.https.HttpsHelpter;
import retrofit2.adapter.https.HttpsUtils;

/**
 * Created by IWALL on 2017/6/8.
 * Http Client
 */

/**
 * okhttpClient
 *
 * @author atomOne
 * @data 2018/8/16/016
 * @describe TODO
 * @email 1299854942@qq.com
 */
public class OkHttpHelper {

    private static final OkHttpHelper ourInstance = new OkHttpHelper();
    private OkHttpClient INSTANCE;

    private OkHttpHelper() {

    }

    public static OkHttpHelper getInstance() {
        return ourInstance;
    }


    public OkHttpClient getDefaultHttpClient() {
        if (INSTANCE == null) {
            INSTANCE = getHttpClientBuilder().build();
        }
        return INSTANCE;
    }


    /**
     * 构建OkHttpClient.Builder
     *
     * @return
     */
    private OkHttpClient.Builder getHttpClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 设置https
        try {
            InputStream cerInput = App.getAppContext().getAssets().open("client.bks");
            InputStream trustInput = App.getAppContext().getAssets().open("truststore.bks");
            HttpsHelpter.buildOkhttpSSLTwoAuth(builder, cerInput, "123456", trustInput, "123456");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 设置hostnameVerifier
        builder.hostnameVerifier(HttpsUtils.getHostnameVerifier());
        // 添加log
        if (AppConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            if (!builder.interceptors().contains(interceptor)) {
                builder.addInterceptor(interceptor);
            }
        }
        return builder;
    }
}
