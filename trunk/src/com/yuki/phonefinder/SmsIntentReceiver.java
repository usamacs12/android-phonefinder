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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsIntentReceiver extends BroadcastReceiver
{	
	private void sendGPSData(Context context, Intent intent, SmsMessage inMessage)
	{
		LocationManager lm = null;
		Location location = null;
		String slocation = "unknown";
		
		lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		location = lm.getLastKnownLocation("gps");
		if (location == null)
			location = lm.getLastKnownLocation("network");
		
		Geocoder gc = new Geocoder(context, Locale.getDefault());
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
			
			PendingIntent dummyEvent = PendingIntent.getBroadcast(context, 0, new Intent("com.yuki.phonefinder.IGNORE_ME"), 0);
			
			SmsManager.getDefault().sendTextMessage(intent.getExtras().getString("dest"), null, slocation, dummyEvent, dummyEvent);
		}catch(Exception e){
			Log.e("PhoneFinder","LocationService Exception", e );
		}
		Toast.makeText(context, context.getResources().getString(R.string.notify_text) + slocation, Toast.LENGTH_SHORT).show();
	}
	
	private SmsMessage[] getMessagesFromIntent(Intent intent)
	{
		SmsMessage retMsgs[] = null;
		Bundle bdl = intent.getExtras();
		try{
			Object pdus[] = (Object [])bdl.get("pdus");
			retMsgs = new SmsMessage[pdus.length];
			for(int n=0; n < pdus.length; n++)
			{
				byte[] byteData = (byte[])pdus[n];
				retMsgs[n] = SmsMessage.createFromPdu(byteData);
			}	
			
		}catch(Exception e)
		{
			Log.e("GetMessages", "fail", e);
		}
		return retMsgs;
	}
	
	public void onReceive(Context context, Intent intent) 
	{
		
		if(!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
		{
			return;
		}
		SmsMessage msg[] = getMessagesFromIntent(intent);
		
		for(int i=0; i < msg.length; i++)
		{
			String message = msg[i].getDisplayMessageBody();
			if(message != null && message.length() > 0)
			{
				Log.i("MessageListener:",  message);
				
				//if(message.startsWith("LOCATE:"))
				if(message.contains("LOCATE:"))
				{
					String[] tokens = message.split("LOCATE:");
					if (tokens.length >= 2) 
					{
						SharedPreferences passwdfile = context.getSharedPreferences(Consts.PASSWORD_PREF_KEY, 0);
						String im = passwdfile.getString(Consts.PASSWORD_PREF_KEY, null);
						if (im.equals(tokens[1])) 
						{
							Intent mIntent = new Intent(context, LocationService.class);
							mIntent.putExtra("dest", msg[i].getOriginatingAddress());
							context.startService(mIntent);
							Toast.makeText(context, "LocationService Started!", Toast.LENGTH_SHORT).show();
						
						//	sendGPSData(context, intent, msg[i]);
						//	same as LocationService except "context", but is does not work, I don't know why.
						}
					}
				}
			}
		}
	}
}