package com.drivingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.drivingapp.NgsiServices.IntentServiceDeviceModel;
import com.drivingapp.NgsiServices.IntentServiceSensors;
import com.drivingapp.NgsiServices.ServiceDevice;
import com.drivingapp.NgsiServices.ServiceSendsDataDetectsConnection;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.uimanager.IllegalViewOperationException;
import www.fiware.org.ngsi.utilities.Constants;
import www.fiware.org.ngsi.utilities.DevicePropertiesFunctions;
import www.fiware.org.ngsi.utilities.Functions;

public class NgsiModule extends ReactContextBaseJavaModule {
  private Context context = getReactApplicationContext();
  private  double latitude, logitude;
  private Intent serviceDevice;
  IntentFilter filter;
  private float speedMS = 0, speedKmHr = 0;
  private DevicePropertiesFunctions deviceProperties;
  private Functions functions;
  private float speedValue =0;
  
  public NgsiModule(ReactApplicationContext reactContext) {
    super(reactContext);
    deviceProperties = new DevicePropertiesFunctions();
    functions = new Functions();
    // Filtro de acciones que serán alertadas
    filter = new IntentFilter(Constants.SERVICE_RUNNING_DEVICE);
    filter.addAction(Constants.SERVICE_RUNNING_DEVICEMODEL);
    filter.addAction(Constants.SERVICE_CHANGE_LOCATION_DEVICE);
    //filter.addAction(Constants.SERVICE_RUNNING_SENSORS);
    // Crear un nuevo ResponseReceiver
    ResponseReceiver receiver = new ResponseReceiver();
    // Registrar el receiver y su filtro
    LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(receiver, filter);
  }

  @Override
  public String getName() {
    return "NgsiModule";
  }



  
  @ReactMethod
  public void InitDeviceModel() {
    //Context context = getReactApplicationContext();
    Intent serviceIntent = new Intent(context, IntentServiceDeviceModel.class);
    context.startService(serviceIntent);
    Toast.makeText(getReactApplicationContext(), "DeviceModelService is Running...!", Toast.LENGTH_SHORT).show();
    
  }

  @ReactMethod 
  public void InitDevice(String owner) {
    serviceDevice = new Intent(context, ServiceDevice.class);
    serviceDevice.putExtra(Constants.OWNER, owner);
    context.startService(serviceDevice);
    // Crear un nuevo ResponseReceiver
    ResponseReceiver receiver = new ResponseReceiver();
    // Registrar el receiver y su filtro
    LocalBroadcastManager.getInstance(getReactApplicationContext()).registerReceiver(receiver, filter);
    Toast.makeText(getReactApplicationContext(), "DeviceService is Running...!", Toast.LENGTH_SHORT).show();
  }

  @ReactMethod
  public void InitBackingUp(){
    Intent serviceBackingUp = new Intent(context, ServiceSendsDataDetectsConnection.class);
    context.startService(serviceBackingUp);
    Toast.makeText(getReactApplicationContext(), "Backing-up Running...!", Toast.LENGTH_SHORT).show();
  }

  @ReactMethod
  public void deviceId(
       Callback successCallback,
      Callback errorCallback) {
    try {
      String  id = "Device_"+deviceProperties.getAndroidId(context);
      successCallback.invoke(id);
    } catch (IllegalViewOperationException e) {
      errorCallback.invoke(e.getMessage());
    }
  }

 @ReactMethod
  public void deviceModelId(
      Callback successCallback,
      Callback errorCallback
      ) {
    try {
      String  id = "DeviceModel_"+functions.getReplaceParent(deviceProperties.getBrand())+"_"+functions.getReplaceParent(deviceProperties.getModel());
      successCallback.invoke(id);
    } catch (IllegalViewOperationException e) {
      errorCallback.invoke(e.getMessage());
    }
  }

  

  @ReactMethod
  public void deviceSpeed( // Nueva funcion que envia los datos de velocidad a la vista 
      Callback successCallback,
      Callback errorCallback
      ) {
    try {
      successCallback.invoke(speedMS, speedKmHr); // Envio de parametros a la vista
    } catch (IllegalViewOperationException e) {
      errorCallback.invoke(e.getMessage());
    }
  }


  // Broadcast receiver que recibe las emisiones desde los servicios
  private class ResponseReceiver extends BroadcastReceiver {
    private ResponseReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case Constants.SERVICE_RUNNING_DEVICEMODEL:
          //Toast.makeText(getReactApplicationContext(), ""+intent.getStringExtra(Constants.SERVICE_RESULT_DEVICEMODEL), Toast.LENGTH_SHORT).show();
          break;
        case Constants.SERVICE_RUNNING_DEVICE:
          if(intent.getStringExtra(Constants.SERVICE_RESULT_DEVICE) != null)
            //Toast.makeText(getReactApplicationContext(), ""+intent.getStringExtra(Constants.SERVICE_RESULT_DEVICE), Toast.LENGTH_SHORT).show();
          break;
        case Constants.SERVICE_CHANGE_LOCATION_DEVICE:

          speedMS = intent.getFloatExtra(Constants.DEVICE_GPS_RESULT_SPEED_MS, 0);
          speedKmHr = intent.getFloatExtra(Constants.DEVICE_GPS_RESULT_SPEED_KMHR, 0);
          if(speedMS != 0 && speedKmHr != 0)
          //Toast.makeText(getReactApplicationContext(), "Speed: "+speedMS+"m/s ---- Speed: "+speedKmHr+"km/h", Toast.LENGTH_SHORT).show();
          Log.i("Speed: ", ""+speedMS+"m/s  "+speedKmHr+"km/h");
          break;
        case Constants.SERVICE_RUNNING_SENSORS:

          break;
      }
    }
  }
}