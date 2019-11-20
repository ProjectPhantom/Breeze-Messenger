package com.breeze.packets;

import com.breeze.datatypes.BrzMessage;
import com.breeze.graph.BrzGraph;
import com.breeze.datatypes.BrzNode;
import com.breeze.packets.graph.BrzGraphEvent;
import com.breeze.packets.graph.BrzGraphQuery;

public class BrzPacketBuilder {

    public static BrzPacket message(String id, String msgBody) {
        return BrzPacketBuilder.message(id, msgBody);
    }

    public static BrzPacket ack(BrzPacket packet, String to) {
        BrzPacket ackPacket = new BrzPacket();

        ackPacket.type = BrzPacket.BrzPacketType.ACK;
        ackPacket.to = to;
        ackPacket.id = packet.id;

        return ackPacket;
    }

    public static BrzPacket message(String id, String msgTo, String msgBody, String chatId, Boolean isStatus) {
        BrzMessage body = new BrzMessage();

        body.from = id;
        body.body = msgBody;
        body.chatId = chatId;

        body.isStatus = isStatus;
        body.datestamp = System.currentTimeMillis();

        BrzPacket packet = new BrzPacket(body);
        packet.to = msgTo;

        return packet;
    }
    public static BrzMessage makeMessage(String fromId, String msgBody, String chatId, Boolean isStatus) {
        BrzMessage body = new BrzMessage();

        body.from = fromId;
        body.body = msgBody;
        body.chatId = chatId;

        body.isStatus = isStatus;
        body.datestamp = System.currentTimeMillis();

        return body;
    }

    public static BrzPacket graphQuery(String to, String id) {
        BrzGraphQuery body = new BrzGraphQuery(true, id);
        BrzPacket packet = new BrzPacket(body);
        packet.to = to;
        packet.type = BrzPacket.BrzPacketType.GRAPH_QUERY;
        return packet;
    }

    public static BrzPacket graphResponse(BrzGraph graph, BrzNode hostNode, String to) {
        BrzGraphQuery body = new BrzGraphQuery(false, "", graph.toJSON(), hostNode.toJSON());
        BrzPacket packet = new BrzPacket(body);
        packet.to = to;
        packet.type = BrzPacket.BrzPacketType.GRAPH_QUERY;
        return packet;
    }

    public static BrzPacket graphEvent(Boolean connection, BrzNode node1, BrzNode node2) {
        BrzGraphEvent body = new BrzGraphEvent(connection, node1, node2);
        BrzPacket packet = new BrzPacket(body);

        packet.type = BrzPacket.BrzPacketType.GRAPH_EVENT;
        packet.to = "BROADCAST";

        return packet;
    }
}
