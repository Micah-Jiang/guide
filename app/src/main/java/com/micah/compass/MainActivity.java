package com.micah.compass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.micah.compass.about.AboutActivity;
import com.micah.compass.common.BaseActivity;
import com.micah.compass.view.DirectionView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private SensorManager manager;
    private SensorListener listener;
    private TextView mCompassDegreeTxt,mCompassDirectionTxt, eastDegree, northDegree, locationName;
    private DirectionView directionView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        initView();
    }

    public void initView(){
        directionView = findViewById(R.id.direction_main);
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        listener = new SensorListener();
        mCompassDegreeTxt = findViewById(R.id.compass_degree);
        mCompassDirectionTxt = findViewById(R.id.compass_direction);
        toolbar = findViewById(R.id.app_toolbar);
        eastDegree = findViewById(R.id.location_east);
        northDegree = findViewById(R.id.location_north);
        locationName = findViewById(R.id.location_name);
        dealToolBar();
    }

    public void dealToolBar(){
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //toolbar的menu点击事件的监听
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        /**
         *  获取方向传感器
         *  通过SensorManager对象获取相应的Sensor类型的对象
         */
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //应用在前台时候注册监听器
        manager.registerListener(listener, sensor,
                SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //应用不在前台时候销毁掉监听器
        manager.unregisterListener(listener);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //实例化菜单
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private final class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            /**
             *  values[0]: x-axis 方向加速度
             　  values[1]: y-axis 方向加速度
             　　values[2]: z-axis 方向加速度
             */
            float degree = event.values[0];// 存放了方向值
            float preDegree = -degree;
            mCompassDegreeTxt.setText(""+((int)degree)+"°");
            mCompassDirectionTxt.setText(formDirection(degree));
            directionView.rotate = preDegree;
            getLocation();

            directionView.postInvalidate();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public String formDirection(float degree){
            if (degree>=67 && degree <= 111){
                return "东 E";
            }else if (degree>=158 && degree <= 202){
                return "南 S";
            }else if (degree>=247 && degree <= 288){
                return "西 W";
            }else if (degree>=338 && degree <= 359){
                return "北 N";
            }else if (degree>=0 && degree <= 21){
                return "北 N";
            }else if (degree>=112 && degree <= 157){
                return "东南 SE";
            }else if (degree>=203 && degree <= 246){
                return "西南 SW";
            }else if (degree>=22 && degree <= 66){
                return "东北 NE";
            }else{
                return "西北 NW";
            }
        }
    }

    /**
     * 定位：权限判断
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {
        //检查定位权限
        ArrayList<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        //判断
        //有权限，直接获取定位
        if (permissions.size() == 0) {
            getLocationLl();
        } else {
            //没有权限，获取定位权限
            requestPermissions(permissions.toArray(new String[permissions.size()]), 2);
            Log.d("*************", "没有定位权限");
        }
    }

    /**
     * 定位：获取经纬度
     */
    private void getLocationLl() {
        Location location = getLastKnownLocation();
        if (location != null) {
            eastDegree.setText(d2Dms(location.getLatitude()));
            northDegree.setText(d2Dms(location.getLongitude()));
            getPositionInfo(location);
        } else {
            Toast.makeText(this, "位置信息获取失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 定位：得到位置对象
     */
    private Location getLastKnownLocation() {
        //获取地理位置管理器
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    /**
     * 定位：权限监听
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("*************", "同意定位权限");
                    getLocationLl();
                } else {
                    Toast.makeText(this, "未同意获取定位权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    /**
     * D to Dms
     */
    public static String d2Dms(double data){
        int d = (int)data;
        int m = (int)((data-d)*60);
        int s = (int)(((data-d)*60-m)*60);
        return  d+"°"+m+"′"+s+"″";
    }

    private void getPositionInfo(Location location) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(MainActivity.this);
            List places = null;

            try {
                places = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String placename = "";
            if (places != null && places.size() > 0) {
                // 以下的信息将会具体到某条街
                // 其中getAddressLine(0)表示国家，getAddressLine(1)表示精确到某个区，getAddressLine(2)表示精确到具体的街，实际使用的时候发现0的精度就已经不错
                placename = ((Address) places.get(0)).getAddressLine(0);
//						+ ((Address) places.get(0)).getAddressLine(1) + ", "
//						+ ((Address) places.get(0)).getAddressLine(2);
            }
            locationName.setText("当前位置:" + placename);
        } else {
            locationName.setText("无法获取地理信息");
        }
    }

}
