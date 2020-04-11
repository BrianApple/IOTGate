package gate.server;

import java.util.concurrent.ThreadFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import gate.base.cache.ProtocalStrategyCache;
import gate.codec.Gate2ClientDecoderMulti;
import gate.codec.Gate2ClientEncoderMulti;
import gate.server.handler.SocketInHandler;
import gate.util.CommonUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

/**
 * 网关获取终端报文
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class Server4Terminal {
	/**
	 * 规约编号作为规约服务以及规约策略的唯一标识
	 */
	private  String  pId;
	private  String  serverPort;
	private  EventLoopGroup  boss;
	private  EventLoopGroup work;
	private DefaultEventExecutorGroup defaultEventExecutorGroup;
	
	public Server4Terminal (String pId,String serverPort){
		this.pId = pId;
		this.serverPort = serverPort;
		this.boss = new NioEventLoopGroup(1);
		this.work = new NioEventLoopGroup();
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
	            Runtime.getRuntime().availableProcessors()*2 ,new ThreadFactory() {

	                private AtomicInteger threadIndex = new AtomicInteger(0);


	                @Override
	                public Thread newThread(Runnable r) {
	                    return new Thread(r, "NettyServerWorkerThread_" + this.threadIndex.incrementAndGet());
	                }
	            });
	}
	
	
	/**
	 * 通过引导配置参数--长度域固定
	 * @return
	 */
	public  ServerBootstrap config(int pId, boolean isBigEndian, int beginHexVal, int lengthFieldOffset, int lengthFieldLength,
			boolean isDataLenthIncludeLenthFieldLenth, int exceptDataLenth ,int heartbeat){
		 ServerBootstrap serverBootstrap = new ServerBootstrap();
		 serverBootstrap
		 .group(boss, work)
		 .channel(NioServerSocketChannel.class)
		 .option(ChannelOption.SO_KEEPALIVE, true)
		 .option(ChannelOption.TCP_NODELAY, true)
		 .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
         .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
		 .childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				//心跳检测,超时时间300秒，指定时间中没有读写操作会触发IdleStateEvent事件
				ch.pipeline().addLast(/*defaultEventExecutorGroup,*/new IdleStateHandler(0, 0, heartbeat, TimeUnit.SECONDS));
				//自定义编解码器  需要在自定义的handler的前面即pipeline链的前端,不能放在自定义handler后面，否则不起作用
				ch.pipeline().addLast("decoder",new Gate2ClientDecoderMulti(pId, isBigEndian, beginHexVal,
						lengthFieldOffset, lengthFieldLength, isDataLenthIncludeLenthFieldLenth, exceptDataLenth));//698长度域表示不包含起始符和结束符长度:1, false, -1, 1, 2, true, 1
				ch.pipeline().addLast("encoder",new Gate2ClientEncoderMulti());
				ch.pipeline().addLast(new SocketInHandler());
			}
		});
		 
		return serverBootstrap;
	}

	
	
	/**
	 * 绑定服务到指定端口
	 * @param serverBootstrap
	 */
	public  void bindAddress(ServerBootstrap serverBootstrap){
		ChannelFuture channelFuture;
		try {
			ProtocalStrategyCache.protocalServerCache.put(pId, this);
			channelFuture = serverBootstrap.bind(Integer.parseInt(serverPort)).sync();
			System.out.println("网关服务端已启动！！");
			channelFuture.channel().closeFuture().sync();
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}finally{
			CommonUtil.closeEventLoop(boss,work);
		}
	}
	/**
	 * 关闭服务
	 */
	public void close(){
		CommonUtil.closeEventLoop(boss,work);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//删除缓存种对应网关规约服务
		ProtocalStrategyCache.protocalServerCache.remove(pId);
	}
	
	
}
