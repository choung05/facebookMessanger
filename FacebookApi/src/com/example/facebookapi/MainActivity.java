package com.example.facebookapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.facebookapi.R;
import com.example.facebookapi.R.id;
import com.example.facebookapi.R.string;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

import android.os.Bundle;
import android.app.Activity;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

public class MainActivity extends Activity {

	private             String TAG      = "Main: ";
	public static final String PLATFORM = "X-FACEBOOK-PLATFORM";
	public static final String API_KEY  = "112522162235388";
	public static       String ACCESS_TOKEN = "";

	private static      Context              context;
	private 			FacebookAsyncMethods asyncFB;
	private static 		FacebookInfo         fbInfo;
	public static boolean isActive = false;
	/********************************************************/
	static List<Map<String, String>> group = new ArrayList<Map<String, String>>();
	static List<List<Map<String, String>>> children = new ArrayList<List<Map<String,String>>>();
	
	static Map<String, String> groupItem = new HashMap<String, String>();
	static List<Map<String, String>> childrenItem = new ArrayList<Map<String,String>>();
	/********************************************************/
	
	private Button buttonSend;
	private TextView textViewName;
	private EditText editTextMessage;
	private static ExpandableListView friendList;
	private static ListView messagesList;
	private static SimpleExpandableListAdapter adapter;
	
	public static int currentFriend = -1;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.facebookapi.R.layout.main);
        /*hide keyboard*/
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        isActive = true;
        buttonSend      = (Button) findViewById(R.id.xButtonSend);
        textViewName    = (TextView) findViewById(R.id.xTextViewMyName);
        friendList      = (ExpandableListView) findViewById(R.id.xExpListViewFriends);
        messagesList    = (ListView) findViewById(R.id.xListViewMessages);
        editTextMessage = (EditText) findViewById(R.id.xEditTextMessage);
        context = this;
        asyncFB = FacebookAsyncMethods.getInstance(context);
        fbInfo  = FacebookInfo.getInstance(context);
       // logOut();
        adapter = new SimpleExpandableListAdapter
        		(context, 
        				group, 
        				android.R.layout.simple_expandable_list_item_1,
        				new String[] {"NAME", "SIGNATURE"}, 
        				new int[] {android.R.id.text1, android.R.id.text2},
        				children, 
        				android.R.layout.simple_expandable_list_item_2, 
        				new String[] {"NAME", "SIGNATURE"}, 
        				new int[] {android.R.id.text1, android.R.id.text2});
    	
        friendList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, final int childPosition, long id) {
				
				parent.collapseGroup(groupPosition);
				currentFriend = childPosition;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateListView(childrenItem.get(childPosition).get("NAME"));
						
						messagesList   .setVisibility(ListView.VISIBLE);
						buttonSend     .setVisibility(Button.VISIBLE);
						editTextMessage.setVisibility(EditText.VISIBLE);
						setHistoryListView();
					}
				});
				return false;
			}
		});
        friendList.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
					
					//Log.i("", "count " + parent.get);
					messagesList.setVisibility(ListView.INVISIBLE);
					buttonSend.setVisibility(Button.INVISIBLE);
					editTextMessage.setVisibility(EditText.INVISIBLE);
			
				return false;
			}
		});
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        fbInfo.getFacebook().authorizeCallback(requestCode, resultCode, data);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	isActive = false;
    	// TODO save session
    	//SessionStore.save(fbInfo.getFacebook(), context);
    	//Toast.makeText(context, "onPause", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	isActive = true;
    	// TODO restore session
    	//SessionStore.restore(fbInfo.getFacebook(), context);
    	
    	if (getIntent().getAction().equals("notofication")) {
    		Bundle b = getIntent().getExtras();
	    	if (b != null) {
	    		currentFriend = getIntent().getExtras().getInt("position");
	    		updateListView(fbInfo.getFriendName(currentFriend));
	    		setHistoryListView();
	    	}
    	}
    }
   
    public static void setHistoryListView () {
    	ChatAdapter localAdapter = new ChatAdapter(context, fbInfo.getUserHistory(fbInfo.getFriendId(currentFriend)));
    	messagesList.setAdapter(localAdapter);
    	messagesList.setSelection(localAdapter.getCount() - 1);
    }
    public static void initExpListView(String status) {
    	groupItem.put("NAME", "Friends");
    	groupItem.put("SIGNATURE", "choose friend");
    	group.add(groupItem);
    	
    	for(int i=0; i<fbInfo.getFriendsNames().size(); i++) {
    		String item = fbInfo.getFriendName(i);
    		
    		Map<String, String> temp = new HashMap<String, String>();
    		temp.put("NAME", item);
    		temp.put("SIGNATURE", status);
    		
    		childrenItem.add(temp);
    	}
    	children.add(childrenItem);
    	//friendList.setAdapter(adapter);
    }
    public static void updateListView(String groupName) {
    	group.remove(0);
    	groupItem.remove("NAME");
    	
    	groupItem.put("NAME", groupName);
    	groupItem.put("SIGNATURE", "choose friend");
    	group.add(groupItem);
    	
    	friendList.setAdapter(adapter);
    } 
  
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        friendList.setIndicatorBounds(friendList.getRight() - 40, friendList.getWidth());

    }
    
    public void onClickExit(View view) {
    	if (ChatServiceListener.isActive) {
    		stopService(new Intent(context, ChatServiceListener.class));
    	}
    	finish();
    }
    
    public void onClickLogInOut(View view) {
    	logIn();
    }
    
    public void onClickSend(View view) {
    	if (!editTextMessage.getText().toString().equals(""))
    		fbInfo.sendMessage(currentFriend, 
    				           editTextMessage.getText().toString());
    }
    
    private boolean logIn() {
		if (fbInfo.internetConnection(context)) {
		//	if (!fbInfo.getFacebook().isSessionValid()) {
				fbInfo.getFacebook().authorize(this, 
						new String[] {"publish_stream", "xmpp_login", "offline_access"}, 
						Facebook.FORCE_DIALOG_AUTH, new DialogListener() {

							@Override
							public void onComplete(Bundle values) {
								try {
			  						final JSONObject obj = new JSONObject(fbInfo.getFacebook().request("me"));
			  						FacebookInfo.ACCESS_TOKEN = fbInfo.getFacebook().getAccessToken();
			  						
			  						asyncFB.getFriendList();
			  						
			  						runOnUiThread(new Runnable() {
										@Override
										public void run() {
											try {
												textViewName.setText(obj.getString("name"));
											} catch (JSONException e) {
												e.printStackTrace();
											}
										}
									});
			  					} catch (MalformedURLException e) {
			  						return;
			  					} catch (JSONException e) {
			  						return;
			  					} catch (IOException e) {
			  						return;
			  					}
							}

							@Override
							public void onFacebookError(FacebookError e) {
								return;
							}

							@Override
							public void onError(DialogError e) {
								return;
							}

							@Override
							public void onCancel() {
								return;
							}});
		//	}
		//	else {
		//		return true;
		//	}
		} else {
			Toast.makeText(context, com.example.facebookapi.R.string.s_no_internet, Toast.LENGTH_SHORT).show();
          	return false;
		}
	//	Log.i(TAG, "logIn complete");
		return true;
	}

    private void logOut() {
		/*if (fbInfo.getFacebook().isSessionValid()) {
			if (fbInfo.internetConnection()) {
				
				fbInfo.getFBRunner().logout(mContext, new RequestListener() {
					
					@Override
					public void onMalformedURLException(MalformedURLException e, Object state) {
						Log.i(TAG, "onMalformedURLException");
					}
					
					@Override
					public void onIOException(IOException e, Object state) {
						Log.i(TAG, "onIOException");
					}
					
					@Override
					public void onFileNotFoundException(FileNotFoundException e, Object state) {
						Log.i(TAG, "onFileNotFoundException");
					}
					
					@Override
					public void onFacebookError(FacebookError e, Object state) {
						Log.i(TAG, "onFacebookError");
					}
					
					@Override
					public void onComplete(String response, Object state) {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Toast.makeText(mContext, com.example.facebookapi.R.string.s_sign_out, Toast.LENGTH_SHORT).show();
								mTextViewName.setText("sign in, please");
						
							}
						});
					}
				});
			}
			else {
				Toast.makeText(mContext, com.example.facebookapi.R.string.s_no_internet, Toast.LENGTH_SHORT).show();
	          	return false;
			}
		}
		else
			return true;
    	return true;*/
	}
}
