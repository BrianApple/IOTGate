package gate.util;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.EventLoopGroup;

public class CommonUtil {
	/**
	 * 计数
	 */
	public static AtomicInteger recieveCount ;
	
	static{
		recieveCount = new AtomicInteger(0);
	}
	
	/**
	 * 网关编号
	 */
	public static int gateNum ;
	/**
	 * 关闭EventLoopGroup
	 * @param group
	 */
	public static void closeEventLoop(EventLoopGroup...  group ){
		for (EventLoopGroup eventLoopGroup : group) {
			eventLoopGroup.shutdownGracefully();
		}
	}
	

}
