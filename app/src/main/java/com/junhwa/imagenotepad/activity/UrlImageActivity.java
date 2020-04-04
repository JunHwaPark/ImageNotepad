package com.junhwa.imagenotepad.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.junhwa.imagenotepad.R;

import java.io.InputStream;
import java.net.URL;

public class UrlImageActivity extends AppCompatActivity {
    private EditText editUrl = null;
    private ImageView imageView = null;
    private InputStream inputStream = null;
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_image);

        editUrl = findViewById(R.id.editUrl);
        imageView = findViewById(R.id.imageUrl);
        Button buttonSearch = findViewById(R.id.buttonSearchUrl);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadImageAsyncTask asyncTask = new LoadImageAsyncTask();
                asyncTask.execute();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("URL", editUrl.getText().toString());
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    class LoadImageAsyncTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_url), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                inputStream = new URL(editUrl.getText().toString()).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
