package com.breeze.packets.graph;

import android.util.Log;

import com.breeze.graph.BrzNode;
import com.breeze.packets.BrzSerializable;

import org.json.JSONObject;

public class BrzGraphEvent implements BrzSerializable {
    public enum BrzGEType { CONNECT, DISCONNECT }

    BrzGEType type = BrzGEType.CONNECT;
    BrzNode node1;
    BrzNode node2;

    public BrzGraphEvent() { }
    public BrzGraphEvent(String json) {
        this.fromJSON(json);
    }
    public BrzGraphEvent(Boolean connection, BrzNode node1, BrzNode node2) {
        this.type = connection ? BrzGEType.CONNECT : BrzGEType.DISCONNECT;
        this.node1 = node1;
        this.node2 = node2;
    }

    @Override
    public void fromJSON(String json) {
        try {
            JSONObject jObj = new JSONObject(json);

            JSONObject node1 = jObj.getJSONObject("node1");
            this.node1 = new BrzNode(node1.toString());

            JSONObject node2 = jObj.getJSONObject("node2");
            this.node2 = new BrzNode(node2.toString());

            this.type = BrzGEType.valueOf(jObj.getString("type"));
        } catch (Exception e) {
            Log.i("DESERIALIZATION ERROR", e.toString());
        }
    }

    @Override
    public String toJSON() {
        JSONObject jObj = new JSONObject();

        try {
            jObj.put("type", this.type);
            jObj.put("node1", new JSONObject(this.node1.toJSON()));
            jObj.put("node2", new JSONObject(this.node2.toJSON()));
        } catch (Exception e) {
            Log.i("DESERIALIZATION ERROR", e.toString());
        }

        return jObj.toString();
    }
}

