package zadrozny.com.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class DisplayFullscreen extends AppCompatActivity {

    public static final int HIDE_BAR_TIME = 1300;

    float scale = 1f;
    ImageView imageView;
    ScaleGestureDetector scaleGestureDetector;

    Runnable mNavHider = new Runnable() {
        @Override public void run() { setNavVisibility(false); }
    };

    int mBaseSystemUiVisibility = 0;
    View view;
    public Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_fullscreen);

        if(Build.VERSION.SDK_INT >= 16) mBaseSystemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        handler = new Handler();
        view = getWindow().getDecorView();
        setNavVisibility(false);

        if(Build.VERSION.SDK_INT >= 21) postponeEnterTransition();

        Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getStringExtra(MainActivity.EXTRA_IMAGE));

        imageView = (ImageView) findViewById(R.id.image_preview);

        Glide.with(getApplicationContext()).load(uri)
                .override(640, 640)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .priority(Priority.IMMEDIATE)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        if(Build.VERSION.SDK_INT >= 21) startPostponedEnterTransition();
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        if(Build.VERSION.SDK_INT >= 21) startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(imageView);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale (ScaleGestureDetector detector) {
            if(Build.VERSION.SDK_INT >= 16) {
                scale *= detector.getScaleFactor();
                scale = Math.max(1f, Math.min(scale, 5f));
                imageView.setScaleX(scale);
                imageView.setScaleY(scale);
            }
            return true;
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent ev) {
        handler.removeCallbacks(mNavHider);
        setNavVisibility(true);
        scaleGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            handler.removeCallbacks(mNavHider);
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setNavVisibility (boolean visible) {
        if(Build.VERSION.SDK_INT >= 16) {
            int newVis = mBaseSystemUiVisibility;
            if (!visible) newVis |= View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN;
            else handler.postDelayed(mNavHider, HIDE_BAR_TIME);
            view.setSystemUiVisibility(newVis);
        }
    }

}
