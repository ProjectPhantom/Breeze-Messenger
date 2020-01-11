package com.breeze.state;

import android.util.Log;

import com.breeze.EventEmitter;
import com.breeze.datatypes.BrzNode;
import com.breeze.datatypes.BrzMessage;
import com.breeze.datatypes.BrzChat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class BrzStateStore extends EventEmitter {

    private static BrzStateStore instance = new BrzStateStore();

    public static BrzStateStore getStore() {
        return instance;
    }

    //
    // Title
    //

    private String title = "";

    public void setTitle(String title) {
        this.title = title;
        this.emit("title", this.title);
    }

    //
    // Viewing Chat ID
    //

    private String currentChat = "";
    public String getCurrentChat() {
        return this.currentChat;
    }
    public void setCurrentChat(String chatId) {
        this.currentChat = chatId;
        this.emit("currentChat", chatId);
    }

    //
    // Host node
    //

    private BrzNode hostNode = null;

    public BrzNode getHostNode() {
        return this.hostNode;
    }

    public void setHostNode(BrzNode hostNode) {
        this.hostNode = hostNode;
        this.emit("hostNode", this.hostNode);
    }

    //
    //  Chats
    //

    private HashMap<String, BrzChat> chats = new HashMap<>();

    public List<BrzChat> getAllChats() {
        return new ArrayList<>(this.chats.values());
    }

    public BrzChat getChat(String chatId) {
        return this.chats.get(chatId);
    }

    public void addChat(BrzChat chat) {
        this.chats.put(chat.id, chat);

        this.emit("allChats", this.getAllChats());
        this.emit("chat" + chat.id, chat);
    }

    public void addAllChats(List<BrzChat> chats) {
        for (BrzChat chat : chats) {
            this.chats.put(chat.id, chat);
            this.emit("chat" + chat.id, chat);
        }

        this.emit("allChats", this.getAllChats());
    }

    public void removeChat(String chatId) {
        this.chats.remove(chatId);

        this.emit("allChats", this.getAllChats());
        this.emit("chat" + chatId, null);
    }

    //
    //  Messages
    //

    private HashMap<String, List<BrzMessage>> messages = new HashMap<>();

    public List<BrzMessage> getMessages(String chatId) {
        return this.messages.get(chatId);
    }

    public void addMessage(BrzMessage msg) {
        List<BrzMessage> messages = this.messages.get(msg.chatId);
        if (messages == null) {
            messages = new ArrayList<>();
            this.messages.put(msg.chatId, messages);
        }
        messages.add(msg);

        this.emit("messages" + msg.chatId, this.getMessages(msg.chatId));
        this.emit("messages");
    }

    public void addAllMessages(List<BrzMessage> newMessages) {
        Set<String> chatIdsToUpdate = new HashSet<>();
        for (BrzMessage msg : newMessages) {
            List<BrzMessage> messages = this.messages.get(msg.chatId);
            if (messages == null) {
                messages = new ArrayList<>();
                this.messages.put(msg.chatId, messages);
            }
            messages.add(msg);
            chatIdsToUpdate.add(msg.chatId);
        }

        for (String chatId : chatIdsToUpdate)
            this.emit("messages" + chatId, this.getMessages(chatId));

        this.emit("messages");
    }


}
