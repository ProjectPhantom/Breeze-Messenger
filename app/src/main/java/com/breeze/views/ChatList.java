package com.breeze.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.breeze.R;
import com.breeze.packets.BrzChat;
import com.breeze.state.BrzStateObserver;
import com.breeze.state.BrzStateStore;

import java.util.ArrayList;

public class ChatList extends BaseAdapter implements BrzStateObserver {

    private ArrayList<BrzChat> chats = new ArrayList<>();
    private Context ctx;

    public ChatList(Context ctx) {
        this.ctx = ctx;

        BrzStateStore store = BrzStateStore.getStore();
        store.getAllChats(this);
    }

    @Override
    public void stateChange(ArrayList chats) {
        if(chats != null) {
            this.chats = chats;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public Object getItem(int i) {
        return chats.get(i);
    }

    public String getChatId(int i) {
        return chats.get(i).id;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        BrzChat chat = chats.get(i);
        LayoutInflater chatInflater = (LayoutInflater) ctx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ChatComponent chatCmp = new ChatComponent();

        convertView = chatInflater.inflate(R.layout.li_chat, null);
        convertView.setTag(chatCmp);

        chatCmp.chatName = (TextView) convertView.findViewById(R.id.chatName);
        chatCmp.chatName.setText(chat.name);

        return convertView;
    }
}

class ChatComponent {
    public TextView chatName;
}
