package cn.iclass.webapp.qudu.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import cn.iclass.webapp.qudu.R;
import cn.iclass.webapp.qudu.adapter.IntroPagerAdapter;


public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        initWidget();
    }

    private void initWidget() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new IntroPagerAdapter(this));
        viewPager.setCurrentItem(0);
    }


}
