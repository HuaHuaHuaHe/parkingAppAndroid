package com.hth.parking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity implements View.OnClickListener{

    //注册按钮
    private Button register;

    private EditText usernameEditText;
    private EditText passwordEditText;

    private String username,password;

    private final OkHttpClient client = new OkHttpClient();
    private RequestBody requestBody;
    private Request request;
    private Call call;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }

    private void init() {
        register = findViewById(R.id.register_r);
        register.setOnClickListener(this);

        usernameEditText = findViewById(R.id.username_r);
        passwordEditText = findViewById(R.id.password_r);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register_r:
                register();
        }
    }

    /*
    封装代码步骤
    * 1：拿到okHttpClient对象
    * 2：构造Request
      2.1构造requestBody
      2.2包装requestBody
      3.call -> execute
    * */
    private void register() {
        //获取输入
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();

        //验证输入是否正确
        if(username.equals("") || password.equals("")){
            Log.d("LoginActivity","输入格式有误");
        }
        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username",username)
                .addFormDataPart("password",password)
                .build();
        request = new Request.Builder().url("https://localhost:/8081/parking/user/register")
                .post(requestBody)
                .build();
        call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("LoginActivity",e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String content=response.body().string();
                if(content.equals("success")){
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                    Log.d("LoginActivity",content);
                }
            }
        });
    }
}
