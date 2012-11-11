package com.example.facebookapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class ChatManager extends Activity {
	private static ListView         mListViewHistory;
	private        EditText         mEditTextMessage;
	public  static boolean          isActive = false; // flag: activity is active?
	private static Context          context;
	private static String 			chatId;			// chat id, like: 12453223446342
	
	private static FacebookInfo fbInfo;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.facebookapi.R.layout.chat);
        isActive = true;
        context = getApplicationContext();
     
        mListViewHistory = (ListView) findViewById(R.id.xListViewChatHistory);
        mEditTextMessage = (EditText) findViewById(R.id.xEditTextChatMessage);
        
        fbInfo = FacebookInfo.getInstance(this);
    }
	@Override
	public void onResume() {
		super.onResume();
		isActive = true;
		
		Intent i = getIntent();
		if (i != null)
			chatId = i.getExtras().getString("id"); // get chat id
		refreshListView();
	}
	@Override
	public void onPause() {
		super.onPause();
		isActive = false; 
	}
	public static void refreshListView() {
		ChatAdapter customAdapter = new ChatAdapter(context, fbInfo.getUserHistory(chatId));
		mListViewHistory.setAdapter(customAdapter);
		mListViewHistory.setSelection(customAdapter.getCount() - 1);
	}
	public void onClickChatSendMessage (View view) {
		if (!mEditTextMessage.getText().toString().equals(""))
			fbInfo.sendMessage(fbInfo.who(chatId),mEditTextMessage.getText().toString());
	}
}
