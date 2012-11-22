package android.test.fbmessanger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

import android.media.FaceDetector.Face;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
	/* Data for expandable list view*/
	static List<Map<String, String>> group = new ArrayList<Map<String, String>>();
	static List<List<Map<String, String>>> children = new ArrayList<List<Map<String,String>>>();
	
	static Map<String, String> groupItem = new HashMap<String, String>();
	static List<Map<String, String>> childrenItem = new ArrayList<Map<String,String>>();
	/********************************************************/
	//private Button   buttonLogIn;
	private TextView textViewName;
	private EditText editTextMessage;
	private static ExpandableListView friendList;
	private static ListView messagesList;
	private static LinearLayout layout;
	private static SimpleExpandableListAdapter adapter;
	
	public  static int currentFriend = -1; // list id current friend
	// if we don't choose friend in list, the show last choosed
	private static boolean expListFlag = false;
	private final  int CAMERA = 1;
	private String photoPath = null; // path to photo in gallery
	private Vibrator vibrate;
	
	@Override  
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*hide keyboard*/
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        isActive = true;
        vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
       // buttonLogIn     = (Button)   findViewById(R.id.xButtonLogIn);
        textViewName    = (TextView) findViewById(R.id.xTextViewMyName);
        friendList      = (ExpandableListView) findViewById(R.id.xExpListViewFriends);
        messagesList    = (ListView) findViewById(R.id.xListViewMessages);
        editTextMessage = (EditText) findViewById(R.id.xEditTextMessage);
       // layout          = (LinearLayout) findViewById(R.id.xLinearLayoutMessageField);
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
						//layout.setVisibility(View.VISIBLE);
						messagesList.setVisibility(View.VISIBLE);
						expListFlag = false; 
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
					if (expListFlag) { // close list
						//layout.setVisibility(View.VISIBLE);
						messagesList.setVisibility(View.VISIBLE);
						expListFlag = false;
					} else { // open list first time
						//layout.setVisibility(View.INVISIBLE);
						messagesList.setVisibility(View.INVISIBLE);
						expListFlag = true;
					}
				return false;
			}
		});
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
  
        if (requestCode == CAMERA) {
        	if (data != null) {
        		photoPath = getPathPhoto(data.getData());
        		showDialog(1);
        	} else 
        		photoPath = null;
        } else {
        	fbInfo.getFacebook().authorizeCallback(requestCode, resultCode, data);
        }
    }
    /* Change image quality*/
    private static Bitmap reSizeImage(String pPath, int pHeight, int pWidth) {
    	
    	Bitmap image;
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(pPath, options);
    	
    	int height = options.outHeight, 
    		width = options.outWidth;
    	int scale = 1;
    	
    	while(true) {
    		if (width < pWidth || height < pHeight)
    			break;
    		width /=2;
    		height /=2;
    		scale *=2;
    	}
    	options.inJustDecodeBounds = false;
    	options.inPurgeable = true;
    	options.inSampleSize = scale;
    	image = BitmapFactory.decodeFile(pPath, options);
    	
    	return image;
    }
   
    public void onClickCamera(View view) {
    	vibrate.vibrate(50);
    	if (currentFriend != -1)
    		startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	isActive = false;

    	SessionStore.save(fbInfo.getFacebook(), context);
    	if (ChatServiceListener.isActive) {
    		Log.i(TAG, "save information..");
    		ChatServiceListener.saveUserInformation();
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	isActive = true;
    	fbInfo = FacebookInfo.getInstance(context);
    	SessionStore.restore(fbInfo.getFacebook(), context);
    	/* When app resume, check session and restore information 
    	 * from service or get new from FB*/
    	if (fbInfo.getFacebook().isSessionValid()) {
    		if (ChatServiceListener.isActive) {
	    		ChatServiceListener.restoreUserInformation();
	    		group.clear();
	    		groupItem.clear();
	    		initExpListView("status"); 
	    		setHistoryListView();
	    		updateListView(fbInfo.getFriendName(currentFriend));
	    	//textViewName.setText(FacebookInfo.me);
	    	} 
    	}
    	/* Open app from notification*/
    	if (getIntent().getAction().equals("notification")) {
    		Bundle b = getIntent().getExtras();
	    	if (b != null) {
	    		currentFriend = getIntent().getExtras().getInt("position");
	    		updateListView(fbInfo.getFriendName(currentFriend));
	    		setHistoryListView();
	    	}
    	}
    }
   
    /*
	  * take String path to image from gallery
	  * from Uri */
	private String getPathPhoto(Uri uri) {

		Cursor cursor = getContentResolver().query(uri, new String[] {MediaStore.Images.Media.DATA}, null, null, null);
		cursor.moveToFirst();
		String path = cursor.getString(0); // get path to file
		cursor.close();

		return path;
	}
	/* Set adapter for ListView*/
    public static void setHistoryListView () {
    	ChatAdapter localAdapter = new ChatAdapter(context, fbInfo.getUserHistory(fbInfo.getFriendId(currentFriend)));
    	messagesList.setAdapter(localAdapter);
    	messagesList.setSelection(localAdapter.getCount() - 1);
    }
    /* Init expandable List View*/
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
    /* Update expandable list view*/
    public static void updateListView(String groupName) {
    	group.remove(0);
    	groupItem.remove("NAME");
    	
    	groupItem.put("NAME", groupName);
    	groupItem.put("SIGNATURE", "choose friend");
    	group.add(groupItem);
    	
    	friendList.setAdapter(adapter);
    } 
  
    /* Set arrow in expadable list view to the right*/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        friendList.setIndicatorBounds(friendList.getRight() - 40, friendList.getWidth());
    }
    
    public void onClickExit(View view) {
    	vibrate.vibrate(50);
    	if (ChatServiceListener.isActive) {
    		stopService(new Intent(context, ChatServiceListener.class));
    	}
    	finish();
    }
    // TODO: auto change icons
    public void onClickLogInOut(View view) {
    	vibrate.vibrate(50);
    	if(ChatServiceListener.isActive)
    		logOut();
    	else
    		logIn();
    }
    
    public void onClickSend(View view) {
    	if (!editTextMessage.getText().toString().equals("") && currentFriend != -1) {
    		fbInfo.sendMessage(currentFriend, 
    				           editTextMessage.getText().toString());
    		editTextMessage.setText("");
    	}
    }
    
    private boolean logIn() {
		if (fbInfo.internetConnection(context)) {
			if (!fbInfo.getFacebook().isSessionValid()) {
				fbInfo.getFacebook().authorize(this, 
						new String[] {"publish_stream", "xmpp_login", "offline_access"}, 
						Facebook.FORCE_DIALOG_AUTH, new DialogListener() {

							@Override
							public void onComplete(Bundle values) {
								try {
			  						final JSONObject obj = new JSONObject(fbInfo.getFacebook().request("me"));
			  						FacebookInfo.ACCESS_TOKEN = fbInfo.getFacebook().getAccessToken();
			  						FacebookInfo.me = obj.getString("name");
			  						String [] temp = FacebookInfo.me.split(" ");
			  						FacebookInfo.me = temp[0] + "\n" + temp[1];
			  						asyncFB.getFriendList(); // call asyncTask
			  						
			  						runOnUiThread(new Runnable() {
										@Override
										public void run() {
										//	textViewName.setText(FacebookInfo.me);
											//buttonLogIn.setBackgroundResource(R.drawable.ic_sign_click);
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
			} else { // if session invalid
				JSONObject obj;
				try {
					obj = new JSONObject(fbInfo.getFacebook().request("me"));
					FacebookInfo.me = obj.getString("name");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
					FacebookInfo.ACCESS_TOKEN = fbInfo.getFacebook().getAccessToken();
					//textViewName.setText(FacebookInfo.me);
					asyncFB.getFriendList();
			}
		} else { 
			Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
          	return false;
		}
		return true;
	}
    /* sign out*/
    private boolean logOut() {
		if (fbInfo.getFacebook().isSessionValid()) {
			if (fbInfo.internetConnection(context)) {
				asyncFB.logOut(context);
				//buttonLogIn.setBackgroundResource(R.drawable.ic_sign);
			}
			else {
				Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
	          	return false;
			}
		}
		else
			return true;
    	return true;
	}

    public void onClickWall(View view) {
    	vibrate.vibrate(50);
    	if (currentFriend != -1)
    		showDialog(2);
    }
    @Override
    protected Dialog onCreateDialog(int pId) {
    	
    	View v = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog, null);
    	Dialog d = new Dialog(context, R.style.myDialog);
    	d.setContentView(v/*, params*/);
   
    	return d;
    }
    @Override
	protected void onPrepareDialog(final int id, final Dialog dialog) {
	    super.onPrepareDialog(id, dialog);
	    
	          Button send   = (Button)   dialog.getWindow().findViewById(R.id.xButtonDialogSend);
	          Button cancel = (Button)   dialog.getWindow().findViewById(R.id.xButtonDialogCancel);
	    final EditText edit = (EditText) dialog.getWindow().findViewById(R.id.xEditTextDialog);
	    
	    send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(id) {
				case 1: // send photo
					Bitmap bitmap = reSizeImage(photoPath, 640, 480);//BitmapFactory.decodeFile(photoPath);
	        		ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	        		byte[] array = stream.toByteArray();
	        		
	        		Bundle bundle = new Bundle();
	        		bundle.putString   (Facebook.TOKEN, FacebookInfo.ACCESS_TOKEN);
	        		bundle.putString   ("message", edit.getText().toString());
	        		bundle.putByteArray("picture", array);
	        		
	        		asyncFB.sendBundle(fbInfo.getFriendId(currentFriend) + "/photos", bundle, MainActivity.this);
	        		dialog.dismiss();
					break;
				case 2: // send message to wall
					if (!edit.getText().toString().equals("")) {
						Bundle b = new Bundle();
						b.putString("message", edit.getText().toString());
						asyncFB.sendBundle(fbInfo.getFriendId(currentFriend)+"/feed", 
										   b, 
										   context); 
					}
					dialog.dismiss();
					break;
				default: break;
				}
        		
			}
		});
	    
	    cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (id) {
				case 1:
					photoPath = null;
					dialog.dismiss();
					break;
				case 2:
					dialog.dismiss();
					break;
				default:
					break;
				}
			}
		});
    }

    public static Context getContext() {
    	return context;
    }
}
