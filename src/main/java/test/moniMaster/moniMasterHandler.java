package test.moniMaster;


import java.util.concurrent.TimeUnit;

import gate.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;
import test.CountHelper;


public class moniMasterHandler extends ChannelInboundHandlerAdapter {
	/**
	 * 当通道刚刚建立时会调用该方法
	 */
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("通道初始化完成。。。。。。。。。");

	}
	/**
	 * 当通道中有数据传递到服务端时  该方法会执行
	 * 第二个参数就是通道传递过来的数据
	 */
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try{
			ByteBuf recieveMsg=(ByteBuf) msg;
			String code = ByteBufUtil.hexDump(recieveMsg).toUpperCase();//将bytebuf中的可读字节 转换成16进制数字符串
			int pos = CountHelper.masterRecieveCount.addAndGet(1);
			if(pos == 1 ){
				//初始化开始时间
				CountHelper.masterRecieveStartTime=System.currentTimeMillis();
			}

				
			System.out.println("接收总数："+pos+"模拟前置收到数据："+code);
			
			if(pos == CountHelper.ThreadNum ){
				//计算结束时间
				
				System.out.println("接收数据总花费时间为:"+(System.currentTimeMillis()-CountHelper.masterRecieveStartTime)+"毫秒");
				
			}
			if(pos == (CountHelper.ThreadNum+1) ){
				//第二次运行起始时间
				CountHelper.masterRecieveStartTime=System.currentTimeMillis();
			}
			if(pos == CountHelper.ThreadNum*2 ){
				//计算结束时间
				System.out.println("接收数据总花费时间为:"+(System.currentTimeMillis()-CountHelper.masterRecieveStartTime)+"毫秒");
			}
			/**
			 * 通过channel写出数据  会直接执行handler链中最后一个handler开始逆向执行
			 */
			Channel  channel = ctx.channel();
			/**
			 * 心跳确认
			 */
			String msgs = "683000010523605413040000A4D781008007E20802050F250D000D07E20802050F250D000D07E20802050F250D000D56F116";
			if(!msgs.startsWith("A8")){
				//登录报文不响应
				msgs = code.substring(0,56)+msgs;//网关头拼接
				channel.writeAndFlush(Unpooled.copiedBuffer(StringUtils.decodeHex(msgs)));
			}
			
		}finally{
			ReferenceCountUtil.release(msg);  //手动释放消息  -如果继承SimpleChannelInboundHandler则不需要  6.4.3
		}
		
		
	}
	/**
	 * 当获取通道中数据的时候，
	 * 
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
		System.err.println("出现异常。。。。。。。。。。"+cause.getMessage());
		ctx.close();
	}
	
}
