package cn.iclass.webapp.qudu.api;



import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import cn.iclass.webapp.qudu.util.CipherUtils;
import cn.iclass.webapp.qudu.util.FileUtil;
import cn.iclass.webapp.qudu.vo.ImageResult;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Api {
    
    /**
     * 上传图片文件到图片服务器
     */
    public static void uploadImage(File file, final ProgressRequestBody.ProgressListener progressListener, final AbstractRequestCallback<ImageResult> requestCallback) {
        MultipartBody multipartBody = fileToMultipartBody(file, progressListener);
        Call<ImageResult> call = RetrofitClient.getInstance().getMediaService().uploadImage(multipartBody);
        enqueue(call, requestCallback);
    }

    /**
     * 下载文件
     */
    public static void downloadFile(final String fileUrl, final File voiceCacheDir, final AbstractRequestCallback<File> requestCallback) {
        Call<ResponseBody> call = RetrofitClient.getInstance().getMediaService().downloadFile(fileUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!voiceCacheDir.exists()) {
                    voiceCacheDir.mkdirs();
                }
                File file = new File(voiceCacheDir, CipherUtils.md5(FileUtil.getFilenameFromUrl(fileUrl)));
                try {
                    BufferedSink sink = Okio.buffer(Okio.sink(file));
                    sink.writeAll(response.body().source());
                    sink.close();
                    requestCallback.onSuccess(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private static MultipartBody fileToMultipartBody(File file, ProgressRequestBody.ProgressListener progressListener) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        if (progressListener != null) {
            builder.addFormDataPart("qqfile", file.getName(), new ProgressRequestBody(requestBody, progressListener));
        } else {
            builder.addFormDataPart("qqfile", file.getName(), requestBody);
        }
        builder.addFormDataPart("bucketName", "smallfiles");
        builder.addFormDataPart("source", "ANDROID");
        builder.setType(MultipartBody.FORM);
        MultipartBody multipartBody = builder.build();
        return multipartBody;
    }

    private static <T> void enqueue(Call<T> call, final AbstractRequestCallback<T> abstractRequestCallback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (abstractRequestCallback != null) {
                    if (response.isSuccessful() && response.code() == 200) {
                        abstractRequestCallback.onSuccess(response.body());
                    } else {
                        abstractRequestCallback.onFailure(response.code(), "内部服务器出错了");
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (abstractRequestCallback != null) {
                    if (t instanceof ServerException) {
                        ServerException serverException = (ServerException) t;
                        abstractRequestCallback.onFailure(serverException.getErrCode(), serverException.getMessage());
                    } else if (t instanceof SocketTimeoutException) {
                        abstractRequestCallback.onFailure(402, "服务器响应超时,请稍后重试！");
                    } else if (t instanceof UnknownHostException) {
                        abstractRequestCallback.onFailure(400, "网络连接不可用，请检查你的网络设置！");
                    } else if (t instanceof SocketException) {
                        SocketException socketException = (SocketException) t;
                        abstractRequestCallback.onFailure(401, socketException.getMessage());
                    }
                }
            }
        });
    }
}
