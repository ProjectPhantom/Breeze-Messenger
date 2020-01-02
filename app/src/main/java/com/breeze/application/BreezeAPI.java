package com.breeze.application;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.breeze.App;
import com.breeze.MainActivity;
import com.breeze.R;
import com.breeze.database.DatabaseHandler;
import com.breeze.datatypes.BrzNode;
import com.breeze.datatypes.BrzChat;
import com.breeze.datatypes.BrzMessage;
import com.breeze.graph.BrzGraph;
import com.breeze.packets.BrzPacket;
import com.breeze.packets.ChatEvents.BrzChatHandshake;
import com.breeze.packets.ChatEvents.BrzChatResponse;
import com.breeze.router.BrzRouter;
import com.breeze.state.BrzStateStore;
import com.breeze.storage.BrzStorage;

public class BreezeAPI extends Service {

    // Singleton

    private static BreezeAPI instance = new BreezeAPI();

    public static BreezeAPI getInstance() {
        return instance;
    }

    // Behavioral Modules

    final private String ACTION_STOP_SERVICE = "STOP THIS NOW";

    public BrzRouter router = null;
    public BrzStorage storage = null;
    public BrzStateStore state = null;
    public DatabaseHandler db = null;

    // Api modules

    public BreezeMetastateModule meta = null;
    public BreezeEncryptionModule encryption = null;

    // Data members

    public BrzNode hostNode = null;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // Initialize api modules
        this.router = BrzRouter.initialize(this, "BREEZE_MESSENGER");
        this.storage = BrzStorage.initialize(this);
        this.state = BrzStateStore.getStore();
        this.db = new DatabaseHandler(this);

        // Initialize api modules
        this.encryption = new BreezeEncryptionModule(this);
        this.meta = new BreezeMetastateModule(this);

        // Upgrade to foreground process
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, notifIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent stopSelf = new Intent(this, BreezeAPI.class);
        stopSelf.setAction(this.ACTION_STOP_SERVICE);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, 0);

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Breeze Service")
                .setContentText("Breeze is running in the background")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pending)
                .addAction(R.drawable.ic_launcher, "Stop", pStopSelf)
                .build();

        startForeground(1, notification);

        Intent shellIntent = new Intent(this, MainActivity.class);
        shellIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(shellIntent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
            stopSelf();
        }

        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    //
    //
    // Host Node
    //
    //

    public void setHostNode(BrzNode hostNode) {
        if (hostNode == null)
            return;

        // Set up encryption
        this.encryption.setHostNode(hostNode);

        this.hostNode = hostNode;

        // Set our id in preferences
        SharedPreferences sp = getSharedPreferences("Breeze", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(App.PREF_HOST_NODE_ID, hostNode.id);
        editor.apply();

        // Push the change elsewhere
        this.router.start(hostNode);
        this.state.setHostNode(hostNode);
        this.db.setNode(hostNode);
    }

    //
    //
    // Chat handshakes
    //
    //

    public void sendChatHandshakes(BrzChat chat) {
        chat.acceptedByHost = true;
        chat.acceptedByRecipient = false;

        if (!chat.nodes.contains(this.hostNode.id))
            chat.nodes.add(this.hostNode.id);

        BrzChatHandshake handshake = new BrzChatHandshake(this.router.hostNode.id, chat);
        encryption.makeSecretKey(handshake);

        BrzPacket p = new BrzPacket(handshake, BrzPacket.BrzPacketType.CHAT_HANDSHAKE, "", false);

        for (String nodeId : chat.nodes) {
            if (nodeId.equals(hostNode.id))
                continue;
            p.to = nodeId;
            this.router.send(p);
        }

        this.state.addChat(chat);
        this.db.setChat(chat);
    }

    public void updateChat(BrzChat chat) {
        this.state.addChat(chat);
        this.db.setChat(chat);
    }

    public void incomingChatResponse(BrzChatResponse response) {
        BrzChat c = this.state.getChat(response.chatId);
        if (c == null)
            return;

        // Chat accepted!
        if (response.accepted) {
            c.acceptedByRecipient = true;

            BrzNode n = BrzGraph.getInstance().getVertex(response.from);
            if (n != null) {
                BrzMessage sm = new BrzMessage(n.name + " accepeted the chat request!");
                sm.chatId = c.id;
                this.state.addMessage(sm);
                this.db.addMessage(sm);
            }
        }

        // Rejected
        else {

            // The chat is a group
            if (c.isGroup)
                c.nodes.remove(response.from);

            BrzNode n = BrzGraph.getInstance().getVertex(response.from);
            if (n != null) {
                BrzMessage sm = new BrzMessage(n.name + " rejected the chat.");
                sm.chatId = c.id;
                this.state.addMessage(sm);
                this.db.addMessage(sm);
            }
        }

        this.state.addChat(c);
        this.db.setChat(c);
    }

    public void incomingHandshake(BrzChatHandshake handshake) {
        BrzChat chat = handshake.chat;
        if (!chat.isGroup) {
            BrzNode n = BrzGraph.getInstance().getVertex(handshake.from);
            chat.name = n.name;
        }

        chat.acceptedByHost = false;
        chat.acceptedByRecipient = false;

        this.state.addChat(handshake.chat);
        this.db.setChat(handshake.chat);
    }

    public void acceptHandshake(BrzChatHandshake handshake) {
        BrzChatResponse response = new BrzChatResponse(this.hostNode.id, handshake.chat.id, true);

        // Send the response
        BrzPacket p = new BrzPacket(response, BrzPacket.BrzPacketType.CHAT_RESPONSE, handshake.from, false);
        this.router.send(p);

        encryption.saveSecretKey(handshake);

        // Set state to have the chat accepted
        BrzChat c = this.state.getChat(handshake.chat.id);
        c.acceptedByHost = true;
        c.acceptedByRecipient = true;
        this.state.addChat(c);
        this.db.setChat(c);
    }

    public void rejectHandshake(BrzChatHandshake handshake) {
        BrzChatResponse response = new BrzChatResponse(this.hostNode.id, handshake.chat.id, false);

        BrzPacket p = new BrzPacket(response, BrzPacket.BrzPacketType.CHAT_RESPONSE, handshake.from, false);
        this.router.send(p);

        this.state.removeChat(handshake.chat.id);
        this.db.deleteChat(handshake.chat.id);
    }

    //
    //
    // Messaging
    //
    //

    public void sendMessage(BrzMessage message, String chatId) {
        BrzPacket p = new BrzPacket(message, BrzPacket.BrzPacketType.MESSAGE, "", false);

        // Send message to each recipient
        BrzChat chat = this.state.getChat(chatId);
        for (String nodeId : chat.nodes) {
            if (nodeId.equals(hostNode.id))
                continue;
            p.to = nodeId;
            this.router.send(p);
        }

        this.addMessage(message);
    }

    public void addMessage(BrzMessage message) {
        this.state.addMessage(message);
        this.db.addMessage(message);
    }

}
