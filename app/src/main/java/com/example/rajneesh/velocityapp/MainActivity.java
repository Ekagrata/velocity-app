package com.example.rajneesh.velocityapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.PointerSpeedometer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  implements SensorEventListener{
    SensorManager sensorManager;
    Sensor accelerometer;
    PointerSpeedometer batterymeter;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    Adapter adapter;
    ArrayList<Stations_info> infos;
    TextView km;
    int disleft=10,perc;
  // TextView speed,gpspeed,kalmanspeed;
    LocationManager locationmanager;
    Boolean loc_changed_called= false;
    float time;
    int count=0;
    float ax=0,ay=0,az=0,vx,vy,vz,v_acc,v_gps;

    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

//            speed = findViewById(R.id.speed);
//            gpspeed = findViewById(R.id.gpsspeed);
//            kalmanspeed = findViewById(R.id.kalmanspeed);
            batterymeter= findViewById(R.id.batterymeter);
            progressBar= findViewById(R.id.progressBar);
            recyclerView= findViewById(R.id.recyclerview);
            //  recyclerView.setNestedScrollingEnabled(false);
            //recyclerView.hasNestedScrollingParent()

            km= findViewById(R.id.km);

            perc= (disleft*100)/50;
            progressBar.setProgress(perc);
          //  batterymeter.speedTo(50);
            km.setText(disleft+"Kms Remaining");

            Stations_info info1= new Stations_info(15,"Rohini Sector 18_MetroStation");
            Stations_info info2= new Stations_info(15,"Rohini west Metro");
            infos= new ArrayList<>();
            infos.add(info1);
            infos.add(info2);
            adapter= new Adapter(this,infos);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            recyclerView.setItemAnimator(new DefaultItemAnimator());




            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            time = (System.currentTimeMillis() / 1000);
            Log.d("initial time", time + "");
            locationmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    loc_changed_called = true;
                    v_gps = location.getSpeed() * (18 / 5);
                   // gpspeed.setText("" + v_gps);
                    if (location == null) {
                        batterymeter.speedTo(v_acc);
                      //  kalmanspeed.setText(v_acc + "" + "no gps data inside func");
                    } else {
                        kalmanfilter(20.0f, 5.0f, v_acc, v_gps);

                    }

                   loc_changed_called = false;
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public int hashCode() {
                    return super.hashCode();
                }

                @Override
                public void onProviderEnabled(String s) {


                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

            // speed.setText(v_acc+""+"no gps data");
            //Toast.makeText(this, v_gps+"", Toast.LENGTH_SHORT).show();


        }
        catch(Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        try {
            count++;
            if (count % 5 == 0) {
                time = (System.currentTimeMillis() / 1000) - time;
                Log.d("timediff", time + "");
                displayvelocity();
            }
            ax = ax + sensorEvent.values[0];
            ay = ay + sensorEvent.values[1];
            az = az + sensorEvent.values[2];
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }


//         double vx= sensorEvent.values[0]*0.2;
//        double vy= sensorEvent.values[1]*0.2;
//        double vz= sensorEvent.values[2]*0.2;
//        double v= Math.sqrt(Math.pow(vx,2)+Math.pow(vy,2)+Math.pow(vz,2));
//        speed.setText("acc- "+sensorEvent.values[0]+"  "+sensorEvent.values[1]+"  "+sensorEvent.values[2]);
//         Log.d("sensor","a="+sensorEvent.values[0]+"  v="+vx);

        //double ax1= sensorEvent.values[0];
//        Timer timer= new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//               // double ax2=sensorEvent.values[0];
//                Log.d("sensor values","X:"+ sensorEvent.values[0]+"  Y:"+ sensorEvent.values[1]+ "  Z:" + sensorEvent.values[2]);
//            }
//        },5000,5000);
    }

    private void displayvelocity() {
        try {
            vx = (ax * time) * (18 / 5);
            vy = (ay * time) * (18 / 5);
            vz = (az * time) * (18 / 5);
            ax = ay = az = 0;
            v_acc = (float)Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2) + Math.pow(vz, 2));
           // speed.setText("" + v_acc);
            if (loc_changed_called == false)
                //kalmanspeed.setText(v_acc + "no gps");
            time = System.currentTimeMillis() / 1000;
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            }
        }
    }

    public void kalmanfilter(Float E_est, Float E_mea,Float Est_tm,Float Meas){
        try {
            Float kg,Est_t=0.0f,nE_est;
            for(int i=0;i<5;i++) {
                 kg = E_est / (E_est + E_mea);
                 Est_t = Est_tm + (kg * (Meas - Est_tm));
                 nE_est = (1 - kg) * (E_est);
                 E_est=nE_est;
                 Est_tm= Est_t;
                 Meas= v_gps;
            }
          //  kalmanspeed.setText(Est_t + "");
            batterymeter.speedTo(Est_t);
//            try {
//                kalmanfilter(nE_est, E_mea, Est_t, v_gps);
//            }
//            catch(Exception e){
//                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
//            }
        }
        catch(Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}








