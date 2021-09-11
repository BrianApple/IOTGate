package test.moniMaster;

import gate.util.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import test.moniTerminal.moniTerminalHandler;

/**
 * 模拟kernel模式下的前置服务
 */
public class moniMaster4Kernel {
	static EventLoopGroup work=new NioEventLoopGroup();
	public static Bootstrap config(){
		Bootstrap bootstrap=new Bootstrap();
		bootstrap.group(work)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.SO_SNDBUF, 32*1024)
				.option(ChannelOption.SO_RCVBUF, 32*1024)

				//这里方法名与服务端不一样，其他一致
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel sc) throws Exception {
						sc.pipeline().addLast(new moniMasterDecoder());
						sc.pipeline().addLast(new moniMasterHandler());
					}
				});
		return bootstrap;
	}

	public static void main(String[] args) throws InterruptedException {
		ChannelFuture channelFuture=config().connect("127.0.0.1", 10915).sync();
		channelFuture.channel().closeFuture().sync();
		work.shutdownGracefully();
	}
}