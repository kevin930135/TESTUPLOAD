package com.example.testupload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private String upload_path;
    ProgressDialog pDialog;

    int serverResponseCode = 0;
    private FileInputStream fileInputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pDialog = new ProgressDialog(this);

        Button btn_upload =  findViewById(R.id.btn_upload);
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();
            upload_path = GetGalleryPath(selectedImageUri, this);
            Upload();
        }
    }

    public static String GetGalleryPath(Uri uri, Activity activity) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    private void Upload() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new UploadFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "your api link");
            } else {
                new UploadFile().execute("your api link");
            }
        } catch (Exception e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private class UploadFile extends AsyncTask<String, Void, Void> {

        String fileName = upload_path;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(upload_path);
        private String Content;
        private String Error = null;

        protected void onPreExecute() {

            pDialog.show();
        }

        protected Void doInBackground(String... urls) {
            BufferedReader reader = null;
            if (!sourceFile.isFile()) {

                pDialog.dismiss();

                Log.e("uploadFile", "Source File not exist");


            } else {
                try {
                    fileInputStream = new FileInputStream(sourceFile);

                    URL url = new URL(urls[0]);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    serverResponseCode = conn.getResponseCode();
                    Content = conn.getResponseMessage();


                } catch (Exception ex) {
                    Error = ex.getMessage();
                } finally {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            pDialog.dismiss();

            try {

                if (Content != null) {

                    Toast.makeText(getApplicationContext(),Content,Toast.LENGTH_SHORT).show();
                    if(Content.equalsIgnoreCase("OK")){

                        Toast.makeText(getApplicationContext(),"Your file uploaded successfully",Toast.LENGTH_SHORT).show();

                    }

                }
            } catch (Exception e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}