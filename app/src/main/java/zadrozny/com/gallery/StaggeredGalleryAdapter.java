package zadrozny.com.gallery;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class StaggeredGalleryAdapter extends RecyclerView.Adapter<StaggeredGalleryAdapter.MyViewHolder> {

    private List<Uri> images;
    private Context mContext;
    private MainActivity mainActivity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public MyViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View v) { mainActivity.click(
                        MyViewHolder.this.getAdapterPosition(),
                        MyViewHolder.this.thumbnail); }
            });
        }
    }


    public StaggeredGalleryAdapter(Context context, List<Uri> images, MainActivity mainActivity) {
        mContext = context;
        this.images = images;
        this.mainActivity = mainActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.staggered_gallery_element, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Uri uri = images.get(position);

        Glide.with(mContext).load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .thumbnail(0.1f)
                .override(200, 200)
                .fitCenter()
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}