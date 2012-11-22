package android.test.fbmessanger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;

import android.R.id;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/* class listen incomming messages*/
public class ChatServiceListener extends Service {
	private        String               TAG = "Service";
	private static Context              context;
	public  static boolean              isActive = false;
	private static FacebookInfo         fbInfo;
	private static FacebookAsyncMethods fbAsync;
	
	private static List<String> names = new ArrayList<String>();
	private static List<String> ids   = new ArrayList<String>();
	private static List<Chat> chats   = new ArrayList<Chat>();
	private static Map<String, List<ChatFields>> history = new HashMap<String, List<ChatFields>>();
	
	private static String me;
	private static int currentFriend;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        context = this;
        fbInfo  = FacebookInfo.getInstance(context);
        fbAsync = FacebookAsyncMethods.getInstance(context);
        
        createChatListener();
        isActive = true;
    
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return 0;
	}
	
	private void createChatListener() {
		fbAsync.createChats(MainActivity.getContext());
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		isActive = false;
	}
	public static void saveUserInformation() {
		names   = fbInfo.getFriendsNames();
		ids     = fbInfo.getFriendsIds();
		chats   = fbInfo.getChats();
		history = fbInfo.getHistory();
		me      = FacebookInfo.me;
		currentFriend = MainActivity.currentFriend;
	}
	public static void restoreUserInformation() {
		fbInfo.setFriendsNames(names);
		fbInfo.setFriendsIds(ids);
		fbInfo.setChats(chats);
		fbInfo.setHistory(history);
		FacebookInfo.me = me;
		MainActivity.currentFriend = currentFriend;
		
	/*	names.clear();
		ids.clear();
		chats.clear();
		history.clear();*/
	}
}
