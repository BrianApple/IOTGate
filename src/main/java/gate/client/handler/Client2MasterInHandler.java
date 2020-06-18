package gate.client.handler;

import java.net.InetSocketAddress;

import java.util.List;

import gate.base.chachequeue.CacheQueue;
import gate.base.domain.ChannelData;
import gate.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
@Sharable
public class Client2MasterInHandler extends SimpleChannelInboundHandler<Object>{

	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if(msg instanceof List){
			List<ChannelData> dataList = (List<ChannelData>) msg;
			for (ChannelData channelData : dataList) {
				CacheQueue.down2TmnlQueue.put(channelData);
			}
		}else{
			ChannelData channelData = (ChannelData)msg;
			CacheQueue.down2TmnlQueue.put(channelData);
		}
		
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		/**
		 * 缓存会话
		 */
		Channel channel = ctx.channel();
		InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
		String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
		String masterIP = ipAddress;
		CacheQueue.addMasterChannel2LocalCache(masterIP, ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		/**
		 * 移除会话
		 */
		Channel channel = ctx.channel();
		InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
		if(insocket!= null){
			String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
			String masterIP = ipAddress;
			CacheQueue.removeMasterChannelFromLocalCache(masterIP);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}

	

}
