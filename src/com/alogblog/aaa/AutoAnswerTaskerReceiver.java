package com.alogblog.aaa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class AutoAnswerTaskerReceiver extends BroadcastReceiver {

	private static final String BOOLEAN_ENABLED = "enabled";
	private static final String BOOLEAN_USE_SPEAKERPHONE = "use_speakerphone";
	private static final String BOOLEAN_HEADSET_ONLY = "headset_only";
	private static final String BOOLEAN_NO_SECOND_CALL = "no_second_call";
	private static final String STRING_WHICH_CONTACTS = "which_contacts";
	private static final String STRING_DELAY = "delay";
	private static final String AUTOANSWER_BUNDLE_NAME = "com.alogblog.aaa.extra.FROM_PLUGIN";
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Bundle bundle = intent.getBundleExtra(AUTOANSWER_BUNDLE_NAME);

		boolean enabled = bundle.getBoolean(BOOLEAN_ENABLED, false);
		boolean use_speakerphone = bundle.getBoolean(BOOLEAN_USE_SPEAKERPHONE, false);
		boolean headset_only = bundle.getBoolean(BOOLEAN_HEADSET_ONLY, false);
		boolean no_second_call = bundle.getBoolean(BOOLEAN_NO_SECOND_CALL, false);
		String which_contacts = bundle.getString(STRING_WHICH_CONTACTS);
		// Re-check data validity
		if (!which_contacts.equals("contacts") && !which_contacts.equals("starred")) {
			which_contacts = "all";
		}
		// Re-check data validity
		String delay = bundle.getString(STRING_DELAY);
		if(!delay.equals("4") && !delay.equals("6") && !delay.equals("8") && !delay.equals("10") && !delay.equals("12")) {
        	delay = "2";
        }
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edt = prefs.edit();
		edt.putBoolean(BOOLEAN_ENABLED, enabled);
		edt.putBoolean(BOOLEAN_USE_SPEAKERPHONE, use_speakerphone);
		edt.putBoolean(BOOLEAN_HEADSET_ONLY, headset_only);
		edt.putBoolean(BOOLEAN_NO_SECOND_CALL, no_second_call);
		edt.putString(STRING_WHICH_CONTACTS, which_contacts);
		edt.putString(STRING_DELAY, String.valueOf(delay));
		
		edt.commit();
		
		AutoAnswerNotifier notifier = new AutoAnswerNotifier(context);
		notifier.updateNotification();
	}
}