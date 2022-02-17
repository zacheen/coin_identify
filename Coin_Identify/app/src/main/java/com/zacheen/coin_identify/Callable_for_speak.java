package com.zacheen.coin_identify;

import android.app.Activity;

import java.util.concurrent.Callable;

public class Callable_for_speak implements Callable<String> {
	String s;
	int sleep;
	Activity activity;
	public Callable_for_speak(String s, int sleep, Activity activity) {
		super();
		this.s = s;
		this.sleep = sleep;
		this.activity = activity;
	}
	
	@Override
	public String call() throws Exception {
		Thread.sleep(sleep);
		GoogleTextToSpeech gtts = new GoogleTextToSpeech(); // make instance gtts
	    gtts.say(s, "en",activity); // use method say
		Thread.sleep(5000);
		return null;
	}

}
