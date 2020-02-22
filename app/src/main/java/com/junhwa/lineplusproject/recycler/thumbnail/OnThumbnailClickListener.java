package com.junhwa.lineplusproject.recycler.thumbnail;

import android.view.View;

public interface OnThumbnailClickListener {
    void onThumbnailClick(ThumbnailAdapter.ItemViewHolder holder, View view, int position);
}
