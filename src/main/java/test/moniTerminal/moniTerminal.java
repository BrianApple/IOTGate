package test.moniTerminal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gate.util.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import test.CountHelper;

/**
 * 客户端--未处理粘包，因此在大量模拟数据的时候，接收的响应数据会产生粘包问题
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
		
		
				ChannelFuture channelFuture=bootstrap.connect("127.0.0.1", port).sync();
				for(int i = 0; i<1 ; i++){
//					Thread.sleep(1000);
//					if(i % 2 == 0){
//						byte[] data = StringUtils.decodeHex("681E0081052360541304000024B801000100F007E2071A040F25090000120416");
//						
//						channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
//					}else{
						/**
						 * 规约类型1
						 * 样例补充：
						 *  报头  长度域							数据域							    报尾
						 * 	68    1E00    81052360541304000024B801000100F007E2071A040F250900001204   16
						 * 	当前规约对应的iotGate.conf文件的配置信息如下： 1,0,-1,1,2,1,1,9811,60
						 * 	从左自又，依次解释含义：
						 * 		1，规约唯一编号：1表示该协议编号为1
						 * 		2，长度域有多个字节时，是否以大端模式解析长度：0表示false
						 * 		3，该协议的所有报文，是否都是以固定数字开头：默认-1，企业版实现具体功能，社区版本置默认值-1即可
						 * 		4，该协议的所有报文，长度域与包头偏移量，1表示长度域与包头有一个字节
						 * 		5，该协议的所有报文，长度域站用报文的字节长度，2表示固定占用2字节，示例报文："1E00"
						 * 		6，该协议的所有报文，长度域解析出来的长度值，是否包含了长度域本身占用的字节数？1 true，示例报文：1E00 = 30 ；30-2 = 28，则长度域表示的数据值长度为28字节
						 * 		7，该协议的所有报文，除了长度域偏移量和长度域能表示的报文长度以外，是否还有剩余固定字节长度，长度值是多少？1表示剩余一个字节（示例报文中对应0x16）
						 * 		8，该协议通过网关分配的固定端口
						 * 		9，该协议默认的设备在线心跳周期
						 */

//						byte[] data = StringUtils.decodeHex("000100000006 01 0600340000".replaceAll(" ", ""));
						byte[] data = StringUtils.decodeHex("FFFF00000002 0001".replaceAll(" ", ""));


						/**
						 * 规约类型2
						 */
//						byte[] data = StringUtils.decodeHex("0000001906343030303132F200077076636C6F7564077076636C6F7564");
						/**
						 * 规约类型3
						 * 684A004A006800114155000000E3000001002342161526044516
						 */
//						byte[] data = StringUtils.decodeHex("40 40 00 00 02 03 37 1A 0C 1A 0A 12 4E 01 00 00 09 FF 00 00 00 00 00 00 0A 00 02 08 01 00 00 36 1A 0C 1A 0A 12 96 23 23".replaceAll(" ", ""));
						channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer(data));
						
						
//					}
					
				}
				channelFuture.channel().closeFuture().sync();
				work.shutdownGracefully();
	}
	private static int port = 9813;
	
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
