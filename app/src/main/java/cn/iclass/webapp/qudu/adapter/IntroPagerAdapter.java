package cn.iclass.webapp.qudu.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.iclass.webapp.qudu.R;
import cn.iclass.webapp.qudu.activity.MainActivity;
import cn.iclass.webapp.qudu.util.PreferenceHelper;


/**
 * Created by Administrator on 2016/11/8.
 */

public class IntroPagerAdapter extends PagerAdapter {

    private Activity activity;

    private int[] imgResIds = new int[]{R.mipmap.nav1, R.mipmap.nav2, R.mipmap.nav3};

    public IntroPagerAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return imgResIds.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(container.getContext());
        imageView.setBackgroundResource(imgResIds[position]);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        container.addView(imageView);

        if (position == 2) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreferenceHelper.write(activity,"QuDu","hasLaunched",true);
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                }
            });
        }
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
