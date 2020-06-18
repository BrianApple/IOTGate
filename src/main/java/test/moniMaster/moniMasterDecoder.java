package test.moniMaster;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.List;

import gate.base.cache.ClientChannelCache;
import gate.base.constant.ConstantValue;
import gate.base.domain.ChannelData;
import gate.base.domain.SocketData;
import gate.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
/**
 * 解码器，模拟前置解码器
 * 
 * @author BriansPC
 *
 */
public class moniMasterDecoder  extends ByteToMessageDecoder{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		//解码网关头 获取终端ip
		ChannelData channelData = decodeGateHeader(in);
		if(channelData != null){
			out.add(channelData);
			
		}
		
		

	}
	public ChannelData decodeGateHeader(ByteBuf in){
		if(in.readableBytes()>31){
			StringBuilder clientIpAddress ;
			int beginReader;
			
			while (true) {
				beginReader = in.readerIndex();
				int gateHeader = in.readByte() & 0xFF;
				if(gateHeader == ConstantValue.GATE_HEAD_DATA){
					int socketDataLen =  readLenArea(in);//in.readShortLE();//
					if(in.readableBytes() >= (socketDataLen+25) ){
						in.readerIndex(beginReader);
						SocketData data = new SocketData(in.readBytes(socketDataLen+28));
						ChannelData channelData =  new ChannelData(data);
						
						
						return channelData;
					}else{
						in.readerIndex(beginReader);
						break;
					}
				}else{
					if (in.readableBytes() <= 31) {
						
						return null;
					}
					continue ;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @param in byteBuf
	 * @return
	 */
	public int readLenArea(ByteBuf in){
		
		ByteBuf buf = in.readBytes(2);//两个字节的长度域
		byte left = buf.readByte();
		byte right = buf.readByte();
		int count = (left & 0xFF) + ((right & 0xFF) << 8 );
		return count;
	}

}
