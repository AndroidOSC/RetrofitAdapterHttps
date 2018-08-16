package com.atomone.retrofitadapterhttps.network;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;


/**
 * API
 *
 * @author atomOne
 * @data 2018/8/16/016
 * @describe TODO
 * @email 1299854942@qq.com
 */
public interface ApiService {
    /**
     * get数据
     */
    @GET
    Observable<String> getData(@Url String url);
}
