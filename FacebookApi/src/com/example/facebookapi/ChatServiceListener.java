package com.example.facebookapi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ChatServiceListener extends Service {
	private        String           TAG = "Service";
	private static Context          context;
	public  static boolean isActive = false;
	private static FacebookInfo fbInfo;
	private static FacebookAsyncMethods fbAsync;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "serive create");
        context = getApplicationContext();
        fbInfo = FacebookInfo.getInstance(context);
        fbAsync = FacebookAsyncMethods.getInstance(context);
        
        createChatListener();
        Log.i(TAG, "chats created");
        isActive = true;
    
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return 0;
	}
	
	private void createChatListener() {
		boolean localResult = fbInfo.initXmppConnection();
		if (localResult) {
			fbInfo.createMulltiChat();
			//fbAsync.createChats();
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		isActive = false;
	}
	
}
