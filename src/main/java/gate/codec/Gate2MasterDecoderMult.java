package gate.codec;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.List;

import gate.base.constant.ConstantValue;
import gate.base.domain.ChannelData;
import gate.base.domain.SocketData;
import gate.util.CommonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
/**
 * 解码器，，将字节形式的报文 解码成对象
 * 
 * 如果当前拦截器没有向out中写入数据  则 后续的handler也不会被执行了
 * @author BriansPC
 *
 */
public class Gate2MasterDecoderMult  extends ByteToMessageDecoder{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		//解码网关头 获取终端ip
//		ChannelData channelData = decodeGateHeader(in);
//		if(channelData != null){
//			out.add(channelData);
//			
//		}
		if(in.readableBytes()>31){
			//网关头固定为28位  加SocketData至少3位
			StringBuilder clientIpAddress ;
			int beginReader;
			
			while (true) {
				if(in.readableBytes()>31){
					beginReader = in.readerIndex();
					int gateHeader = in.readByte() & 0xFF;
					if(gateHeader == ConstantValue.GATE_HEAD_DATA){
						//1.获取到网关头A8
						int socketDataLen = in.readIntLE();// readLenArea(in);
						System.out.println(in.readableBytes());
						if(in.readableBytes() >= (socketDataLen+25) ){
							//报文完整
							in.skipBytes(1);
							boolean isIPV4 = true;
							int pId = -1;
							{
								int sig = in.readByte()&0xFF;
								pId = sig & 127 ;
								int type = sig >> 7 & 1;
								isIPV4 = type == 0	? true : false;	
								
							}
							clientIpAddress = new StringBuilder();
							if(isIPV4){
								in.skipBytes(13);
								clientIpAddress.append(in.readByte()&0xFF);  //ip地址需要转成10进制数
								clientIpAddress.append(".");
								clientIpAddress.append(in.readByte()&0xFF);
								clientIpAddress.append(".");
								clientIpAddress.append(in.readByte()&0xFF);
								clientIpAddress.append(".");
								clientIpAddress.append(in.readByte()&0xFF);
								
								
							}else{
								in.skipBytes(1);
								byte[] dataTemp = new byte[16];
								for(int i = 0 ; i < 16 ;i++){
									dataTemp[i] = in.readByte();
								}
								try {
									clientIpAddress.append(Inet6Address.getByAddress(dataTemp).getHostAddress());
								} catch (UnknownHostException e) {
									e.printStackTrace();
								}
							}
							clientIpAddress.append("|");
							clientIpAddress.append(in.readShortLE() & 0xFFFF);
							
							in.skipBytes(4);//连接次数
							SocketData data = new SocketData(in.readBytes(socketDataLen));
							data.setpId(pId);//规约类型
							ChannelData channelData =  new ChannelData(data);
							channelData.setIpAddress(clientIpAddress.toString());
							out.add(channelData);
							continue;
						}else{
							//报文不完整
							in.readerIndex(beginReader);
							break;
						}
					}else{
						if (in.readableBytes() <= 31) {
							
							break;
						}
						continue ;
					}
				}else{
					break;
				}
			}
		}
	}

}
