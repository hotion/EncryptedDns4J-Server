
package com.snail.dnslb4j.util;

import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

/**
 *
 * @author pengmeng
 */
public interface RequestTimeoutCallback {

	public void onTimeout(Channel ch, DatagramPacket requestPacket,Integer timeout);
}
