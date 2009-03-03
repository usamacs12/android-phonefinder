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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PhoneFinder extends Activity{
	private static final int DIALOG_TEXT_ENTRY = 7;
    private static final int MENU_INFO = 0;
    private static final int MENU_SHARE = 1;
    private static final int MENU_MARKET = 2;

    private Editor editor;
    private TextView tvVerifyCode;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        Button textEntry = (Button) findViewById(R.id.button_change);
        textEntry.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
                showDialog(DIALOG_TEXT_ENTRY);
            }
        });
        tvVerifyCode = (TextView)findViewById(R.id.textview_verify_code);
        SharedPreferences passwdfile = getSharedPreferences( Consts.PASSWORD_PREF_KEY, 0);
        String im = passwdfile.getString(Consts.PASSWORD_PREF_KEY, null);
        editor = passwdfile.edit();
		if(im == null){
			im = "Heydude";
			editor.putString(Consts.PASSWORD_PREF_KEY, im);
			editor.commit();
		}
		tvVerifyCode.setText(im);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_TEXT_ENTRY:
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
            return new AlertDialog.Builder(PhoneFinder.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_text_entry)
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	EditText et = (EditText)textEntryView.findViewById(R.id.password_edit);
                    	String vcode = et.getText().toString();
                    	if( !vcode.trim().equals("") ){
	                    	editor.putString(Consts.PASSWORD_PREF_KEY, vcode);
	                    	editor.commit();
	                		
	                		tvVerifyCode.setText(vcode);
                    	}
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    	dialog.dismiss();
                    }
                })
                .create();
        }
        return null;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_INFO, 0, R.string.menu_info).setIcon(
                android.R.drawable.ic_menu_info_details);
        menu.add(0, MENU_MARKET, 2, R.string.menu_market).setIcon(
                R.drawable.ic_menu_update);
        return true;
    }
    
    private void openLink(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case MENU_INFO:
            	new AlertDialog.Builder(this) 
			    .setTitle("Phone Finder") 
			    .setMessage("Version: 1.1.0\nAuthor: evin AN Email: anyupu@gmail.com website: " + Consts.URL_INFO_LINK) 
			    .show();
                return true;
            case MENU_SHARE:
                //share();
                return true;
            case MENU_MARKET:
                openLink(Consts.URL_MARKET_SEARCH);
                return true;
        }
        return false;
    }
}
