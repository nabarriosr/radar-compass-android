package com.orbitalcompass.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public final class MainActivity extends Activity implements SensorEventListener, LocationListener {
    private static final int LOCATION_REQUEST = 42;
    private SensorManager sensors;
    private LocationManager locations;
    private Sensor accelerometer, magnetometer;
    private final float[] gravity = new float[3];
    private final float[] magnetic = new float[3];
    private boolean hasGravity, hasMagnetic;
    private float declination;
    private CompassView compass;
    private BannerAds bannerAds;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        compass = findViewById(R.id.compass);
        bannerAds = new BannerAds(this,
                (FrameLayout) findViewById(R.id.ad_container),
                (TextView) findViewById(R.id.ad_placeholder),
                (TextView) findViewById(R.id.privacy_ads));
        sensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        locations = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (accelerometer == null || magnetometer == null) compass.setError("Este equipo no tiene los sensores necesarios");
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        }
    }

    private void startLocationIfPermitted() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            compass.setGps(false);
            return;
        }
        try {
            locations.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000L, 100f, this);
            Location last = locations.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) onLocationChanged(last);
        } catch (SecurityException ignored) { compass.setGps(false); }
    }

    private void stopLocation() {
        try { locations.removeUpdates(this); } catch (RuntimeException ignored) { }
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == LOCATION_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationIfPermitted();
        } else compass.setGps(false);
    }

    @Override protected void onResume() {
        super.onResume();
        if (accelerometer != null) sensors.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (magnetometer != null) sensors.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        startLocationIfPermitted();
        if (bannerAds != null) bannerAds.resume();
    }

    @Override protected void onPause() {
        sensors.unregisterListener(this);
        stopLocation();
        if (bannerAds != null) bannerAds.pause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (bannerAds != null) bannerAds.destroy();
        super.onDestroy();
    }

    @Override public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.16f;
        float[] target;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { target = gravity; hasGravity = true; }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) { target = magnetic; hasMagnetic = true; }
        else return;
        for (int i = 0; i < 3; i++) target[i] += alpha * (event.values[i] - target[i]);
        if (hasGravity) {
            float pitch = (float) Math.toDegrees(Math.atan2(-gravity[0], Math.sqrt(gravity[1] * gravity[1] + gravity[2] * gravity[2])));
            float roll = (float) Math.toDegrees(Math.atan2(gravity[1], gravity[2]));
            compass.setTilt(pitch, roll);
        }
        if (!hasGravity || !hasMagnetic) return;
        float[] rotation = new float[9];
        float[] orientation = new float[3];
        if (SensorManager.getRotationMatrix(rotation, null, gravity, magnetic)) {
            SensorManager.getOrientation(rotation, orientation);
            float magneticHeading = (float) Math.toDegrees(orientation[0]);
            compass.setHeading((magneticHeading + declination + 360f) % 360f, declination != 0f);
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) compass.setAccuracy(accuracy);
    }

    @Override public void onLocationChanged(Location location) {
        GeomagneticField field = new GeomagneticField((float) location.getLatitude(), (float) location.getLongitude(), (float) location.getAltitude(), System.currentTimeMillis());
        declination = field.getDeclination();
        compass.setGps(true);
    }
}
