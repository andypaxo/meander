package net.softwarealchemist.meander;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Application;

public class MeanderApp extends Application {
	private Tracker tracker;
	
	synchronized Tracker getTracker() {
		if (tracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			tracker = analytics.newTracker(R.xml.app_tracker);
		}
		return tracker;
	}
}
