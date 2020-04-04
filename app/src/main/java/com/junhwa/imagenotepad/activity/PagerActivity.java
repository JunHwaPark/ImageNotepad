package com.junhwa.imagenotepad.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.junhwa.imagenotepad.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class PagerActivity extends AppCompatActivity {
    private ViewPager pager = null;
    private ImagePagerAdapter adapter = null;
    private int id, selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        Intent data = getIntent();
        if (data == null)
            Toast.makeText(getApplicationContext(), getString(R.string.wrong_approach), Toast.LENGTH_LONG).show();

        this.id = data.getIntExtra("ID", 0);
        this.selected = data.getIntExtra("SELECTED", 0);

        if (id == 0)
            Toast.makeText(getApplicationContext(), getString(R.string.wrong_approach), Toast.LENGTH_LONG).show();

        ShowImageAsyncTask asyncTask = new ShowImageAsyncTask();
        asyncTask.execute(id);
    }

    class ImagePagerAdapter extends PagerAdapter {
        private ArrayList<Bitmap> bitmaps = new ArrayList<>();

        public void addBitmap(Bitmap bitmap) {
            bitmaps.add(bitmap);
        }

        @Override
        public int getCount() {
            return bitmaps.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            //super.destroyItem(container, position, object);
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.item_image, container, false);
            ImageView imageView = v.findViewById(R.id.imageViewThumbnail);

            imageView.setImageBitmap(bitmaps.get(position));
            container.addView(v);
            return v;
        }
    }

    class ShowImageAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
            if (aBoolean) {
                pager.setAdapter(adapter);
                pager.setCurrentItem(selected);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.wrong_approach), Toast.LENGTH_LONG).show();
                Log.e("onPostExecute", "cannot show images");
            }
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
                File dir = new File(MemoListActivity.PATH + "/LinePlusProject/" + id);
                File[] files = dir.listFiles();
                if (files == null || files.length == 0)
                    return false;

                Arrays.sort(files, MemoListActivity.fileIndexComparator);
                pager = findViewById(R.id.pager);
                adapter = new ImagePagerAdapter();

            try {
                for (File file : files) {
                    //Get orientation and rotate bitmaps
                    Matrix matrix = new Matrix();
                    matrix.postRotate(WriteActivity.getOrientation(file));

                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    adapter.addBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
