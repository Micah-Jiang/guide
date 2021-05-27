package com.micah.compass.common;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.micah.compass.utils.StatusBarUtil;

/**
 * @Author m.kong
 * @Date 2021/5/26 下午9:14
 * @Version 1
 * @Description
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBarLightMode(getWindow());
    }
}
