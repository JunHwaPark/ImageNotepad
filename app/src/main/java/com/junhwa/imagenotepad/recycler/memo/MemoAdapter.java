package com.junhwa.imagenotepad.recycler.memo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.junhwa.imagenotepad.R;

import java.util.ArrayList;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ItemViewHolder> implements OnItemClickListener {
    private ArrayList<MemoItem> memoItems = new ArrayList<>();
    private OnItemClickListener listener;

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);
        return new ItemViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(memoItems.get(position));
    }

    @Override
    public int getItemCount() {
        return memoItems.size();
    }

    @Override
    public void onItemClick(ItemViewHolder holder, View view, int position) {
        if (listener != null)
            listener.onItemClick(holder, view, position);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle, textContents;
        private ImageView thumbnail;

        ItemViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textContents = itemView.findViewById(R.id.textContents);
            thumbnail = itemView.findViewById(R.id.imageThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null)
                        listener.onItemClick(ItemViewHolder.this, v, position);
                }
            });
        }

        void onBind(MemoItem item) {
            textTitle.setText(item.getTitle());
            textContents.setText(item.getContents());
            thumbnail.setImageBitmap(item.getPicture());
        }
    }

    public void addItem(MemoItem item) {
        memoItems.add(item);
        this.notifyDataSetChanged();
    }

    public MemoItem getMemo(int position) {
        return memoItems.get(position);
    }

    public void clearItem() {
        this.memoItems.clear();
        this.notifyDataSetChanged();
    }

    public static void initializeMemoList(RecyclerView view, Context context, MemoAdapter adapter) {
        RecyclerView recyclerView = view;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
    }
}
