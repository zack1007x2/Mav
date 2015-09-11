package com.paku.mavlinkhub.queue.items;

import java.io.Serializable;

import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPacket;
import com.paku.mavlinkhub.enums.MSG_SOURCE;

public class ItemMavLinkMsg implements Serializable {

	/**
	 * serializable MavlinkMsg extended with packet data and the in-stream repetition count
	 */
	private static final long serialVersionUID = -2616788128278070587L;

	public final int count; // how many times the the same msg was repeated
	public int seqNo;
	public MAVLinkMessage msg;
	public long timestamp;
	public MSG_SOURCE direction;
	public int msgId;

	public int[] ch = new int[10];

	public ItemMavLinkMsg(MAVLinkPacket pkt, MSG_SOURCE direction, int count) {
		super();
		this.count = count;
		msg = pkt.unpack();
		seqNo = pkt.seq;
		msg.sysid = pkt.sysid;
		timestamp = System.currentTimeMillis();
		msgId = pkt.msgid;
		this.direction = direction;
	}

	public String countToString() {
		return String.valueOf(count);
	}

	@Override
	public String toString() {
		return "msgId = " + msgId + " |  SysId:" + msg.sysid + " |  SeqNo:" + seqNo + "|  msg =  " + msg.toString();
	}

	public void setChannel() {
		int counter = 0;
		String[] result = msg.toString().split(":");
		for (int i = 0; i < result.length; i++) {
			if (i > 0 && i < 11) {
				String[] aa = result[i].split(" ");
				Log.d("Zack", "SUB = " + aa[0]);
				ch[counter] = Integer.valueOf(aa[0]);
				counter++;
			}
		}
	}

	public String humanDecode() {
		return toString();
	}

	public byte[] getPacketBytes() {
		final MAVLinkPacket pkt = msg.pack();
		pkt.seq = seqNo;
		pkt.sysid = msg.sysid;
		return pkt.encodePacket();
	}
}
