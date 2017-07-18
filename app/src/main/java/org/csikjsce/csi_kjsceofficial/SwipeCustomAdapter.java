package org.csikjsce.csi_kjsceofficial;

/**
 * Created by sumit on 18/7/17.
 */
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import javax.security.auth.Destroyable;

public class SwipeCustomAdapter extends  PagerAdapter {
    private Context ctx;
    private LayoutInflater inflater;
    private int[] images= new int[]{R.drawable.csi_ic_splash_screen,R.drawable.csi_ic_splash_screen};
    SwipeCustomAdapter(Context ctx){

        this.ctx=ctx;
    }
    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return (view==(LinearLayout)object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater=(LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View swiper_view=(View)inflater.inflate(R.layout.swiper_layout,container ,false);
        ImageView sliderimage=(ImageView)swiper_view.findViewById(R.id.swiper_imageview);

        sliderimage.setImageResource(images[position]);
        container.addView(swiper_view);
        return swiper_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }
}