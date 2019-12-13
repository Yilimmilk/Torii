package com.icezx.torii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    GoogleApiClient googleApiClient;
    ConstraintLayout constraintLayout;
    EditText etUrl;
    Button btSend,btRequest,btClear,btUuid;
    TextView tvUuid;
    String urlString="";
    String uuidString="";

    //获取设备名称
    String device_model = Build.MODEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //每个变量对应的xml资源
        constraintLayout=findViewById(R.id.constraintlayout_main);
        etUrl=findViewById(R.id.et_url);
        btSend=findViewById(R.id.bt_sendtowatch);
        btRequest=findViewById(R.id.bt_sendrequest);
        btClear=findViewById(R.id.bt_clear);
        btUuid=findViewById(R.id.bt_uuid);
        tvUuid=findViewById(R.id.tv_uuid);

        //创建一个GoogleApiClient对象
        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        //创建一个SharedPreferences对象
        SharedPreferences sharedPreferences= getSharedPreferences("settings", Context.MODE_PRIVATE);
        //每次启动时获取SharedPreferences的url和uuid值，并赋值给urlString和uuidString变量，默认为空,不为null
        urlString=sharedPreferences.getString("url","");
        uuidString=sharedPreferences.getString("uuid","");
        //实例化SharedPreferences.Editor对象
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        //开始连接
        googleApiClient.connect();

        //设置发送到手表按钮监听
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用SendToWatch方法
                SendToWatch(v);
                //如果urlString不为空，则.....
                if (urlString!=null){
                    //将值放入文件
                    editor.putString("url", urlString);
                    //提交
                    editor.commit();
                }
            }
        });

        //清除按钮监听
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除输入框内容
                etUrl.setText("");
                //同时清除SharedPreferences的值
                editor.putString("url", "");
                //提交
                editor.commit();
            }
        });

        //发送URL请求按钮监听
        btRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用sendRequestWithOkHttp方法
                SendRequestWithOkHttp();
            }
        });

        //uuid按钮监听
    btUuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //生成uuid，replaceAll后面是正则表达式
                uuidString= UUID.randomUUID().toString().replaceAll("-","");
                //将uuid放入文件
                editor.putString("uuid", uuidString);
                //提交
                editor.commit();
                //设置tv_uuid控件的值
                tvUuid.setText(uuidString);
            }
        });

        //每次启动时，自动输入urlString的值和uuid值
        etUrl.setText(urlString);
        tvUuid.setText(uuidString);

    }

    //发送至手表方法
    public void SendToWatch(View view){
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/KEY");//使用KEY来过滤
        DataMap dataMap = dataMapRequest.getDataMap();//获取消息的载体
        dataMap.putString("url",CheckEditText());//发送url
        Wearable.DataApi.putDataItem(googleApiClient,dataMapRequest.asPutDataRequest());//发送数据
    }

    //检查url框方法
    private String CheckEditText(){
        //判断输入框是否为空
        if (TextUtils.isEmpty(etUrl.getText().toString())) {
            //如果是，则弹出Toast
            Snackbar.make(constraintLayout, "URL不能为空", Snackbar.LENGTH_SHORT).show();
            return "";
        }else {
            //如果不是，则赋值给变量并弹Toast
            urlString=etUrl.getText().toString();
            Toast.makeText(MainActivity.this,urlString,Toast.LENGTH_LONG).show();
        }
        //返回输入框内容
        return urlString;
    }

    //发送http请求方法
    private void SendRequestWithOkHttp() {
        //新建一个线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//新建一个OkHttp对象
                    FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                    formBody.add("uuid",uuidString);//传递键值对参数:uuid
                    formBody.add("devicename", device_model);//传递键值对参数:设备名
                    Request request = new Request.Builder()
                            .url(urlString)//设置URL
                            .post(formBody.build())//添加参数
                            .build();//添加
                    Response response = client.newCall(request).execute();//发送请求
                    String responseData = response.body().string();//并没有什么卵用，用来接收返回数据，备用滴
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();//线程开启
    }
}