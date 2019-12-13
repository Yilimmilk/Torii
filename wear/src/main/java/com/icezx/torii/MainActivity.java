package com.icezx.torii;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity implements DataApi.DataListener{

    private TextView mTextView;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        googleApiClient.connect();//开启连接
        Wearable.DataApi.addListener(googleApiClient,this);//添加消息变化的监听

        // Enables Always-on
        setAmbientEnabled();


    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1){

            }
            Toast.makeText(getApplicationContext(),"设置URl为"+"",Toast.LENGTH_SHORT).show();
            return false;
        }
    });

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for(DataEvent event : dataEventBuffer) {
            Uri uri = event.getDataItem().getUri();//获取消息的uri
            String path = uri!=null ? uri.getPath() : null;//获取标识

            if("/KEY".equals(path)) {
                DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String url = map.getString("url");//获取消息内容
                handler.obtainMessage(1,url).sendToTarget();
            }
        }
    }
}