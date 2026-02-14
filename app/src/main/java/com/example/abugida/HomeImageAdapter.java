package com.example.abugida;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HomeImageAdapter extends RecyclerView.Adapter<HomeImageAdapter.ImageViewHolder> {
    private final Context context;
    private final int[] imageResIds;

    public HomeImageAdapter(Context context, int[] imageResIds) {
        this.context = context;
        this.imageResIds = imageResIds;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.imageView.setImageResource(imageResIds[position]);
    }

    @Override
    public int getItemCount() {
        return imageResIds.length;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        ImageViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }
}
