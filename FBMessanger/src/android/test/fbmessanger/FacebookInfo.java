package android.test.fbmessanger;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import com.facebook.android.Facebook;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.RemoteViews;
import android.widget.Toast;

public class FacebookInfo {
	private static final String PLATFORM = "X-FACEBOOK-PLATFORM";
	private static final String API_KEY  = "112522162235388";
	public  static       String ACCESS_TOKEN;
	public  static 		 String me = "";
	private 			 Facebook       facebook = new Facebook(API_KEY);
	private 			 XMPPConnection xmppConnection;
	private 			 List<String>   friendsNames = new ArrayList<String>();
	private	  			 List<String>   friendsIds = new ArrayList<String>();
	
	/*history: String - user id; List<ChatFields> - messages*/
	private 			 Map<String, List<ChatFields>> history = 
							new HashMap<String, List<ChatFields>>();
	/*chat: collection users chats*/
	private				 List<Chat> chat = new ArrayList<Chat>();
	int num = 1;
	private static       FacebookInfo instance = null;
	private static       Context      context  = null;
	private static 		 Handler      handler  = new Handler();

	private FacebookInfo(){}
	
	public static FacebookInfo getInstance(Context pContext){
		if (instance == null) {
			instance = new FacebookInfo();
			context = pContext;
		}
		return instance;
	}
	
	public Facebook getFacebook(){
		return facebook;
	}
	public List<Chat> getChats() {
		return chat;
	}
	public void setChats(List<Chat> pChat) {
		if (chat.isEmpty()) 
			chat = pChat;
	}
	public Map<String, List<ChatFields>> getHistory() {
		return history;
	}
	public void setHistory(Map<String, List<ChatFields>> pHistory) {
		if (history.isEmpty()) 
			history = pHistory;
	}
	/* Get user's history(messages)
	 * @param userId: id, like 1354346123431
	 * @return list messages */
	public List<ChatFields> getUserHistory(String userId) {
		return history.get(userId);
	}
	/* create chat to each user*/
	public void createMulltiChat() {
		for(int i=0; i<friendsIds.size();i++) {
			String localId = friendsIds.get(i);
			chat.add(createChat(localId));
			addUserToHistory(localId, new ArrayList<ChatFields>());
		}
	}
	/* Add user to history map
	 * @param userId: id, like 1354346123431
	 * @param data: list messages*/
	public void addUserToHistory(String userId, List<ChatFields> data) {
		history.put(userId, data);
	}
	/* @return friends ids collection*/
	public List<String> getFriendsIds(){
		return friendsIds;
	}
	public void setFriendsIds (List<String> pIds) {
		if (friendsIds.isEmpty())
			friendsIds = pIds;
	}
	/* @return friends names collection*/
	public List<String> getFriendsNames(){
		return friendsNames;
	}
	public void setFriendsNames (List<String> pNames) {
		if (friendsNames.isEmpty())
			friendsNames = pNames;
	}
	/* @param  id:position in ListView
	 * @return friend id*/
	public String getFriendId(int id){
		return friendsIds.get(id);
	}
	/* @param  id:position in ListView
	 * @return  friend name*/
	public String getFriendName(int id){
		return friendsNames.get(id);
	}
	/* Add friend name to collection
	 * @param name: friend name*/
	public void addFriendName(String name){
		friendsNames.add(name);
	}
	/* Add friend id to collection
	 * @param id: friend id*/
	public void addFriendId(String id){
		friendsIds.add(id);
	}
	/* Create XMPP connection
	 * Use X-FACEBOOK-PLATFORM: facebook login and password
	 * @return true  - connection create
	 * @return false - something wrong*/
	public boolean initXmppConnection(){
		ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222);
    	config.setSASLAuthenticationEnabled(true);
    	config.setSendPresence(true);
    	xmppConnection = new XMPPConnection(config);
    	XMPPConnection.DEBUG_ENABLED = true;
    	
    	SASLAuthentication.registerSASLMechanism(PLATFORM, SASLMech.class);
    	SASLAuthentication.supportSASLMechanism (PLATFORM, 0);
    	
    	try {
    		xmppConnection.connect();
    		xmppConnection.login(API_KEY, ACCESS_TOKEN);
    		xmppConnection.sendPacket(new Presence(Presence.Type.available));
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
    	
    	return true;
	}
	
	/*Create facebook chat with user
	 * @param  userId: friend id
	 * @return Chat*/
	public Chat createChat(String userId){
		return xmppConnection.getChatManager()
			   .createChat( "-"+userId+"@chat.facebook.com", 
						     globalMessageListener);
		
	}
	/*Send message to current user
	 * @param chatId : friend id in ListView
	 * @param message: message body*/
	public void sendMessage(int chatId, String message){
		try {
			chat.get(chatId).sendMessage(message);
			
			ChatFields record = new ChatFields();
			record.setText(message);
			record.setName("me:");
			record.setTime(Calendar.getInstance().getTime().toLocaleString());
			record.setColor(true); // blue color
			
			//history.get(getFriendId(chatId)).add(record);
			addNewMessage(record, friendsIds.get(chatId));
			
			MainActivity.setHistoryListView();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	
	/* Take id like : 12435322453 or -2356443545@chat.facebook.com 
	 * and return position in lists
	 * @param id: friend id
	 * @return position list or -1 if didn't find*/
	public int who(String id){
		/* get -xxxxxxxx@chat.facebook.com*/
		if (id.endsWith("com")) {
			String[] p = id.split("@");
			id = p[0].substring(1);
			p = null;
		}
		/* find position in list*/
		for(int i = 0; i < friendsIds.size();i++) {
			String temp = friendsIds.get(i);
			if (temp.equals(id))
				return i;
		}
		return -1;
	}
	
	
	/* Create notification: name + message.
	 * Click on the notification - go to ChatManager
	 * @param pName   : friend name
	 * @param pMessage: text message
	 * @param pUserId : friend id like: 12454343453473 
	 * @param num     : notification № */
	public void createNotification(String pName, String pMessage, int pUserId, int num){
		Notification notification;
		RemoteViews  remoteViews;
		NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notification = new Notification(R.drawable.ic_notification, "new facebook message", System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

	    remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
	    remoteViews.setTextViewText(R.id.xTextViewNotificationName, pName);
	    remoteViews.setTextViewText(R.id.xTextViewNotificationBody, pMessage);
	    notification.contentView = remoteViews;
	   
	    Intent intent = new Intent(context, MainActivity.class);
	    intent.setAction("notification");
	    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // clear activity stack
	    intent.putExtra("position", pUserId);
	   
	    PendingIntent contentIntent = PendingIntent.getActivity(context, pUserId, intent, 0);
	    notification.contentIntent = contentIntent;
	    notifManager.notify(num, notification);
	}
	
	public void postMessage(){};
	public void postPhoto(){};
	
	private void addNewMessage(ChatFields pMessage, String pId) {
		if (history.get(friendsIds.get(who(pId))).size() > 19)
			history.get(friendsIds.get(who(pId))).remove(0);
			
		history.get(friendsIds.get(who(pId))).add(pMessage);
	}
	/* Actions what we do, when get incoming message
	 * Save message in current user history
	 * and refresh ListView or show notification */
	public MessageListener globalMessageListener = new MessageListener() {
		
		@Override
		public void processMessage(Chat arg0, Message message) {
			if (message.getBody() != null) {
				final ChatFields temp = new ChatFields();
				temp.setText(message.getBody());
				temp.setName(friendsNames.get(who(message.getFrom())));
				temp.setTime(Calendar.getInstance().getTime().toLocaleString());
				temp.setColor(false); // color red
				
				//history.get(friendsIds.get(who(message.getFrom()))).add(temp);
				addNewMessage(temp, message.getFrom());
				
				if(MainActivity.isActive) {
					if (MainActivity.currentFriend == who(message.getFrom())) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								MainActivity.setHistoryListView();
							}
						});
					}
					else {
						createNotification(friendsNames.get(who(message.getFrom())), 
								message.getBody(), 
								who(message.getFrom()), 
								num++);
					}
				} else {
					createNotification(friendsNames.get(who(message.getFrom())), 
							message.getBody(), 
							who(message.getFrom()), 
							num++);
				}
			}
		}
	};
	/* Check internet connection
	 * @param c: context
	 * @return true: have inet. conn.
	 * @return false: no internet conn.*/
	public boolean internetConnection(Context c) {
		ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected())
            return true;
        else
            return false;
	}
}
