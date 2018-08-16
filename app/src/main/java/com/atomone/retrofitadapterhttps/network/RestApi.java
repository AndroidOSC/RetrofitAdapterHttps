package com.atomone.retrofitadapterhttps.network;

/**
 * Rest API Center
 *
 * @author atomOne
 * @data 2018/8/16/016
 * @describe TODO
 * @email 1299854942@qq.com
 */
public class RestApi {

    private static final RestApi    ourInstance = new RestApi();
    private              ApiService apiService  = null;//Basic API

    private RestApi() {
    }

    public static RestApi getInstance() {
        return ourInstance;
    }

    public ApiService getApiService() {
        if (apiService == null) {
            apiService = RetrofitHelper.getRetrofit()
                    .create(ApiService.class);
        }
        return apiService;
    }

}
