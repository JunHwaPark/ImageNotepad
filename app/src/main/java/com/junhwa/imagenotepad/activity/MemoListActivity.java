package com.junhwa.imagenotepad.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.junhwa.imagenotepad.R;
import com.junhwa.imagenotepad.database.MemoDatabase;
import com.junhwa.imagenotepad.recycler.memo.MemoAdapter;
import com.junhwa.imagenotepad.recycler.memo.MemoItem;
import com.junhwa.imagenotepad.recycler.memo.OnItemClickListener;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;


//Use Open-Source called "AutoPermissions"
//From : https://github.com/pedroSG94/AutoPermissions
public class MemoListActivity extends AppCompatActivity implements AutoPermissionsListener {
    public static final int REQUEST_PERMISSIONS = 100;

    public static String PATH = null;
    private MemoAdapter adapter = null;
    private MemoDatabase dbManager = null;

    public static Comparator<File> fileIndexComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            //The names of files(except the extension) are always an integer-type string.
            //The file first jas a smaller number.
            return Integer.parseInt(o1.getName().split("\\.")[0]) - Integer.parseInt(o2.getName().split("\\.")[0]);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        PATH = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        dbManager = MemoDatabase.getInstance(this);
        adapter = new MemoAdapter();
        MemoAdapter.initializeMemoList((RecyclerView) findViewById(R.id.recyclerMemo), this, adapter);
        adapter.setListener(new OnItemClickListener() {
            @Override
            public void onItemClick(MemoAdapter.ItemViewHolder holder, View view, int position) {
                MemoItem item = adapter.getMemo(position);
                Intent intent = new Intent(getApplicationContext(), ViewMemoActivity.class);
                intent.putExtra("ID", item.getId());
                startActivity(intent);
            }
        });

        Button newMemoButton = findViewById(R.id.newMemo);
        newMemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
                startActivity(intent);
            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, REQUEST_PERMISSIONS);
    }

    private void showMemoList(MemoDatabase memoDatabase, MemoAdapter adapter) {
        Cursor cursor = memoDatabase.queryMemo(new String[]{"ID", "Title", "Contents"}, null, null, null, null, "ID DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String contents = cursor.getString(2);

                File dir = new File(MemoListActivity.PATH + "/thumbnails/" + id);
                File[] files = dir.listFiles();
                if (files == null || files.length == 0) {
                    adapter.addItem(new MemoItem(id, title, contents, null));
                } else {
                    //find thumbnails and put lowest one into item ("The first image must be thumbnail")
                    Arrays.sort(files, fileIndexComparator);
                    adapter.addItem(new MemoItem(id, title, contents, BitmapFactory.decodeFile(files[0].getAbsolutePath())));
                }
            }
            cursor.close();
        }
    }

    @Override
    public void onDenied(int i, String[] strings) {
        Toast.makeText(getApplicationContext(), getString(R.string.request_permissions), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onGranted(int i, String[] strings) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("signal", "onActivityResult()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clearItem();
        showMemoList(dbManager, adapter);
    }

    public MemoAdapter getAdapter() {
        return this.adapter;
    }
}
