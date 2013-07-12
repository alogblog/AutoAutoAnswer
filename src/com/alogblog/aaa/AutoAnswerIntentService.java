package com.alogblog.aaa;
/*
 * AutoAnswer
 * Copyright (C) 2010 EverySoft
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *   Copyright (C) 2010 Tedd Scofield
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import java.lang.reflect.Method;
import java.util.List;

import com.android.internal.telephony.ITelephony;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class AutoAnswerIntentService extends IntentService {
	private static final String TAG = "AutoAnswerIntentService";

	// from Honeycomb.
	private BluetoothHeadset mBluetoothHeadset;
	private BluetoothAdapter mBluetoothAdapter;
	private Object mProfileListenerObject;
	// for below Honeycomb.
	private com.alogblog.aaa.BluetoothHeadset mBluetoothHeadset4Old;
	private boolean mHeadsetOnly = false;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate() {
		Context context = getBaseContext();
	
		super.onCreate();
		
		// Load preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mHeadsetOnly = prefs.getBoolean("headset_only", false);
	
		if (mHeadsetOnly && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
			Log.d(TAG, "onCreate/HONEYCOMB.");
			mProfileListenerObject = (BluetoothProfile.ServiceListener)new BluetoothProfile.ServiceListener() {
			    public void onServiceConnected(int profile, BluetoothProfile proxy) {
			        if (profile == BluetoothProfile.HEADSET) {
			            mBluetoothHeadset = (BluetoothHeadset) proxy;
			        	Log.d(TAG, "onCreate/HONEYCOMB/onServiceConnected.");
			        }
			    }
			    public void onServiceDisconnected(int profile) {
			        if (profile == BluetoothProfile.HEADSET) {
			            mBluetoothHeadset = null;
			            Log.d(TAG, "onCreate/HONEYCOMB/onServiceDisconnected.");
			        }
			    }
			};	
		}
	}

	public AutoAnswerIntentService() {
		super("AutoAnswerIntentService");
	}

	@SuppressLint("NewApi")
	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getBaseContext();
		List<BluetoothDevice> btDevices;
		boolean returnNow = false;
		
		// Load preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if ( mHeadsetOnly ) {
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if( mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() ) {
					mBluetoothAdapter.getProfileProxy(this, (BluetoothProfile.ServiceListener)mProfileListenerObject, BluetoothProfile.HEADSET);
					Log.d(TAG, "onHandleIntent/getProfileProxy.");
				}
				else {
					Log.d(TAG, "onHandleIntent/No BT adapter or deactivated.");
					return;
				}
			}
			else {
				mBluetoothHeadset4Old = new com.alogblog.aaa.BluetoothHeadset(this, null);
				Log.d(TAG, "onHandleIntent/aaa.BluetoothHeadset.");
			}
		}
		
		// Let the phone ring for a set delay
		try {
			Thread.sleep(Integer.parseInt(prefs.getString("delay", "2")) * 1000);
		} catch (InterruptedException e) {
			// We don't really care
		}

		// Check headset status right before picking up the call
		if ( mHeadsetOnly ) {
			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
				String logMessage = "";
				if ( mBluetoothAdapter.isEnabled() == false || mBluetoothHeadset == null ) {
					returnNow = true;
					logMessage = "onHandleIntent/closeProfileProxy/During waiting for answering, BT may be disabled.";
				}
				else {
					btDevices = mBluetoothHeadset.getConnectedDevices();
					if ( btDevices.isEmpty() ) {
						returnNow = true;
						logMessage = "onHandleIntent/closeProfileProxy/No connected BT devices.";
					}
					else if ( mBluetoothHeadset.getConnectionState(btDevices.get(0)) != BluetoothProfile.STATE_CONNECTED)  {
						returnNow = true;
						logMessage = "onHandleIntent/closeProfileProxy/No connected headset.";
					}
				}
				
				mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
				
				if( returnNow ) {
					Log.d(TAG, logMessage);
					return;
				}
	            Log.d(TAG, "onHandleIntent/closeProfileProxy/Headset connected.");
			}
			else {
				if ( mBluetoothHeadset4Old != null ) {
					try {
						if ( mBluetoothHeadset4Old.isConnected(mBluetoothHeadset4Old.getCurrentHeadset()) == false ) {
							mBluetoothHeadset4Old.close();
							return;
						}
						mBluetoothHeadset4Old.close();
					} catch(Exception e) {
						Log.e(TAG, "onHandleIntent/mBluetoothHeadset4Old : " + e.toString());
						return;
					}
				}
			}
		}
		
		// Make sure the phone is still ringing
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
			Log.d(TAG, "onHandleIntent/NO CALL_STATE_RINGING.");
			return;
		}
		
		// Answer the phone
		try {
			answerPhoneAidl(context);
			Log.d(TAG,"Until Android 2.2");			
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG,"From Android 2.3, Error trying to answer using telephony service.  Falling back to headset.");
			answerPhoneHeadsethook(context);
		}

		// Enable the speakerphone
		if (prefs.getBoolean("use_speakerphone", false)) {
			enableSpeakerPhone(context);
		}
		return;
	}

	private void enableSpeakerPhone(Context context) {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(true);
	}

	private void answerPhoneHeadsethook(Context context) {
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

		// froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);		
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void answerPhoneAidl(Context context) throws Exception {
		// Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService;
		telephonyService = (ITelephony)m.invoke(tm);

		// Silence the ringer and answer the call!
		telephonyService.silenceRinger();
		telephonyService.answerRingingCall();
	}
}
