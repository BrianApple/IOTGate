package gate.client.handler;

import java.net.InetSocketAddress;

import gate.base.cache.ClientChannelCache;
import gate.base.chachequeue.CacheQueue;
import gate.base.domain.ChannelData;
import gate.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class Client2MasterInHandler extends SimpleChannelInboundHandler<ChannelData>{

	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ChannelData msg) throws Exception {
		String str=  StringUtils.encodeHex(msg.getSocketData().getLenArea())+StringUtils.encodeHex(msg.getSocketData().getContent());
		
		
		Channel channel = ClientChannelCache.get(msg.getIpAddress());
		if(channel != null){
			channel.writeAndFlush(msg);
			System.out.println("Gate Down = 68"+str+"16");
		}
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		/**
		 * 一旦网关与前置 建立连接 将  该连接通道channel缓存起来，方便Server选择发送上行报文的前置
		 */
		Channel channel = ctx.channel();
		InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
		String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
		String masterIP = ipAddress;
		CacheQueue.masterChannelCache.put(masterIP, ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		/**
		 * 当网关与前置断开连接 则从缓存中删除对应的channel 以便选择存活的channel发送报文到前置
		 */
		Channel channel = ctx.channel();
		InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
		String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
		String masterIP = ipAddress;
		CacheQueue.masterChannelCache.remove(masterIP);
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}

	

}
