package zadrozny.com.gallery;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class ListGalleryAdapter extends RecyclerView.Adapter<ListGalleryAdapter.MyViewHolder> {

    private List<Uri> images;
    private Context mContext;
    private MainActivity mainActivity;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;
        public TextView name;
        public TextView size;
        public MyViewHolder(View view) {
            super(view);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            name = (TextView) view.findViewById(R.id.name);
            size = (TextView) view.findViewById(R.id.size);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View v) { mainActivity.click(
                        MyViewHolder.this.getAdapterPosition(),
                        MyViewHolder.this.thumbnail); }
            });
        }
    }


    public ListGalleryAdapter(Context context, List<Uri> images, MainActivity mainActivity) {
        mContext = context;
        this.images = images;
        this.mainActivity = mainActivity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_element, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Uri uri = images.get(position);

        holder.name.setText(mainActivity.titles.get(position));
        float size = (new File(uri.getPath())).length();
        if(size != 0) holder.size.setText(String.format("%.2f", size / 1000000f) + "MB");
        else holder.size.setText("");
        Glide.with(mContext).load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .thumbnail(0.1f)
                .override(200, 200)
                .centerCrop()
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}