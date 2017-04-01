package cn.iclass.webapp.qudu.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import cn.iclass.webapp.qudu.Constants;
import cn.iclass.webapp.qudu.R;
import cn.iclass.webapp.qudu.api.AbstractRequestCallback;
import cn.iclass.webapp.qudu.api.Api;
import cn.iclass.webapp.qudu.bottomsheet.BottomSheetLayout;
import cn.iclass.webapp.qudu.bottomsheet.MenuSheetView;
import cn.iclass.webapp.qudu.jsbridge.WVJBWebViewClient;
import cn.iclass.webapp.qudu.update.UpdateAgent;
import cn.iclass.webapp.qudu.update.UpdateInfo;
import cn.iclass.webapp.qudu.update.UpdateManager;
import cn.iclass.webapp.qudu.util.AudioPlayUtil;
import cn.iclass.webapp.qudu.util.SystemUtil;
import cn.iclass.webapp.qudu.vo.ImageResult;
import cn.iclass.webapp.qudu.vo.VoiceResult;
import cn.iclass.webapp.qudu.widget.RecordButton;

import static cn.iclass.webapp.qudu.Constants.REQUEST_CODE_ALBUM;
import static cn.iclass.webapp.qudu.Constants.REQUEST_CODE_CAMERA;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, MenuSheetView.OnMenuItemClickListener {
    private final String TAG = "MainActivity";
    private int clickCount = 0;
    private String imagePath;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private BottomSheetLayout bottomSheetLayout;
    private RecordButton recordButton;
    private WebView webView;
    private Toolbar toolBar;
    private TextView titleTv;
    private TextView indexTv;
    private TextView leftTv;
    private TextView rightTv;
    private String currentUrl;
    private List<String> specialUrls = new ArrayList<>();
    private WVJBWebViewClient.WVJBResponseCallback myCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidget();
    }

    private void initWidget() {
        check(true, false, false, false, 998);
        recordButton = (RecordButton) findViewById(R.id.btn_record);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avi);
        avLoadingIndicatorView.setIndicatorColor(Color.parseColor("#FF4081"));
        avLoadingIndicatorView.setIndicator("BallSpinFadeLoaderIndicator");
        avLoadingIndicatorView.hide();
        bottomSheetLayout = (BottomSheetLayout) findViewById(R.id.bottomSheetLayout);
        toolBar = (Toolbar) findViewById(R.id.toolbar);
        titleTv = (TextView) toolBar.findViewById(R.id.toolbar_title);
        titleTv.setOnClickListener(this);
        toolBar.setTitle("");
        leftTv = (TextView) findViewById(R.id.toolbar_left);
        leftTv.setOnClickListener(this);
        indexTv = (TextView) findViewById(R.id.toolbar_index);
        indexTv.setOnClickListener(this);
        rightTv = (TextView) findViewById(R.id.toolbar_right);
        rightTv.setOnClickListener(this);

        recordButton.setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, long recordTime) {
                recordButton.setVisibility(View.GONE);
                String time = (new BigDecimal(recordTime / 1000.0).setScale(0, RoundingMode.HALF_UP)) + "";
                uploadAmr(audioPath, time);
            }
        });

        webView = (WebView) findViewById(R.id.webView);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webView.setWebChromeClient(new WebChromeClient() {
                                       @Override
                                       public void onReceivedTitle(WebView view, String title) {
                                           super.onReceivedTitle(view, title);
                                           currentUrl = view.getUrl().toLowerCase();
                                           titleTv.setText(title);
                                           if (!TextUtils.equals(Constants.URL.BASE_URL.toLowerCase(), currentUrl) && !specialUrls.contains(currentUrl)) {
                                               indexTv.setVisibility(View.VISIBLE);
                                           } else {
                                               indexTv.setVisibility(View.GONE);
                                           }

                                           if (specialUrls.contains(currentUrl)) {
                                               leftTv.setBackground(null);
                                               leftTv.setText("刷新");
                                           } else {
                                               leftTv.setBackgroundResource(R.mipmap.ic_back);
                                               leftTv.setText(null);
                                           }

                                           if (TextUtils.equals(Constants.URL.BASE_URL.toLowerCase() + "readMobile/messages/teacher".toLowerCase(), currentUrl)) {
                                               rightTv.setText("发送私信");
                                               rightTv.setBackground(null);
                                           } else if (TextUtils.equals(Constants.URL.BASE_URL.toLowerCase() + "readMobile/teach".toLowerCase(), currentUrl)) {
                                               rightTv.setText("创建任务");
                                               rightTv.setBackground(null);
                                           } else if (TextUtils.equals(Constants.URL.BASE_URL.toLowerCase() + "readMobile/bookLib".toLowerCase(), currentUrl)) {
                                               rightTv.setText("分类");
                                               rightTv.setBackground(null);
                                           } else {
                                               rightTv.setText(null);
                                               rightTv.setBackgroundResource(R.mipmap.ic_setting);
                                           }
                                       }
                                   }
        );
        webView.setWebViewClient(new CustomWebViewClient(webView));

        initSpecialUrls();
        webView.loadUrl(Constants.URL.BASE_URL);
    }

    private void initSpecialUrls() {
        specialUrls.clear();
        specialUrls.add(Constants.URL.BASE_URL + "readMobile/bookLib".toLowerCase());
        specialUrls.add(Constants.URL.BASE_URL + "readMobile/messages/teacher".toLowerCase());
        specialUrls.add(Constants.URL.BASE_URL + "readMobile/messages/student".toLowerCase());
        specialUrls.add(Constants.URL.BASE_URL + "readMobile/me".toLowerCase());
        specialUrls.add(Constants.URL.BASE_URL + "readMobile/teach".toLowerCase());
        specialUrls.add(Constants.URL.BASE_URL + "readMobile/toRead".toLowerCase());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            uploadImage(imagePath);
        } else if (requestCode == REQUEST_CODE_ALBUM && intent != null) {
            String imagePath = SystemUtil.getFilePathFromUri(intent.getData(), this).toString();
            if (new File(imagePath).exists()) {
                uploadImage(imagePath);
            }
        }
    }

    void check(boolean isManual, final boolean isForce, final boolean isSilent, final boolean isIgnorable, final int notifyId) {
        UpdateManager.create(this).setUrl(Constants.URL.UPDATE_URL).setWifiOnly(false).setManual(isManual).setNotifyId(notifyId).setParser(new UpdateAgent.InfoParser() {
            @Override
            public UpdateInfo parse(String source) throws Exception {
                JSONObject jsonObject = new JSONObject(source);
                String versionCode = jsonObject.getString("versionCode");
                String downloadUrl = jsonObject.getString("downloadUrl");
                String updateIntro = jsonObject.getString("updateIntro");
                String isForceUpdate = jsonObject.getString("isForceUpdate");

                UpdateInfo info = new UpdateInfo();
                if (SystemUtil.getVersionCode(MainActivity.this) < Integer.valueOf(versionCode)) {
                    info.hasUpdate = true;
                } else {
                    info.hasUpdate = false;
                }
                info.updateContent = updateIntro;
                info.url = downloadUrl;
                info.md5 = "56cf48f10e4cf6043fbf53bbbc4009e2";
                info.isForce = TextUtils.equals("1", isForceUpdate);
                info.isIgnorable = isIgnorable;
                info.isSilent = isSilent;
                return info;
            }
        }).check();
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_right:
                if (TextUtils.equals(Constants.URL.BASE_URL.toLowerCase() + "readMobile/messages/teacher".toLowerCase(), currentUrl)) {
                    webView.loadUrl(Constants.URL.BASE_URL + "readMobile/sendPrivateMsg");
                } else if (TextUtils.equals(Constants.URL.BASE_URL.toLowerCase() + "readMobile/teach".toLowerCase(), currentUrl)) {
                    webView.loadUrl(Constants.URL.BASE_URL + "readMobile/addTask");
                } else if (TextUtils.equals(Constants.URL.BASE_URL.toLowerCase() + "readMobile/bookLib".toLowerCase(), currentUrl)) {
                    webView.loadUrl(Constants.URL.BASE_URL + "readMobile/bookpage");
                } else {
                    showMenuSheet(MenuSheetView.MenuType.GRID, R.menu.menu_share);
                }

                break;
            case R.id.toolbar_index:
                webView.loadUrl(Constants.URL.BASE_URL);
                break;
            case R.id.toolbar_left:
                if (specialUrls.contains(currentUrl)) {
                    webView.reload();
                } else {
                    onBackPressed();
                }
                break;

            case R.id.toolbar_title:
                clickCount++;
                if (clickCount > 9) {
                    Constants.URL.BASE_URL = "https://qudumobiletest.joy-read.com/";
                    initSpecialUrls();
                    webView.clearHistory();
                    webView.loadUrl(Constants.URL.BASE_URL);
                }
                break;
        }
    }


    private void showMenuSheet(final MenuSheetView.MenuType menuType, final int menuId) {
        MenuSheetView menuSheetView = new MenuSheetView(MainActivity.this, menuType, null, this);
        menuSheetView.inflateMenu(menuId);
        bottomSheetLayout.showWithSheetView(menuSheetView);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        bottomSheetLayout.dismissSheet();
        if (item != null) {
            switch (item.getItemId()) {
                case R.id.share_copy:
                    SystemUtil.putTextDataIntoClipboard(webView.getUrl(), "currentUrl_" + webView.getUrl(),
                            MainActivity.this);
                    SystemUtil.showToast(MainActivity.this, "已经复制到剪贴板");
                    break;

                case R.id.share_browser:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(webView.getUrl()));
                    startActivity(intent);
                    break;

                case R.id.share_refresh:
                    webView.reload();
                    break;
            }
        }
        return false;
    }


    private void uploadAmr(String path, final String recordTime) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("正在上传语音。。。").canceledOnTouchOutside(false)
                .progress(true, 0)
                .progressIndeterminateStyle(false).build();
        dialog.show();
        Api.uploadVoice(new File(path), new AbstractRequestCallback<VoiceResult>() {
            @Override
            public void onSuccess(VoiceResult voiceResult) {
                if (!TextUtils.isEmpty(voiceResult.html)) {
                    myCallback.callback(recordTime + "|" + voiceResult.html);
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, String errorMsg) {
                SystemUtil.showToast(MainActivity.this, "上传失败，请检查网络!");
                dialog.dismiss();
            }
        });
    }


    private void uploadImage(String path) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("正在上传图片,请稍候。。。").canceledOnTouchOutside(false)
                .progress(true, 0)
                .progressIndeterminateStyle(false).build();
        dialog.show();
        Api.uploadImage(new File(path), null, new AbstractRequestCallback<ImageResult>() {
            @Override
            public void onSuccess(ImageResult imageResult) {
                dialog.dismiss();
                myCallback.callback(imageResult.html);
            }

            @Override
            public void onFailure(int statusCode, String errorMsg) {
                dialog.dismiss();
                SystemUtil.showToast(MainActivity.this, "上传失败，请检查网络!");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.URL.BASE_URL = "https://qudumobile.joy-read.com/";
    }

    class CustomWebViewClient extends WVJBWebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            avLoadingIndicatorView.setVisibility(View.INVISIBLE);
            super.onPageFinished(view, url);
        }

        public CustomWebViewClient(WebView webView) {
            super(webView, new WVJBWebViewClient.WVJBHandler() {
                @Override
                public void request(Object data, WVJBResponseCallback callback) {

                }
            });

            registerHandler("shouldnextpage", new WVJBWebViewClient.WVJBHandler() {
                @Override
                public void request(Object data, WVJBResponseCallback callback) {
                    myCallback = callback;
                    myCallback.callback(1);
                }
            });

            registerHandler("onPicTake", new WVJBWebViewClient.WVJBHandler() {
                @Override
                public void request(Object data, WVJBResponseCallback callback) {
                    myCallback = callback;
                    imagePath = Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".jpg";
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imagePath)));
                    startActivityForResult(intent, Constants.REQUEST_CODE_CAMERA);
                }
            });

            registerHandler("onAlbumSelect", new WVJBWebViewClient.WVJBHandler() {
                @Override
                public void request(Object data, WVJBResponseCallback callback) {
                    myCallback = callback;
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, Constants.REQUEST_CODE_ALBUM);
                }
            });

            registerHandler("onVoiceCreate", new WVJBWebViewClient.WVJBHandler() {
                @Override
                public void request(Object data, WVJBResponseCallback callback) {
                    myCallback = callback;
                    recordButton.setVisibility(View.VISIBLE);
                }

            });

            registerHandler("onVoicePreview", new WVJBWebViewClient.WVJBHandler() {
                @Override
                public void request(Object data, WVJBResponseCallback callback) {
                    myCallback = callback;
                    if (data != null) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(data.toString());
                            String voiceUrl = jsonObject.getString("data");
                            Api.downloadFile(voiceUrl, new File(Environment.getExternalStorageDirectory() + "/QuDu/voice"), new AbstractRequestCallback<File>() {
                                @Override
                                public void onSuccess(File file) {
                                    String localPath = file.getAbsolutePath();
                                    if (!TextUtils.isEmpty(localPath)) {
                                        AudioPlayUtil.getInstance().startPlay(localPath, null, false, null);
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, String errorMsg) {

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            registerHandler("onVoiceInit", new WVJBWebViewClient.WVJBHandler() {
                @Override
                public void request(Object data, WVJBResponseCallback callback) {
                    myCallback = callback;
                    int maxRecordTime = 60;
                    if (data != null) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(data.toString());
                            maxRecordTime = jsonObject.getInt("data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (recordButton.getVisibility() == View.GONE) {
                        recordButton.setVisibility(View.INVISIBLE);
                        recordButton.setShowDialog(false);
                        recordButton.setMaxIntervalTime(maxRecordTime * 1000);
                        recordButton.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recordButton.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, recordButton.getLeft() + 5, recordButton.getTop() + 5, 0));
                            }
                        }, 250);
                    } else if (recordButton.getVisibility() == View.INVISIBLE) {
                        recordButton.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, recordButton.getLeft() + 5, recordButton.getTop() + 5, 0));
                        recordButton.setVisibility(View.GONE);
                    }
                }
            });
        }

    }

}
