package test.moniTerminal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gate.util.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import test.CountHelper;

/**
 * 客户端
 * @author yangcheng
 * @date 2017年12月29日 
 * @version V1.0
 */
public class moniTerminal {
	static EventLoopGroup work=new NioEventLoopGroup();
	public static Bootstrap config(){
		//客户这边只需要创建一个线程组
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
				
				sc.pipeline().addLast(new moniTerminalHandler());
			}
		});
		return bootstrap;
	}
	public static void startClient(Bootstrap bootstrap) throws InterruptedException{
		//服务端是绑定到服务器某个端口就行，但是客户端是需要连接到指定ip+指定端口的 因此方法不一样
				ChannelFuture channelFuture=bootstrap.connect("127.0.0.1", 9811).sync();
				for(int i = 0; i<1 ; i++){
					if(i == 2){
						byte[] data = StringUtils.decodeHex("681E0081052360541304000024B801000100F007E2071A040F25090000120416");
						
						channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
						
//						Thread.sleep(2000);

					}else{
						byte[] data = StringUtils.decodeHex("681E0081052360541304000024B801000100F007E2071A040F25090000120416");
						
						channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
						
//						Thread.sleep(2000);
					}
					
				}
				channelFuture.channel().closeFuture().sync();
				work.shutdownGracefully();
	}
	
	public static void main(String[] args) throws InterruptedException {
		/**
		 * 模拟终端启动
		 */
		final Bootstrap bootstrap =config();
		ExecutorService serExecutorService = Executors.newFixedThreadPool(CountHelper.ThreadNum);
		for(int i=0 ; i<CountHelper.ThreadNum ;i++){
			
			serExecutorService.execute(new Runnable() {
				public void run() {
					try {
						startClient(bootstrap);//阻塞运行 需要开线程启动
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
