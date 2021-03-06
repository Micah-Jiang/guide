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

        //toolbar???menu?????????????????????
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
         *  ?????????????????????
         *  ??????SensorManager?????????????????????Sensor???????????????
         */
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //????????????????????????????????????
        manager.registerListener(listener, sensor,
                SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //??????????????????????????????????????????
        manager.unregisterListener(listener);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //???????????????
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private final class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            /**
             *  values[0]: x-axis ???????????????
             ???  values[1]: y-axis ???????????????
             ??????values[2]: z-axis ???????????????
             */
            float degree = event.values[0];// ??????????????????
            float preDegree = -degree;
            mCompassDegreeTxt.setText(""+((int)degree)+"??");
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
                return "??? E";
            }else if (degree>=158 && degree <= 202){
                return "??? S";
            }else if (degree>=247 && degree <= 288){
                return "??? W";
            }else if (degree>=338 && degree <= 359){
                return "??? N";
            }else if (degree>=0 && degree <= 21){
                return "??? N";
            }else if (degree>=112 && degree <= 157){
                return "?????? SE";
            }else if (degree>=203 && degree <= 246){
                return "?????? SW";
            }else if (degree>=22 && degree <= 66){
                return "?????? NE";
            }else{
                return "?????? NW";
            }
        }
    }

    /**
     * ?????????????????????
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {
        //??????????????????
        ArrayList<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        //??????
        //??????????????????????????????
        if (permissions.size() == 0) {
            getLocationLl();
        } else {
            //?????????????????????????????????
            requestPermissions(permissions.toArray(new String[permissions.size()]), 2);
            Log.d("*************", "??????????????????");
        }
    }

    /**
     * ????????????????????????
     */
    private void getLocationLl() {
        Location location = getLastKnownLocation();
        if (location != null) {
            eastDegree.setText(d2Dms(location.getLatitude()));
            northDegree.setText(d2Dms(location.getLongitude()));
            getPositionInfo(location);
        } else {
            Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ???????????????????????????
     */
    private Location getLastKnownLocation() {
        //???????????????????????????
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
     * ?????????????????????
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("*************", "??????????????????");
                    getLocationLl();
                } else {
                    Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();
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
        return  d+"??"+m+"???"+s+"???";
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
                // ???????????????????????????????????????
                // ??????getAddressLine(0)???????????????getAddressLine(1)???????????????????????????getAddressLine(2)?????????????????????????????????????????????????????????0????????????????????????
                placename = ((Address) places.get(0)).getAddressLine(0);
//						+ ((Address) places.get(0)).getAddressLine(1) + ", "
//						+ ((Address) places.get(0)).getAddressLine(2);
            }
            locationName.setText("????????????:" + placename);
        } else {
            locationName.setText("????????????????????????");
        }
    }

}
