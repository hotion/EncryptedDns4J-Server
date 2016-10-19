package com.snail.dnslb4j;

import com.snail.dnslb4j.util.Cfg;
import com.snail.dnslb4j.util.Log;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public final class TcpServer {

	public static void main(String[] args) throws Exception {
		io.netty.util.internal.logging.InternalLoggerFactory.setDefaultFactory(new io.netty.util.internal.logging.JdkLoggerFactory());
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		String listenIp = Cfg.config("listen_ip");
		int listenPort = Cfg.configInt("listen_port");
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch)
						throws Exception {
						ch.pipeline().addLast(new ObjectDecoder(1024 * 1024, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
						ch.pipeline().addLast(new ObjectEncoder());
						ch.pipeline().addLast(new TcpServerHandler());
					}
				});
			ChannelFuture f = b.bind(listenIp,listenPort).sync();
			Log.logger().info("listen on "+listenIp+":"+listenPort);
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
