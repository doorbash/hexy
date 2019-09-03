package ir.doorbash.hexy.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.badlogic.gdx.backends.android.AndroidInput;

import java.util.List;

/**
 * Created by Milad Doorbash on 9/3/2019.
 */
public class SensorUtil {

    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;

    public static boolean isDeviceRotationAvailable(Context context) {
        if (isRotationVectorAvailable(context)) return true;
        return isAccelerometerAvailable(context) && isCompassAvailable(context);
    }

    public static boolean isGyroscopeAvailable(Context context) {
        boolean gyroscopeAvailable;
        SensorListener gyroscopeListener;
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager.getSensorList(Sensor.TYPE_GYROSCOPE).isEmpty()) {
            gyroscopeAvailable = false;
        } else {
            Sensor gyroscope = manager.getSensorList(Sensor.TYPE_GYROSCOPE).get(0);
            gyroscopeListener = new SensorListener();
            gyroscopeAvailable = manager.registerListener(gyroscopeListener, gyroscope, SENSOR_DELAY);
            if (gyroscopeAvailable) {
                manager.unregisterListener(gyroscopeListener);
            }
        }
        return gyroscopeAvailable;
    }

    public static boolean isRotationVectorAvailable(Context context) {
        boolean rotationVectorAvailable = false;
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        SensorListener rotationVectorListener;

        if (manager == null)
            manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> rotationVectorSensors = manager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
        if (!rotationVectorSensors.isEmpty()) {
            rotationVectorListener = new SensorListener();
            for (Sensor sensor : rotationVectorSensors) { // favor AOSP sensor
                if (sensor.getVendor().equals("Google Inc.") && sensor.getVersion() == 3) {
                    rotationVectorAvailable = manager.registerListener(rotationVectorListener, sensor, SENSOR_DELAY);
                    if (rotationVectorAvailable) {
                        manager.unregisterListener(rotationVectorListener);
                    }
                    break;
                }
            }
            if (!rotationVectorAvailable) {
                rotationVectorAvailable = manager.registerListener(rotationVectorListener, rotationVectorSensors.get(0), SENSOR_DELAY);
                if (rotationVectorAvailable) {
                    manager.unregisterListener(rotationVectorListener);
                }
            }
        }
        return rotationVectorAvailable;
    }

    public static boolean isAccelerometerAvailable(Context context) {
        boolean accelerometerAvailable;
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        SensorListener accelerometerListener;
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty()) {
            accelerometerAvailable = false;
        } else {
            Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            accelerometerListener = new SensorListener();
            accelerometerAvailable = manager.registerListener(accelerometerListener, accelerometer, SENSOR_DELAY);
            if (accelerometerAvailable) {
                manager.unregisterListener(accelerometerListener);
            }
        }
        return accelerometerAvailable;
    }

    public static boolean isCompassAvailable(Context context) {
        boolean compassAvailable;
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        SensorListener compassListener;
        if (manager == null)
            manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor != null) {
            compassAvailable = isAccelerometerAvailable(context);
            if (compassAvailable) {
                compassListener = new SensorListener();
                compassAvailable = manager.registerListener(compassListener, sensor, SENSOR_DELAY);
                if (compassAvailable) {
                    manager.unregisterListener(compassListener);
                }
            }
        } else {
            compassAvailable = false;
        }
        return compassAvailable;
    }
}
