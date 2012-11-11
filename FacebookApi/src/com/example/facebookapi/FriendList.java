package com.example.facebookapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FriendList extends Activity {
	
	private ListView     mListViewFriends;
	private String       TAG = "friend activity:";
	private Context      context;
	private FacebookInfo fbInfo;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.facebookapi.R.layout.friend_list);
        context = this;
        fbInfo = FacebookInfo.getInstance(context);
        
        mListViewFriends = (ListView) findViewById(R.id.xListViewFriends);
        
        mListViewFriends.setAdapter(new ArrayAdapter<String>(context, 
				android.R.layout.simple_list_item_1, 
				fbInfo.getFriendsNames()));	
        
        mListViewFriends.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				
				Intent i = new Intent(context, ChatManager.class);
				i.putExtra("id", fbInfo.getFriendId(position));
				startActivity(i);
				return false;
			}
		});
	}
}
