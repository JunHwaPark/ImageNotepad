package com.junhwa.imagenotepad.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.junhwa.imagenotepad.R;
import com.junhwa.imagenotepad.database.MemoDatabase;
import com.junhwa.imagenotepad.recycler.thumbnail.OnThumbnailClickListener;
import com.junhwa.imagenotepad.recycler.thumbnail.ThumbnailAdapter;

public class ViewMemoActivity extends AppCompatActivity {
    public static final int REQUEST_UPDATE = 300;

    private TextView textTitle, textContents, textCreation, textModification = null;
    private ThumbnailAdapter adapter = null;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_memo);

        textTitle = findViewById(R.id.textViewTitle);
        textContents = findViewById(R.id.textViewContents);
        textCreation = findViewById(R.id.textCreation);
        textModification = findViewById(R.id.textModification);

        Intent data = getIntent();
        id = data.getIntExtra("ID", 0);

        adapter = new ThumbnailAdapter();
        ThumbnailAdapter.initializeThumbnailList((RecyclerView) findViewById(R.id.recyclerThumbnail), this, adapter);
        adapter.setListener(new OnThumbnailClickListener() {
            @Override
            public void onThumbnailClick(ThumbnailAdapter.ItemViewHolder holder, View view, int position) {
                Intent intent = new Intent(getApplicationContext(), PagerActivity.class);
                intent.putExtra("SELECTED", position);
                intent.putExtra("ID", id);
                startActivity(intent);
            }
        });
        showMemo();

        Button updateButton = findViewById(R.id.buttonUpdate);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
                intent.putExtra("ID", id);
                startActivityForResult(intent, REQUEST_UPDATE);
            }
        });
        Button removeButton = findViewById(R.id.buttonRemove);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemoDatabase.deleteMemo(getApplicationContext(), "ID = " + id, null);
                finish();
            }
        });
    }

    private void showMemo() {
        String[] memo = MemoDatabase.loadMemo(this, id);
        if (memo != null) {
            textTitle.setText(memo[0]);
            textContents.setText(memo[1]);
            textCreation.setText(getString(R.string.creation) + memo[2]);
            textModification.setText(getString(R.string.modification) + memo[3]);
        } else
            Toast.makeText(getApplicationContext(), getString(R.string.wrong_approach), Toast.LENGTH_LONG).show();

        adapter.clearItem();
        ThumbnailAdapter.showThumbnails(adapter, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showMemo();
    }
}
