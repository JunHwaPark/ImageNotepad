package com.junhwa.lineplusproject.activity;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.junhwa.lineplusproject.R;
import com.junhwa.lineplusproject.database.MemoDatabase;
import com.junhwa.lineplusproject.recycler.thumbnail.OnThumbnailClickListener;
import com.junhwa.lineplusproject.recycler.thumbnail.ThumbnailAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class WriteActivity extends AppCompatActivity {
    public static final int REQUEST_GALLERY_SAMSUNG = 200;
    public static final int REQUEST_GALLERY = 201;
    public static final int REQUEST_CAMERA = 202;
    public static final int REQUEST_URL = 203;

    private ContentResolver resolver = null;
    private List<Integer> pictures = null;
    private HashSet<Integer> pictureSet = null;
    private ThumbnailAdapter adapter = null;

    private EditText editTitle, editContents = null;

    private long maximum, sequence, maxPictureIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        editTitle = findViewById(R.id.editTitle);
        editContents = findViewById(R.id.editContents);
        resolver = getContentResolver();
        pictures = new ArrayList<>();
        pictureSet = new HashSet<>();
        adapter = new ThumbnailAdapter();
        ThumbnailAdapter.initializeThumbnailList((RecyclerView) findViewById(R.id.recyclerThumbnail), this, adapter);
        adapter.setListener(new OnThumbnailClickListener() {
            @Override
            public void onThumbnailClick(ThumbnailAdapter.ItemViewHolder holder, View view, final int position) {
                //Show dialog and re-confirm about remove_picture.
                AlertDialog.Builder builder = new AlertDialog.Builder(WriteActivity.this);
                builder.setTitle(getString(R.string.remove_picture));
                builder.setMessage(getString(R.string.remove_confirm))
                        .setCancelable(true)
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pictureSet.remove(pictures.get(position));
                                pictures.remove(position);
                                adapter.removeItem(position);
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        checkAndMkdir(MemoListActivity.PATH);
        checkAndMkdir(MemoListActivity.PATH, 0);

        Intent data = getIntent();
        if (data == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.wrong_approach), Toast.LENGTH_LONG).show();
        } else {
            maximum = MemoDatabase.getMemoSequence(this) + 1;
            this.sequence = data.getIntExtra("ID", (int) maximum);
            if (this.sequence != maximum) {
                //Load memo and set text to EditText.
                String[] memo = MemoDatabase.loadMemo(this, (int) this.sequence);
                if (memo != null) {
                    editTitle.setText(memo[0]);
                    editContents.setText(memo[1]);
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.wrong_approach), Toast.LENGTH_LONG).show();

                pictures = ThumbnailAdapter.showThumbnails(adapter, (int) sequence);
                maxPictureIndex = pictures.size() > 0 ? pictures.get(pictures.size() - 1) : 0;
                for (Integer integer : pictures)
                    pictureSet.add(integer);
            }
        }

        Button buttonPicture = findViewById(R.id.buttonPicture);
        buttonPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (pictures.size() > 0)
                    maxPictureIndex = pictures.get(pictures.size() - 1) + 1;
                else
                    maxPictureIndex = 0;

                try {
                    //Set path of image by file provider
                    File file = new File(MemoListActivity.PATH + "/LinePlusProject/" + 0 + "/" + maxPictureIndex + ".jpeg");
                    file.createNewFile();
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.junhwa.lineplusproject", file);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent chooser = Intent.createChooser(cameraIntent, "CAMERA");
                startActivityForResult(chooser, REQUEST_CAMERA);
            }
        });

        Button buttonGallery = findViewById(R.id.buttonGallery);
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                if (Build.MANUFACTURER.equals("samsung")) {
                    //Samsung devices are distinguished by their different behaviors in multiple image select.
                    galleryIntent.setAction("android.intent.action.MULTIPLE_PICK");
                    Intent chooser = Intent.createChooser(galleryIntent, "Gallery");
                    startActivityForResult(chooser, REQUEST_GALLERY_SAMSUNG);
                } else {
                    galleryIntent.setAction(Intent.ACTION_PICK);
                    galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                    Intent chooser = Intent.createChooser(galleryIntent, "Gallery");
                    startActivityForResult(chooser, REQUEST_GALLERY);
                }
            }
        });

        Button buttonUrl = findViewById(R.id.buttonUrl);
        buttonUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent urlIntent = new Intent(getApplicationContext(), UrlImageActivity.class);
                startActivityForResult(urlIntent, REQUEST_URL);
            }
        });

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndMkdir(MemoListActivity.PATH, sequence);

                long result;
                Date now = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (maximum == sequence) {
                    //Insert new memo
                    ContentValues memoValue = new ContentValues();
                    memoValue.put("Title", editTitle.getText().toString());
                    memoValue.put("Contents", editContents.getText().toString());
                    memoValue.put("Creation", dateFormat.format(now));
                    memoValue.put("Modification", dateFormat.format(now));
                    result = MemoDatabase.insertMemo(getApplicationContext(), memoValue);
                } else {
                    //Update previous memo
                    ContentValues memoValue = new ContentValues();
                    memoValue.put("Title", editTitle.getText().toString());
                    memoValue.put("Contents", editContents.getText().toString());
                    memoValue.put("Modification", dateFormat.format(now));
                    result = MemoDatabase.updateMemo(getApplicationContext(), memoValue, "ID = " + sequence, null);
                }

                //Move temporary image files and thumbnails
                for (int index : pictures) {
                    File file = new File(MemoListActivity.PATH + "/LinePlusProject/" + 0 + "/" + index + ".jpeg");
                    if (file.exists())
                        file.renameTo(new File(MemoListActivity.PATH + "/LinePlusProject/" + sequence + "/" + index + ".jpeg"));
                    File thumb = new File(MemoListActivity.PATH + "/thumbnails/" + 0 + "/" + index + ".jpeg");
                    if (thumb.exists())
                        thumb.renameTo(new File(MemoListActivity.PATH + "/thumbnails/" + sequence + "/" + index + ".jpeg"));
                }

                //Delete discarded image files
                File dir = new File(MemoListActivity.PATH + "/LinePlusProject/" + sequence);
                for (File file : dir.listFiles()) {
                    if (!pictureSet.contains(Integer.parseInt(file.getName().split("\\.")[0])))
                        file.delete();
                }
                //Delete discarded thumbnails
                dir = new File(MemoListActivity.PATH + "/thumbnails/" + sequence);
                for (File file : dir.listFiles()) {
                    if (!pictureSet.contains(Integer.parseInt(file.getName().split("\\.")[0])))
                        file.delete();
                }
                //Clear temporary directory
                for (int index : pictures) {
                    File file = new File(MemoListActivity.PATH + "/LinePlusProject/" + 0 + "/" + index + ".jpeg");
                    File thumb = new File(MemoListActivity.PATH + "/thumbnails/" + 0 + "/" + index + ".jpeg");
                    file.delete();
                    thumb.delete();
                }
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Log.w("onActivityResult", "RESULT_CANCELED");
            return;
        }

        switch (requestCode) {
            case REQUEST_GALLERY_SAMSUNG: {
                final Bundle extras = data.getExtras();
                int count = extras.getInt("selectedCount");
                Log.i("onActivityResult", "selected images : " + count);
                Object items = extras.getStringArrayList("selectedItems");
                List<Uri> uris = (ArrayList<Uri>) items;
                for (int i = 0; i < count; i++)
                    saveTemporaryImage(uris.get(i));

                break;
            }
            case REQUEST_GALLERY: {
                ClipData clip = data.getClipData();
                if (clip != null) {
                    Log.i("onActivityResult", "selected images : " + clip.getItemCount());
                    for (int i = 0; i < clip.getItemCount(); i++)
                        saveTemporaryImage(clip.getItemAt(i).getUri());

                } else {
                    //select only one image in gallery or cloud_berry
                    Log.i("onActivityResult", "selected images : " + 1);
                    saveTemporaryImage(data.getData());
                }
                break;
            }
            case REQUEST_URL: {
                String url = data.getStringExtra("URL");
                if (url == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_url), Toast.LENGTH_LONG).show();
                } else {
                    if (pictures == null || pictures.size() > 0)
                        maxPictureIndex = pictures.get(pictures.size() - 1) + 1;
                    else
                        maxPictureIndex = 0;

                    //Get image from URL
                    //Network thread needed
                    BitmapAsyncTask asyncTask = new BitmapAsyncTask();
                    asyncTask.execute(url);

                    pictures.add((int) maxPictureIndex);
                    pictureSet.add((int) maxPictureIndex);
                }
                break;
            }
            case REQUEST_CAMERA: {
                try {
                    File file = new File(MemoListActivity.PATH + "/LinePlusProject/" + 0 + "/" + maxPictureIndex + ".jpeg");
                    saveThumbnail(MemoListActivity.PATH + "/thumbnails/" + 0 + "/" + maxPictureIndex + ".jpeg", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pictures.add((int) maxPictureIndex);
                pictureSet.add((int) maxPictureIndex);
                break;
            }
        }
    }

    class BitmapAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private File file = null;

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                InputStream inputStream = new URL(strings[0]).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                file = new File(MemoListActivity.PATH + "/LinePlusProject/" + 0 + "/" + maxPictureIndex + ".jpeg");
                file.createNewFile();
                OutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                try {
                    saveThumbnail(MemoListActivity.PATH + "/thumbnails/" + 0 + "/" + maxPictureIndex + ".jpeg", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_url), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveTemporaryImage(Uri uri) {
        try {
            InputStream stream = resolver.openInputStream(uri);
            if (pictures.size() > 0)
                maxPictureIndex = pictures.get(pictures.size() - 1) + 1;
            else
                maxPictureIndex = 0;
            saveStreamToFile(stream, 0, (int) maxPictureIndex);
            pictures.add((int) maxPictureIndex);
            pictureSet.add((int) maxPictureIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveStreamToFile(InputStream input, long memoSequence, int name) throws IOException {
        File file = new File(MemoListActivity.PATH + "/LinePlusProject/" + memoSequence + "/" + name + ".jpeg");
        file.createNewFile();
        OutputStream outputStream = new FileOutputStream(file);
        byte[] bytes = new byte[input.available()];
        input.read(bytes);
        outputStream.write(bytes);
        outputStream.close();

        saveThumbnail(MemoListActivity.PATH + "/thumbnails/" + memoSequence + "/" + name + ".jpeg", file);
    }

    private void saveThumbnail(String destPath, File source) throws IOException {
        Matrix matrix = new Matrix();
        matrix.postRotate(getOrientation(source));

        InputStream input = new FileInputStream(source);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 150, 150);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        adapter.addItem(bitmap);

        File thumbnail = new File(destPath);
        thumbnail.createNewFile();
        OutputStream thumbnailStream = new FileOutputStream(thumbnail);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbnailStream);
    }

    public static int getOrientation(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return ExifInterface.ORIENTATION_UNDEFINED;
            }
        } catch (IOException e) {
            Log.e("getOrientation", e.getMessage());
            return 0;
        }
    }

    private void checkAndMkdir(String path) {
        File dir1 = new File(path + "/LinePlusProject/");
        if (!dir1.exists())
            dir1.mkdir();
        File dir2 = new File(path + "/thumbnails/");
        if (!dir2.exists())
            dir2.mkdir();
    }

    private void checkAndMkdir(String path, long id) {
        File dir1 = new File(path + "/LinePlusProject/" + id);
        if (!dir1.exists())
            dir1.mkdir();
        File dir2 = new File(path + "/thumbnails/" + id);
        if (!dir2.exists())
            dir2.mkdir();
    }
}
