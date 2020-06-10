package com.hth.parking;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @author hth
 * @date 2020/5/31
 */
public class BaseActivity extends AppCompatActivity {
    /*登录状态*/
    public boolean LOGINING_STATE= false;
    /*导航视角*/
    /*日夜模式*/
    /*导航中的图面展示：全览小窗、路况条*/
    /*多路线推荐*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ACTIVITY",getClass().getName());
    }
}
