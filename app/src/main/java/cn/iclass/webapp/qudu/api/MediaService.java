package cn.iclass.webapp.qudu.api;


import cn.iclass.webapp.qudu.vo.ImageResult;
import cn.iclass.webapp.qudu.vo.VoiceResult;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2016/8/7.
 */
public interface MediaService {

    @POST("form")
    Call<ImageResult> uploadImage(@Body MultipartBody multipartBody);

    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    @POST("formAudio")
    Call<VoiceResult> uploadVoice(@Body MultipartBody multipartBody);

}
