package com.siyann.taidaapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import utils.ActivityCollector;
import utils.LogUtil;
import utils.OkHttpUtil;

/**
 * 社区推客
 */
public class CommunityTwitterActivity extends Activity {

    @Bind(R.id.back)
    ImageView back;
    @Bind(R.id.title_view)
    TextView titleView;
    @Bind(R.id.content_text)
    EditText contentText;
    @Bind(R.id.submit_btn)
    Button submitBtn;

    private Context mContext;

    private String username=""; //昵称
    private String date=""; //日期

    private String result=""; //返回结果

    String content="";          //内容


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_twitter);
        ButterKnife.bind(this);
        mContext=this;

        ActivityCollector.addActivity(this);

        Intent intent=getIntent();
        titleView.setText(intent.getStringExtra("title"));


        /**
         * 获取当前时间
         */
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        date=simpleDateFormat.format(new java.util.Date());
        LogUtil.e("date",date);


        /**
         * 从缓存中读取username
         */
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
        username=pref.getString("username","");
        LogUtil.e("username",username);

    }

    @OnClick({R.id.back, R.id.submit_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:        //返回
                finish();
                break;
            case R.id.submit_btn:  //提交
                 content=contentText.getText().toString();
                 LogUtil.e("content",content);
                if (TextUtils.isEmpty(content)){
                    Toast.makeText(mContext,"内容不能为空",Toast.LENGTH_SHORT).show();
                }else {

                    if (OkHttpUtil.isNetworkAvailable(mContext)){
                        submittwitter();
                    }else {
                        /**
                         * 提示没有网络连接
                         */
                       final SweetAlertDialog dialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE);
                        dialog.setTitleText("提示")
                                .setContentText("网络连接错误，请检查网络连接")
                                .setConfirmText("确定")
                                .setCancelable(false);
                                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        ActivityCollector.finishAll();
                                        dialog.dismissWithAnimation();
                                    }
                                }).show();
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * 将内容传递过来
     * @param
     */
    private void submittwitter() {
        OkHttpUtil.sendOkHttpRequest("http://121.42.32.107:8001/TwitterReturn.asmx/TwitterReturnTo?UserID=FHMS&Password=FHMS2016&Content="+content+"&Name="+username+"&Date="+date+"&Type=App", new Callback() {
                @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e("e",e+"");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                result=response.body().string();
                result=result.replace("<string xmlns=\"http://tempuri.org/\">", "")
                        .replace("</string>", "")
                        .replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
                LogUtil.e("result",result);
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(mContext,result,Toast.LENGTH_SHORT).show();
                       if (!TextUtils.isEmpty(result)){
                           finish();
                       }
                   }
               });
            }
        });

    }


    /**
     * 重写返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
