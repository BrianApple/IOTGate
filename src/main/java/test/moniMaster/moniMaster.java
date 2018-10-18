package test.moniMaster;

import gate.codec.Gate2ClientDecoder;
import gate.codec.Gate2MasterDecoder;
import gate.codec.Gate2MasterEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 模拟 前置
 * @author BriansPC
 *
 */
public class moniMaster {
	public static void main(String[] args) {
		EventLoopGroup boss=new NioEventLoopGroup();
		EventLoopGroup work=new NioEventLoopGroup();
		//创建ServerBootstrap辅助类  客户端是Bootstrap辅助类 注意区分
		ServerBootstrap bootstrap=new ServerBootstrap();
		//通过辅助类配置通道参数
		bootstrap.group(boss,work);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		//关联通道的处理类
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				
				sc.pipeline().addLast(new moniMasterDecoder());
//				sc.pipeline().addLast(new Gate2MasterEncoder());
				sc.pipeline().addLast(new moniMasterHandler());
			}
		});
		ChannelFuture channelFuture;
		try {
			channelFuture = bootstrap.bind(8888).sync();
			System.out.println("模拟前置已启动！！port = " +8888);
			
			channelFuture.channel().closeFuture().sync();
			
			boss.shutdownGracefully();
			work.shutdownGracefully();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
	}
}
