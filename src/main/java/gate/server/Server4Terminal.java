package gate.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import gate.base.cache.ClientChannelCache;
import gate.base.chachequeue.CacheQueue;
import gate.client.Client2Master;
import gate.codec.Gate2ClientDecoder;
import gate.codec.Gate2ClientEncoder;
import gate.server.handler.SocketInHandler;
import gate.threadWorkers.TServer2MClient;
import gate.util.CommonUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 
 * 网关获取终端报文
 * @author BriansPC
 *
 */
public class Server4Terminal {
	
	private static EventLoopGroup boss = new NioEventLoopGroup(1);
	private static  EventLoopGroup work = new NioEventLoopGroup();
	/**
	 * 通过引导配置参数
	 * @return
	 */
	public static ServerBootstrap config(){
		 ServerBootstrap serverBootstrap = new ServerBootstrap();
		 serverBootstrap
		 .group(boss, work)
		 .channel(NioServerSocketChannel.class)
		 .option(ChannelOption.SO_KEEPALIVE, true)
		 .childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				//心跳检测,超时时间300秒，指定时间中没有读写操作会触发IdleStateEvent事件
				ch.pipeline().addLast(new IdleStateHandler(0, 0, 300, TimeUnit.SECONDS));
				//自定义编解码器  需要在自定义的handler的前面即pipeline链的前端,不能放在自定义handler后面，否则不起作用
				ch.pipeline().addLast("decoder",new Gate2ClientDecoder());
				ch.pipeline().addLast("encoder",new Gate2ClientEncoder());
				ch.pipeline().addLast(new SocketInHandler());
			}
		});
		 
		return serverBootstrap;
		
		 
	}
	/**
	 * 绑定服务到指定端口
	 * @param serverBootstrap
	 */
	public static void bindAddress(ServerBootstrap serverBootstrap,int address){
		ChannelFuture channelFuture;
		try {
			channelFuture = serverBootstrap.bind(address).sync();
			System.out.println("网关服务端已启动！！");
			channelFuture.channel().closeFuture().sync();
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}finally{
			CommonUtil.closeEventLoop(boss,work);
		}
	}
	
	
	
	
	public static void main(String[] args) {
		//网关编号
		CommonUtil.gateNum = 1;
		//启动中间线程
		initEnvriment();
		//添加JVM钩子
		addHook();
		
		//启动与终端对接的服务端  因为是阻塞运行 需要开线程启动
		new Thread(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("启动与终端对接的服务端.............");
				bindAddress(config(),9811);
			}
		}).start();
		//启动与前置对接的客户端  因为是阻塞运行 需要开线程启动
		new Thread(new Runnable() {
			
			public void run() {
				try {
					System.out.println("启动与前置对接的客户端.............");
					Client2Master.bindAddress2Client(Client2Master.configClient(),"127.0.0.1",8888);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		
		

	}
	/**
	 * 环境初始化  ---目前最还先不用spring管理
	 */
	public static  void initEnvriment(){
		
		ExecutorService exService = Executors.newFixedThreadPool(1);
		
		//初始化  网关终端端 --->  网关前置端   搬运数据线程
		exService.execute(new TServer2MClient(CacheQueue.up2MasterQueue));
	}
	/**
	 * JVM的关闭钩子--JVM正常关闭才会执行
	 */
	public static void addHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			public void run() {
				//清空缓存信息
				System.out.println("网关正常关闭前执行  清空所有缓存信息...............................");
				ClientChannelCache.clearAll();
				CacheQueue.clearIpCountRelationCache();
				CacheQueue.clearMasterChannelCache();
			}
		}));
	}
}
