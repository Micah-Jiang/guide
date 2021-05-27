package com.micah.compass.about;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.micah.compass.R;
import com.micah.compass.common.BaseActivity;

/**
 * @Author m.kong
 * @Date 2021/5/26 下午8:45
 * @Version 1
 * @Description
 */
public class AboutActivity extends BaseActivity {
    private Toolbar toolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        init();
    }

    public void init(){
        toolbar = findViewById(R.id.app_toolbar);
        dealToolBar();
    }

    public void dealToolBar(){
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
