package gate.client;

import java.net.Inet4Address;
import java.net.InetSocketAddress;

import gate.base.chachequeue.CacheQueue;
import gate.base.constant.ConstantValue;
import gate.base.domain.GateHeader;
import gate.base.domain.SocketData;
import gate.client.handler.Client2MasterInHandler;
import gate.codec.Gate2ClientDecoder;
import gate.codec.Gate2ClientEncoder;
import gate.codec.Gate2MasterDecoder;
import gate.codec.Gate2MasterEncoder;
import gate.util.CommonUtil;
import gate.util.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 网关与前置相连的客户端
 * 
 * 获取数据去要将A8网关头去掉
 * 
 * @author BriansPC
 */
public class Client2Master {
	private static EventLoopGroup worker = new NioEventLoopGroup();
	public static Bootstrap configClient(){
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(worker)
		.channel(NioSocketChannel.class)
		.option(ChannelOption.SO_KEEPALIVE, true)
		/**
		 * 设置ByteBuf的高低水位线，原方法WRITE_BUFFER_HIGH_WATER_MARK，WRITE_BUFFER_LOW_WATER_MARK已经被废弃
		 * 由WRITE_BUFFER_WATER_MARK代替
		 */
		.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024 * 1024, 64 * 1024 * 1024))//加上该配置后，网byteBuf写数据前需要判断iswriteble
		.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				//添加控制链
				ch.pipeline().addLast(new Gate2MasterDecoder());
				ch.pipeline().addLast(new Gate2MasterEncoder());//自定义编解码器
				ch.pipeline().addLast(new Client2MasterInHandler());
			}
			
		});
		return bootstrap;
	}
	/**
	 * 绑定客户端到指定的ip 和 端口port
	 * @param bootstrap
	 * @param ip
	 * @param port
	 * @throws Exception 
	 */
	public static void bindAddress2Client(Bootstrap bootstrap,String ip, int port) throws Exception{
		ChannelFuture channelFuture=bootstrap.connect(ip, port).sync();
		
		/**
		 * 链接成功之后 向前置发送网关头信息
		 */
		Channel channel  =  channelFuture.channel();
		//获取网关本地地址
		InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
		String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
		channelFuture.channel().writeAndFlush(loginGateHeader(ipAddress));
		
		channelFuture.channel().closeFuture().sync();
	}
	
	/**
	 * 组装网关登录报文
	 * @param channel
	 * @throws Exception 
	 */
	public static ByteBuf loginGateHeader(String LocalIpAddress) throws Exception{
		/**
		 * 创建直接内存形式的ByteBuf，不能使用array()方法，但效率高
		 */
		ByteBuf out = Unpooled.directBuffer();
//		PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
//		ByteBuf out = allocator.buffer();
		String ipAddress = LocalIpAddress;
		//连接序号默认1
		int count = 1;
		//登录报文的真实报文长度为0（真实报文是指 以68开始 16结尾的报文）
		int len = 0;
		

		GateHeader headBuf= new GateHeader(); 
		headBuf.writeInt8(Integer.valueOf(ConstantValue.GATE_HEAD_DATA).byteValue());
		headBuf.writeInt16(len);//整个长度
		headBuf.writeInt8(Integer.valueOf("03").byteValue());//type
		headBuf.writeInt8(Integer.valueOf("15").byteValue());//protocolType
		headBuf.writeInt8((byte) CommonUtil.gateNum);//网关编号
		for(int i = 0; i < 3; i++) {  //12个字节的00
			headBuf.writeInt32(0);
		}
		
		byte[] bs = Inet4Address.getByName(ipAddress.split("|")[0]).getAddress();//127.0.0.1 -->  [127, 0, 0, 1]
		headBuf.writeInt8(bs[0]);
		headBuf.writeInt8(bs[1]);
		headBuf.writeInt8(bs[2]);
		headBuf.writeInt8(bs[3]);
		headBuf.writeInt16(Integer.parseInt(ipAddress.split("|")[1]));//port  两个字节表示端口号
		headBuf.writeInt32(count);//count  4个字节的count
		out.writeBytes(headBuf.getDataBuffer());
		return out;
	}
	

}
