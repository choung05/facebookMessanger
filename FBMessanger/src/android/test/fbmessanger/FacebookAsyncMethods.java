package android.test.fbmessanger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FacebookAsyncMethods {
	private   static FacebookAsyncMethods instance = null;
	private static Context  context;
	private static FacebookInfo fbInfo;
	
	private FacebookAsyncMethods() {}
	public  static FacebookAsyncMethods getInstance(Context pContext) {
		context  = pContext;
		fbInfo = FacebookInfo.getInstance(context);
		if (instance == null) {
			instance = new FacebookAsyncMethods();
			return instance;
		} else {
			return instance;
		}
	}
	
	public void getFriendList() {
		AsyncGetFriends getFriends = new AsyncGetFriends();
		getFriends.execute();
	}
	public void createChats(Context pContext) {
		AsyncCreateChats chats = new AsyncCreateChats(pContext);
		chats.execute();
	}
	public void sendBundle (String graphPath, Bundle bundle, Context c) {
		AsyncSendBundle send = new AsyncSendBundle(graphPath, bundle, c);
		send.execute();
	} 
	public void logOut (Context pContext) {
		AsyncLogOut out = new AsyncLogOut(pContext);
		out.execute();
	}
 	
	/* use Facebook.requst to create async requst
	 * get friends list and save in FBFunctions */
	private class AsyncGetFriends extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog dialog = new ProgressDialog(context);
		private Object state = null;
		private String TAG = "AsyncGetFriends";
		private RequestListener listener = new RequestListener(){

			@Override
			public void onComplete(String response, Object state) {
				try {
					JSONObject json = Util.parseJson(response);
					JSONArray array = json.getJSONArray("data");
					
					for(int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						fbInfo.addFriendName(obj.getString("name"));
						fbInfo.addFriendId  (obj.getString("id"));
					}
				} catch (FacebookError e) {
					return;
				} catch (JSONException e) {
					return;
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
				Log.i(TAG, "IOException");
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				Log.i(TAG, "FileNotFound");
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				Log.i(TAG, "MalformedURL");
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				Log.i(TAG, "FacebookError");
			}};
	
		@Override
		protected void onPreExecute() {
			dialog.setMessage("get friends");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			if (fbInfo.initXmppConnection()) {
				try {
					String answer = fbInfo.getFacebook().request("me/friends", new Bundle(), "GET");
					listener.onComplete(answer, state);
				} catch (FileNotFoundException e) {
					
					listener.onFileNotFoundException(e, state);
					return false;
				} catch (MalformedURLException e) {
					listener.onMalformedURLException(e, state);
					return false;
				} catch (IOException e) {
					listener.onIOException(e, state);
					return false;				
				}
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result) {
				context.startService(new Intent(context, ChatServiceListener.class));
				MainActivity.initExpListView("offline");
				MainActivity.updateListView("Friends");
			} else
				Toast.makeText(context, "didn't get friend list", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected void onCancelled() {
			
		}
	}
	/* Send bundle to some graphPath*/
	private class AsyncSendBundle extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog dialog;
		private Object state = null;
		private Bundle bundle = null;
		private String graphPath = null;
		private String TAG = "AsyncSendBundle";
		private RequestListener listener = new RequestListener(){

			@Override
			public void onComplete(String response, Object state) {
				Log.i(TAG, "Photo send");
			}

			@Override
			public void onIOException(IOException e, Object state) {
				Log.i(TAG, "IOException");
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
				Log.i(TAG, "FileNotFound");
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
				Log.i(TAG, "MalformedURL");
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				Log.i(TAG, "FacebookError");
			}};
	
		public AsyncSendBundle (String pGraphPath, Bundle pBundle, Context c) {
			bundle = pBundle;
			graphPath = pGraphPath;
			dialog = new ProgressDialog(c);
		}
		@Override
		protected void onPreExecute() {
			dialog.setMessage("send bundle...");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String answer = fbInfo.getFacebook().request(graphPath, bundle, "POST");
				listener.onComplete(answer, state);
				return true;
			} catch (FileNotFoundException e) {
				listener.onFileNotFoundException(e, state);
				return false;
			} catch (MalformedURLException e) {
				listener.onMalformedURLException(e, state);
				return false;
			} catch (IOException e) {
				listener.onIOException(e, state);
				return false;				
			}
			//return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result) {
				Log.i(TAG, "bundle uploaded");
			} else
				Toast.makeText(context, "bundle didn't send", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected void onCancelled() {
			
		}
	}
	
	/* Creating XMPP connection and chats in other thread*/
	private class AsyncCreateChats extends AsyncTask<Void, Void, Void> {
		ProgressDialog dialog;
		public AsyncCreateChats(Context pContext) {
			 dialog = new ProgressDialog(pContext);
		};
		@Override
		protected void onPreExecute() {
			dialog.setMessage("creating chat's...");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			fbInfo.createMulltiChat();
			return null;
		}
		@Override
		protected void onPostExecute(Void v) {
			dialog.dismiss();
			Toast.makeText(context, "chats created", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected void onCancelled() {
			
		}
	}
	/* Sign out*/
	private class AsyncLogOut extends AsyncTask<Void, Void, Boolean> {
		private static final String TAG = "Async log out";
		Object state = null;
		Context context;
		ProgressDialog dialog;
		RequestListener listener = new RequestListener() {
			
			@Override
			public void onMalformedURLException(MalformedURLException e, Object state) {
				
			}
			
			@Override
			public void onIOException(IOException e, Object state) {
				
			}
			
			@Override
			public void onFileNotFoundException(FileNotFoundException e, Object state) {
				
			}
			
			@Override
			public void onFacebookError(FacebookError e, Object state) {
				
			}
			
			@Override
			public void onComplete(String response, Object state) {
				
			}
		};

		public AsyncLogOut(Context pContext) {
			dialog = new ProgressDialog(pContext);
			context = pContext;
		}
		@Override
		protected void onPreExecute() {
			dialog.setMessage("sign out...");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String response = fbInfo.getFacebook().logout(context);
				if (response.length() == 0 || response.equals("false")) {
					listener.onFacebookError(new FacebookError("auth.expireSession failed"), 
											 state);
					return false;
				}
			} catch (MalformedURLException e) {
				listener.onMalformedURLException(e, state);
				return false;
			} catch (IOException e) {
				listener.onIOException(e, state);
				return false;
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			if (result) {
				Log.i(TAG, "loging out complete");
			} else
				Toast.makeText(context, "you didn't sign out", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected void onCancelled() {
			
		}
	}
}
