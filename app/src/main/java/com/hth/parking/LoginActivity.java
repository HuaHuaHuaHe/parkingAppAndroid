package com.hth.parking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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


public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private Button login;
    private EditText usernameEdit,passwordEdit;
    private String username,password;
    private TextView toRegister; //这是跳转到注册界面的TextView

    //
    private final OkHttpClient client = new OkHttpClient();
    private RequestBody requestBody;
    private Request request;
    private Call call;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        login = findViewById(R.id.login);
        login.setOnClickListener(this);

        usernameEdit = findViewById(R.id.username);
        passwordEdit = findViewById(R.id.password);

        toRegister = findViewById(R.id.to_register);
        toRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login:
                Intent intenst = new Intent(this,LocateActivity.class);
                startActivity(intenst);
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
    private void login() {
        //获取输入
        username = usernameEdit.getText().toString();
        password = passwordEdit.getText().toString();

        //验证输入是否正确

        if(username.equals("") || password.equals("")){
            Log.d("LoginActivity","输入格式有误");
        }
        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username",username)
                .addFormDataPart("password",password)
                .build();
        request = new Request.Builder().url("localhost:/8081/parking/user/Login")
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
                Log.d("LoginActivity","success");
                Intent intent = new Intent(getApplicationContext(),ReserveActivity.class);
                startActivity(intent);
            }
        });
    }
}
