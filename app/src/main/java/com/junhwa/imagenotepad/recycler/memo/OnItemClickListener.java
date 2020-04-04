package com.junhwa.imagenotepad.recycler.memo;

import android.view.View;

public interface OnItemClickListener {
    void onItemClick(MemoAdapter.ItemViewHolder holder, View view, int position);
}
