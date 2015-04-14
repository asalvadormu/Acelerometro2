package com.samuan.acelerometro2;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.os.Handler;

/**
 * Servicio encargado de escuchar el sensor acelerómetro y procesar los datos
 *
 * Created by SAMUAN on 13/04/2015.
 */
public class ServicioMuestreador extends Service implements SensorEventListener {

    private Monitor monitor;

    @Override
    public void onCreate() {
        Log.i("ServicioMuestreador","creando");
    }

    /**
     * Activa el servicio. Iniciar la captación de datos del sensor acelerómetro y arranca un
     * nuevo hilo donde se recibirán los eventos del sensor.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        monitor=new Monitor(getResources());
        SensorManager sensor=(SensorManager) getSystemService(SENSOR_SERVICE);
        HandlerThread handlerThread=new HandlerThread("hilosensor");
        handlerThread.start();
        Handler handler=new Handler(handlerThread.getLooper());
        sensor.registerListener(ServicioMuestreador.this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  20000, handler);
        return START_STICKY;
    }

    @Override
    public void onDestroy() { }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        monitor.gestionar(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {   }


}
