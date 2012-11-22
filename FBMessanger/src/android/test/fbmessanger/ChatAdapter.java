package android.test.fbmessanger;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/* Custom ListView adapter*/
public class ChatAdapter extends BaseAdapter{

	List<ChatFields> object;
	LayoutInflater inflater;
	ChatAdapter(Context context, List<ChatFields> pObjects) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		object = pObjects;
	}
	@Override
	public int getCount() {
		
		return object.size();
	}

	@Override
	public Object getItem(int position) {
		return object.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = inflater.inflate(R.layout.chat_row, null);
		
		ChatFields record = (ChatFields) getItem(position);
		
		((TextView) view.findViewById(R.id.xTextViewChatWho)).
									setText(record.getName());
		((TextView) view.findViewById(R.id.xTextViewChatMessage)).
									setText(record.getText());
		((TextView) view.findViewById(R.id.xTextViewChatWhen)).
									setText(record.getTime());
		if (record.getColor())  // if it's me - color:blue
			((LinearLayout) view.findViewById(R.id.xLayoutColor)).setBackgroundColor(0x441255AA);
		else
			((LinearLayout) view.findViewById(R.id.xLayoutColor)).setBackgroundColor(0x44aa1255);
		return view;
	}

}
