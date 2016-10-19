/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snail.dnslb4j.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.IDN;
import java.net.InetSocketAddress;

/**
 *
 * @author pengmeng
 */
public class DnsMessage {

	public static void request(ByteBuf packet, String hostname, int port, int timeout, RequestSuccessCallback succcessCallback, RequestTimeoutCallback timeoutCallback) {
		DatagramPacket newPacket = new DatagramPacket(packet, new InetSocketAddress(hostname, port));
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		try {
			bootstrap.group(group)
				.channel(NioDatagramChannel.class)
				.handler(new SimpleChannelInboundHandler<DatagramPacket>() {
					@Override
					protected void messageReceived(ChannelHandlerContext ctx1, DatagramPacket packet1) throws Exception {
						Log.logger().debug("revceived from endpoint <-" + packet1.sender().getAddress().getHostAddress() + ":" + packet1.sender().getPort());
						if (succcessCallback != null) {
							succcessCallback.onMessage(ctx1, packet1, newPacket);
						}
						ctx1.channel().close();
						ctx1.close();
					}
				});
			Channel ch = bootstrap.bind(0).sync().channel();
			Log.logger().debug("request to endpoint ->" + hostname + ":" + port + ".");
			ch.writeAndFlush(newPacket).sync();
			if (!ch.closeFuture().await(timeout)) {
				if (timeoutCallback != null) {
					timeoutCallback.onTimeout(ch, newPacket, timeout);
				}
				Log.logger().debug("request timed out endpoint (" + timeout + "ms)->" + hostname + ":" + port + ".");
			}
		} catch (InterruptedException ex) {
			Log.logger().error("DnsMessage.request", ex);
		} finally {
			group.shutdownGracefully();
		}
	}

	public static ByteBuf buildQuery(String domain, int id) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		DataOutputStream dos = new DataOutputStream(baos);
		BitSet bits = new BitSet();
		bits.set(8);
		try {
			dos.writeShort((short) id);
			dos.writeShort((short) bits.value());
			dos.writeShort(1);
			dos.writeShort(0);
			dos.writeShort(0);
			dos.writeShort(0);
			dos.flush();
			writeQuestion(baos, domain);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		return Unpooled.copiedBuffer(baos.toByteArray());
	}

	private static void writeDomain(OutputStream out, String domain) throws IOException {
		for (String s : domain.split("[.\u3002\uFF0E\uFF61]")) {
			byte[] buffer = IDN.toASCII(s).getBytes();
			out.write(buffer.length);
			out.write(buffer, 0, buffer.length); // ?
		}
		out.write(0);
	}

	private static void writeQuestion(OutputStream out, String domain) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);
		writeDomain(out, domain);
		dos.writeShort(1);
		dos.writeShort(1);
	}
}
