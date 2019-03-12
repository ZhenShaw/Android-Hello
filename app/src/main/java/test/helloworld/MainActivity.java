package test.helloworld;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;


public class MainActivity extends AppCompatActivity {
    //记录视图
    public LinearLayout PhoneLayout;
    public RelativeLayout LoginLayout, RegisterLayout;
    public LinearLayout Loading;

    public boolean Flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化参数对象
        PhoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
        LoginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        RegisterLayout = (RelativeLayout) findViewById(R.id.register_layout);
        Loading = (LinearLayout) findViewById(R.id.loading);

        //监听登录按键点击事件
        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneLayout.setVisibility(View.GONE);
                //交换位置
                if (Flag) {
                    ObjectAnimator.ofFloat(LoginLayout, "TranslationY", 0).setDuration(1000).start();
                    ObjectAnimator.ofFloat(RegisterLayout, "TranslationY", 0).setDuration(1000).start();
                    Flag = false;
                    return;
                }
                submit("login");
            }
        });
        //监听注册按键点击事件
        findViewById(R.id.register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneLayout.setVisibility(View.VISIBLE);
                //交换位置
                if (!Flag) {
                    ObjectAnimator.ofFloat(LoginLayout, "TranslationY", 230).setDuration(1000).start();
                    ObjectAnimator.ofFloat(RegisterLayout, "TranslationY", -230).setDuration(1000).start();
                    Flag = true;
                    return;
                }
                submit("register");
            }
        });
    }

    //提交信息处理
    private void submit(String type) {

        EditText Username = (EditText) findViewById(R.id.username);
        EditText Password = (EditText) findViewById(R.id.password);
        EditText Phone = (EditText) findViewById(R.id.phone);

        //trim()删除头尾空白符的字符串
        String username = Username.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String phone = Phone.getText().toString().trim();

        if (username.length() == 0 || password.length() == 0) {
            Toast.makeText(this, "用户名和密码不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (type == "register" && phone.length() != 11) {
            Toast.makeText(this, "手机号长度不正确！", Toast.LENGTH_SHORT).show();
            return;
        }
        Loading.setVisibility(View.VISIBLE);

        if (type == "login") {
            login(username, password);
        } else {
            register(username, password, phone);
        }
    }


    //发送登录请求
    private void login(final String username, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormEncodingBuilder()
                        .add("username", username)
                        .add("password", password)
                        .build();
                Request request = new Request.Builder().url("http://task.cst.ifeel.vip:8000/login2")
                        .post(formBody).build();

                try {
                    Response response = client.newCall(request).execute();//发送请求
                    String result = response.body().string();
                    Log.d("res", "result: " + result);
                    resHandler(result, "login");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    //发送注册请求
    private void register(final String username, final String password, final String phone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormEncodingBuilder()
                        .add("username", username)
                        .add("password", password)
                        .add("phone", phone)
                        .build();
                Request request = new Request.Builder().url("http://task.cst.ifeel.vip:8000/sign2")
                        .post(formBody).build();

                try {
                    Response response = client.newCall(request).execute();//发送请求
                    String result = response.body().string();
                    Log.d("res", "result: " + result);
                    resHandler(result, "register");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    //请求结果处理
    private void resHandler(final String result, final String type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Loading.setVisibility(View.GONE);
                TextView tv = findViewById(R.id.text);
                ResData res = JSONHandler(result);
                String tosatStr;
                if (!res.status) {
                    tv.setText("错误信息：" + res.errMsg);
                    if (type == "login") {
                        tosatStr = "登录失败";
                    } else {
                        tosatStr = "注册失败";
                    }
                    Toast.makeText(MainActivity.this, tosatStr, Toast.LENGTH_SHORT).show();
                } else {
                    tv.setVisibility(View.GONE);
                    if (type == "login") {
                        tosatStr = "登录成功";
                        //转跳页面
                        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                        startActivity(intent);
                    } else {
                        tosatStr = "注册成功，请登录！";
                        if (Flag) {
                            PhoneLayout.setVisibility(View.GONE);
                            ObjectAnimator.ofFloat(LoginLayout, "TranslationY", 0).setDuration(1000).start();
                            ObjectAnimator.ofFloat(RegisterLayout, "TranslationY", 0).setDuration(1000).start();
                            Flag = false;
                        }
                    }
                    Toast.makeText(MainActivity.this, tosatStr, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //处理JSON数据
    private ResData JSONHandler(final String result) {
        ResData d = new ResData();
        try {
            JSONObject data = new JSONObject(result);
            //d.objects = data.getString("objects");
            d.errMsg = data.getString("errMsg");
            d.status = data.getBoolean("status");

            return d;
        } catch (JSONException e) {
            e.printStackTrace();
            return d;
        }
    }

    public class ResData {
        boolean status;
        String errMsg;
        //String objects;
    }

}


