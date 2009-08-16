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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public final class Consts {
	/** Market link to details of this application. */
	static final String URL_MARKET_SEARCH =
	        "http://market.android.com/search?q=pname:com.yuki.phonefinder";
    /** Web site containing more information about this application. */
    static final String URL_INFO_LINK = "http://code.google.com/p/android-phonefinder/";
    
    public static final String PASSWORD_PREF_KEY = "VERIFYCODE";
    
    static public String getAddr(Location location, Context ctx){
		Double lat = location.getLatitude();
		Double lon = location.getLongitude();
		String slocation = "Lat:" + String.format("%f",lat);
		slocation += "|Lon:" + String.format("%f",lon);
		slocation += "\n" + String.format("http://maps.google.com/maps?q=%f", lat) + "%20" + String.format("%f", lon) + "\n"; 
		//Toast.makeText(this, slocation, Toast.LENGTH_SHORT).show();
		Geocoder gc = new Geocoder(ctx, Locale.getDefault());
		Address addr;
		try{
			List<Address> myList = gc.getFromLocation(lat, lon, 1);
			for(int i=0; i<myList.size(); i++){
				addr = myList.get(i);
				slocation += addr.getLocality();
				if( addr.getThoroughfare() != null )
					slocation += "|" + addr.getThoroughfare();
				if( addr.getFeatureName() != null )
					slocation += "|" + addr.getFeatureName();
				if( addr.getAdminArea() != null )
					slocation += "|" + addr.getAdminArea();
				if( addr.getSubAdminArea() != null )
					slocation += "|" + addr.getSubAdminArea();
				if( addr.getPostalCode() != null )
					slocation += "|" + addr.getPostalCode();
			}
		}
		catch(IOException  e){
			slocation += "[ERROR:network is unavailable!]";
		}
		catch(Exception  e){
			Log.e("PhoneFinder","LocationService Exception", e );
		}
		return slocation;
	}
}
