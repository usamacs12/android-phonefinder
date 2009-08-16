/* Copyright (c) 2009 Kevin AN <anyupu@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yuki.phonefinder;

import java.lang.reflect.Method;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.gsm.SmsManager;
import android.util.Log;

public class LocationService extends Service {
	private NotificationManager mNM;
	LocationManager locationManager;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override  
	public void onCreate() {  
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}
	
	/*
	 * http://www.maximyudin.com/2008/12/07/android/vklyuchenievyklyuchenie-gps-na-g1-programmno/
	 */
	private boolean getGPSStatus()
	{
		String allowedLocationProviders =
			Settings.System.getString(getContentResolver(),
			Settings.System.LOCATION_PROVIDERS_ALLOWED);
	 
		if (allowedLocationProviders == null) {
			allowedLocationProviders = "";
		}
	 
		return allowedLocationProviders.contains(LocationManager.GPS_PROVIDER);
	}	

	private void setGPSStatus(boolean pNewGPSStatus)
	{
		String allowedLocationProviders =
			Settings.System.getString(getContentResolver(),
			Settings.System.LOCATION_PROVIDERS_ALLOWED);
	 
		if (allowedLocationProviders == null) {
			allowedLocationProviders = "";
		}
		
		boolean networkProviderStatus =
			allowedLocationProviders.contains(LocationManager.NETWORK_PROVIDER);
	 
		allowedLocationProviders = "";
		if (networkProviderStatus == true) {
			allowedLocationProviders += LocationManager.NETWORK_PROVIDER;
		}
		if (pNewGPSStatus == true) {
			allowedLocationProviders += "," + LocationManager.GPS_PROVIDER;
		}	
	 
		Settings.System.putString(getContentResolver(),
			Settings.System.LOCATION_PROVIDERS_ALLOWED, allowedLocationProviders);	   
	 
		try
		{
			Method m =
				locationManager.getClass().getMethod("updateProviders", new Class[] {});
			m.setAccessible(true);
			m.invoke(locationManager, new Object[]{});
		}
		catch(Exception e)
		{
			Log.e("%s:%s", e.getClass().getName());
		}
		return;
	}
	
	public void onStart(final Intent intent, int startId) {
        super.onStart(intent, startId);
        try{
	        if( ! getGPSStatus() )
	        	setGPSStatus(true);
	        }
        catch(Exception e)
		{
			Log.e("%s:%s", e.getClass().getName());
		}
        LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				PendingIntent dummyEvent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("com.yuki.phonefinder.IGNORE_ME"), 0);
				//Toast.makeText(getBaseContext(), getAddr(location), Toast.LENGTH_SHORT).show();
				SmsManager.getDefault().sendTextMessage(intent.getExtras().getString("dest"), null, Consts.getAddr(location, getApplicationContext()), dummyEvent, dummyEvent);
				
				locationManager.removeUpdates(this);
			}
            
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub

			}

			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
   
		Location location = locationManager.getLastKnownLocation("gps");
		if (location == null)
			location = locationManager.getLastKnownLocation("network");
		if (location != null){
			PendingIntent dummyEvent = PendingIntent.getBroadcast(this, 0, new Intent("com.yuki.phonefinder.IGNORE_ME"), 0);
			String dest = intent.getExtras().getString("dest");
			String sms = Consts.getAddr(location, this);
			SmsManager.getDefault().sendTextMessage(dest, null, sms, dummyEvent, dummyEvent);
		}
	}
	
	public void onDestroy(){
		
	}

}
