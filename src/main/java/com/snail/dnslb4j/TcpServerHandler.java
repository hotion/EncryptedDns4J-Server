package com.snail.dnslb4j;

import com.snail.dnslb4j.util.DnsPacket;
import com.snail.dnslb4j.util.Log;
import com.snail.dnslb4j.util.Misc;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;
import java.net.InetSocketAddress;
import jodd.exception.ExceptionUtil;

public class TcpServerHandler extends SimpleChannelInboundHandler<Object> {

	// 接收到新的数据  
	@Override
	protected void messageReceived(ChannelHandlerContext ctx0, Object msg) throws Exception {
		DnsPacket dnsQueryPacket = (DnsPacket) msg;
		String dnsIp = dnsQueryPacket.getQueryInfo().getString("ip");
		int dnsPort = dnsQueryPacket.getQueryInfo().getIntValue("port");
		int dnsTimeout = dnsQueryPacket.getQueryInfo().getIntValue("timeout");
		String destDns = dnsIp + ":" + String.valueOf(dnsPort);
		DatagramPacket newPacket = new DatagramPacket(dnsQueryPacket.getQueryPacket(), new InetSocketAddress(dnsIp, dnsPort));
		Log.logger().info("revceived from <-" + ctx0.channel().remoteAddress());
		try {
			EventLoopGroup group = new NioEventLoopGroup();
			try {
				Bootstrap b = new Bootstrap();
				b.group(group)
					.channel(NioDatagramChannel.class)
					.handler(new SimpleChannelInboundHandler<DatagramPacket>() {
						@Override
						protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
							Log.logger().debug("revceived from <- " + packet.sender().getAddress().getHostAddress() + ":" + packet.sender().getPort());
							DnsPacket responseDnsPacket = new DnsPacket();
							responseDnsPacket.setReplyPacket(Misc.byteBuf2bytes(packet.content().copy()));
							Log.logger().info("reply to -> " + ctx0.channel().remoteAddress());
							ctx0.writeAndFlush(responseDnsPacket);
							ctx.channel().close();
						}
					});

				Channel ch = b.bind(0).sync().channel();
				Log.logger().debug("request to " + destDns);
				ch.writeAndFlush(newPacket).sync();
				if (!ch.closeFuture().await(dnsTimeout)) {
					Log.logger().warn("request to " + destDns + " timeout");
				}
			} finally {
				group.shutdownGracefully();
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		Log.logger().error(ExceptionUtil.exceptionChainToString(cause));
		ctx.close();
	}

}
