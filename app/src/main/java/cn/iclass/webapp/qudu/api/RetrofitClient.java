package cn.iclass.webapp.qudu.api;


import java.util.concurrent.TimeUnit;

import cn.iclass.webapp.qudu.Constants;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class RetrofitClient {

    private Retrofit.Builder retrofitBuilder = null;
    private OkHttpClient.Builder okHttpClientBuilder = null;
    private HttpInterceptor httpInterceptor = null;
    private MediaService mediaService = null;

    private static final RetrofitClient instance = new RetrofitClient();

    public static RetrofitClient getInstance() {
        return instance;
    }

    private RetrofitClient() {
        initOkHttp();
        initRetrofit();
        mediaService = retrofitBuilder.baseUrl(Constants.URL.MEDIA_SERVER_BASE_URL ).client(okHttpClientBuilder.addInterceptor(httpInterceptor).build()).build().create(MediaService.class);
    }

    private void initOkHttp() {
        httpInterceptor = new HttpInterceptor();
        httpInterceptor.setLogLevel(HttpInterceptor.Level.NONE);
        okHttpClientBuilder = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS);
    }

    private void initRetrofit() {
        retrofitBuilder = new Retrofit.Builder().addConverterFactory(JacksonConverterFactory.create());
    }

    public MediaService getMediaService() {
        return mediaService;
    }

}
