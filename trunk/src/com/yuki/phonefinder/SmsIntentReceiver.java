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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

public class SmsIntentReceiver extends BroadcastReceiver
{	
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
				
				SharedPreferences passwdfile = context.getSharedPreferences(Consts.PASSWORD_PREF_KEY, 0);
				String im = passwdfile.getString(Consts.PASSWORD_PREF_KEY, null);
					
				if (message.trim().toLowerCase().contains(im.toLowerCase())) // Verify code included is ok!
				{
					Intent mIntent = new Intent(context, LocationService.class);
					mIntent.putExtra("dest", msg[i].getOriginatingAddress());
					context.startService(mIntent);
					/*
					 * a user said:
					 * 
					 * Thank you! Been waiting for someone to get this right! A
					 * few suggestions for future updates:
					 * 
					 * - fine tune location. Was off by 1 block. 
					 * - change  LOCATE: xxxxx to something innoculous like. HEYDUDE : xxxxx 
					 * - do not show on 'lost phone' that location info has been sent. (May already do this, tested with my own phone)
					 * 
					 * Keep up the great work!
					 * 
					 * Mario Sellitti mario@mariosellitti.com (sent from my
					 * mobile)
					 */
					// Toast.makeText(context, "LocationService Started!", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
	}
}