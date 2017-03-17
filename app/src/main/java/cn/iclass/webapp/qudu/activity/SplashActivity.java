package cn.iclass.webapp.qudu.activity;

import android.content.Intent;
import android.widget.ImageView;

import cn.iclass.webapp.qudu.R;
import cn.iclass.webapp.qudu.util.PreferenceHelper;


public class SplashActivity extends BaseSplash {

    @Override
    protected void setRootBackground(ImageView view) {
        view.setBackgroundResource(R.mipmap.splash_bg);
    }

    @Override
    protected void redirectTo() {
        boolean hasLaunched = PreferenceHelper.readBoolean(this, "QuDu", "hasLaunched", false);
        if (hasLaunched) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, IntroActivity.class));
        }
        finish();
    }
}
