package com.junhwa.lineplusproject.recycler.thumbnail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.junhwa.lineplusproject.R;
import com.junhwa.lineplusproject.activity.MemoListActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.junhwa.lineplusproject.activity.MemoListActivity.fileIndexComparator;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ItemViewHolder> implements OnThumbnailClickListener{
    private ArrayList<Bitmap> thumbnails = new ArrayList<>();
    private OnThumbnailClickListener listener;

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ItemViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(thumbnails.get(position));
    }

    @Override
    public int getItemCount() {
        return thumbnails.size();
    }

    @Override
    public void onThumbnailClick(ItemViewHolder holder, View view, int position) {
        if (listener != null)
            listener.onThumbnailClick(holder, view, position);
    }

    public void setListener(OnThumbnailClickListener listener) {
        this.listener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;

        ItemViewHolder(View itemView, final OnThumbnailClickListener listener) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.imageViewThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null)
                        listener.onThumbnailClick(ItemViewHolder.this, v, position);
                }
            });
        }
        void onBind(Bitmap bitmap) {
            thumbnail.setImageBitmap(bitmap);
        }
    }

    public void addItem(Bitmap bitmap) {
        thumbnails.add(bitmap);
        this.notifyDataSetChanged();
    }

    public void removeItem(int position) {
        thumbnails.remove(position);
        this.notifyDataSetChanged();
    }

    public Bitmap getThumbnail(int position) {
        return thumbnails.get(position);
    }

    public void clearItem() {
        this.thumbnails.clear();
        this.notifyDataSetChanged();
    }

    public static void initializeThumbnailList(RecyclerView view, Context context, ThumbnailAdapter adapter) {
        RecyclerView recyclerView = view;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    public static ArrayList<Integer> showThumbnails(ThumbnailAdapter adapter, int id) {
        File dir = new File(MemoListActivity.PATH + "/thumbnails/" + id);
        File[] files = dir.listFiles();
        ArrayList<Integer> pictures = new ArrayList<>();
        if (files != null && files.length > 0) {
            Arrays.sort(files, fileIndexComparator);
            for (int i = 0; i < files.length; i++) {
                pictures.add(Integer.parseInt(files[i].getName().split("\\.")[0]));
                adapter.addItem(BitmapFactory.decodeFile(files[i].getAbsolutePath()));
            }
        }
        return pictures;
    }
}
