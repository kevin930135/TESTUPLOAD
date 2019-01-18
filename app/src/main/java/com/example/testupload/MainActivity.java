package com.example.testupload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends Activity{
    private String imagePath;//將要上傳的圖片路徑
    private Handler handler;//將要綁定到創建他的線程中(通常是位於主線程)
    private MultipartEntity multipartEntity;
    private Boolean isUpload = false;//推斷是否上傳成功
    private TextView tv;
    private String sImagePath;//server端返回路徑
    private static final String[] LOCATION_AND_CONTACTS =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imagePath = "/sdcard/Download/11.jpg";


        handler = new Handler();//綁定到主線程中
        String url = "http://c79d11c7.ngrok.io/upload";
        url = url.replaceAll("/","%2F");
        Button btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {



// TODO Auto-generated method stub


                new Thread() {
                    public void run() {
                        File file = new File(imagePath);
                        multipartEntity = new MultipartEntity();
                        ContentBody contentBody = new FileBody(file,
                                "image/jpeg");
                        multipartEntity.addPart("filename", contentBody);
                        HttpPost httpPost = new HttpPost(
                                "http://c79d11c7.ngrok.io/upload");
                        httpPost.setEntity(multipartEntity);
                        HttpClient httpClient = new DefaultHttpClient();
                        try {
                            HttpResponse httpResponse = httpClient
                                    .execute(httpPost);
                            InputStream in = httpResponse.getEntity()
                                    .getContent();
                            String content = readString(in);
                            int httpStatus = httpResponse.getStatusLine()
                                    .getStatusCode();//獲取通信狀態
                            if (httpStatus == HttpStatus.SC_OK) {
                                JSONObject jsonObject = parseJSON(content);//解析字符串為JSON對象
                                String status = jsonObject.getString("status");//獲取上傳狀態
                                sImagePath = jsonObject.getString("path");
                                Log.d("MSG", status);
                                if (status.equals("true")) {
                                    isUpload = true;
                                }
                            }
                            Log.d("MSG", content);
                            Log.d("MSG", "Upload Success");
                        } catch (ClientProtocolException e) {
// TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
// TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (Exception e) {
// TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
// TODO Auto-generated method stub
                                if (isUpload) {
                                    tv = (TextView)findViewById(R.id.textView1);
                                    tv.setText(sImagePath);
                                    Toast.makeText(MainActivity.this, "上傳成功",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "上傳失敗",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();
            }
        });

    }

    protected String readString(InputStream in) throws Exception {
        byte[] data = new byte[1024];
        int length = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while ((length = in.read(data)) != -1) {
            bout.write(data, 0, length);
        }
        return new String(bout.toByteArray(), "GBK");
    }

    protected JSONObject parseJSON(String str) {
        JSONTokener jsonParser = new JSONTokener(str);
        JSONObject result = null;
        try {
            result = (JSONObject) jsonParser.nextValue();
        } catch (JSONException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

}