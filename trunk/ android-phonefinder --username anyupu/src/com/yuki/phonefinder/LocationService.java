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

import java.util.List;
import java.util.Locale;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.gsm.SmsManager;
import android.util.Log;

public class LocationService extends Service {
	LocationManager lm = null;
	Location location = null;
	String slocation = "unknown";
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onStart(final Intent intent, int startId) {
        super.onStart(intent, startId);
		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		location = lm.getLastKnownLocation("gps");
		if (location == null)
			location = lm.getLastKnownLocation("network");
		
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		Address addr;
		/*
		 * note: Address.toString() or Location.toString() is bug here , cause it will make a NULL POINTER EXCEPTION to sendTextMessage();
		 */
		try{
			Double lat = location.getLatitude();
			Double lon = location.getLongitude();
			List<Address> myList = gc.getFromLocation(lat, lon, 1);
			addr = myList.get(0);
			slocation = addr.getLocality();
			slocation += "|" + addr.getThoroughfare();
			slocation += "|" + addr.getFeatureName();
			slocation += "|" + addr.getSubAdminArea();
			slocation += "|Lat:" + Double.toString(lat);
			slocation += "|Lon:" + Double.toString(lon);
			
			PendingIntent dummyEvent = PendingIntent.getBroadcast(this, 0, new Intent("com.yuki.phonefinder.IGNORE_ME"), 0);
			
			SmsManager.getDefault().sendTextMessage(intent.getExtras().getString("dest"), null, slocation, dummyEvent, dummyEvent);
		}catch(Exception e){
			Log.e("PhoneFinder","LocationService Exception", e );
		}
	} 

}
