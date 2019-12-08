package gate.codec;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import gate.base.constant.ConstantValue;
import gate.base.domain.ChannelData;
import gate.base.domain.SocketData;
import gate.util.CommonUtil;
import gate.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.RecyclableArrayList;
/**
 * 长度域定长多规约解码器
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月20日
 */
public class Gate2ClientDecoderMulti  extends ByteToMessageDecoder{
	private int pId;//规约类型--[0,127]共128种规约
	private boolean isBigEndian ;//大小端
	private int beginHexVal;//暂时传-1
	private int lengthFieldOffset;
	private int lengthFieldLength;//值为Data得长度
	private boolean isDataLenthIncludeLenthFieldLenth ;//长度域长度值是否包含长度域本身长度
	private int exceptDataLenth;
	private int initialBytesToStrip;//默认为0
	
	/**
	 * 全参构造
	 * @param pId
	 * @param isBigEndian
	 * @param beginHexVal
	 * @param lengthFieldOffset
	 * @param lengthFieldLength
	 * @param isDataLenthIncludeLenthFieldLenth
	 * @param exceptDataLenth
	 * @param initialBytesToStrip
	 */
	public Gate2ClientDecoderMulti( int pId, boolean isBigEndian, int beginHexVal, int lengthFieldOffset, int lengthFieldLength,
			boolean isDataLenthIncludeLenthFieldLenth, int exceptDataLenth, int initialBytesToStrip) {
		super();
		this.pId = pId;
		this.isBigEndian = isBigEndian;
		this.beginHexVal = beginHexVal;
		this.lengthFieldOffset = lengthFieldOffset;
		this.lengthFieldLength = lengthFieldLength;
		this.isDataLenthIncludeLenthFieldLenth = isDataLenthIncludeLenthFieldLenth;
		this.exceptDataLenth = exceptDataLenth;
		this.initialBytesToStrip = initialBytesToStrip;
	}
	
	/**
	 * 默认起始偏移量为0
	 * @param pId
	 * @param isBigEndian
	 * @param beginHexVal
	 * @param lengthFieldOffset
	 * @param lengthFieldLength
	 * @param isDataLenthIncludeLenthFieldLenth
	 * @param exceptDataLenth
	 */
	public Gate2ClientDecoderMulti(int pId, boolean isBigEndian, int beginHexVal, int lengthFieldOffset, int lengthFieldLength,
			boolean isDataLenthIncludeLenthFieldLenth, int exceptDataLenth) {
		this(pId, isBigEndian, beginHexVal, lengthFieldOffset, lengthFieldLength,
				isDataLenthIncludeLenthFieldLenth, exceptDataLenth, 0);
	}




	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int baseLen = lengthFieldOffset + lengthFieldLength + exceptDataLenth + initialBytesToStrip;
		if(in.readableBytes()>= baseLen){
//			if (beginHexVal >=0){
//				
//			}
			int beginReader;
			RecyclableArrayList arrayList = RecyclableArrayList.newInstance();
			while (true) {
				if(in.readableBytes()>= baseLen){
					// 获取包头开始的index
					beginReader = in.readerIndex();
					// 标记包头开始的index
	//				in.markReaderIndex();
					
					ByteBuf byteBuf = CommonUtil.getByteBuf();
					
					if(initialBytesToStrip == 0){
						for(int i = 0 ; i < lengthFieldOffset ; i++){
							byteBuf.writeByte(in.readByte());
						}
						
						//处理长度域
						ByteBuf lenAre = in.readBytes(lengthFieldLength);
						int lenVal = 0;//data域长度
						switch (lengthFieldLength) {
						case 1:
								lenVal = lenAre.readByte() & 0xFF;
							break;
						case 2:
							if(isBigEndian){
								lenVal = lenAre.readShort();
							}else{
								lenVal = lenAre.readShortLE();
							}
							break;
						case 4:
							if(isBigEndian){
								lenVal = lenAre.readInt();
							}else{
								lenVal = lenAre.readIntLE();
							}
							break;
						default:
							CommonUtil.releaseByteBuf(byteBuf);
							break;
						}
						lenAre.readerIndex(0);
						byteBuf.writeBytes(lenAre);
						if(isDataLenthIncludeLenthFieldLenth){
							lenVal = lenVal - lengthFieldLength;
						}
						
						if(in.readableBytes() >= (lenVal+exceptDataLenth)  && lenVal>0){
							for(int i = 0 ; i < (lenVal+exceptDataLenth) ; i++ ){
								byteBuf.writeByte(in.readByte());
							}
//							in.markReaderIndex();
							Channel channel = ctx.channel();
							InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
							String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
							String clientIpAddress = ipAddress;
							SocketData data = new SocketData(byteBuf);
							data.setpId(pId);
							ChannelData channelData =  new ChannelData(clientIpAddress, data);
							arrayList.add(channelData);
							continue;
						}else{
							//还原
							in.readerIndex(beginReader+1);
							CommonUtil.releaseByteBuf(byteBuf);
							break;
						}
						
					}else{
						for(int i = 0 ; i < (initialBytesToStrip) ; i++ ){
							byteBuf.writeByte(in.readByte());
						}
						for(int i = 0 ; i < (lengthFieldOffset) ; i++ ){
							byteBuf.writeByte(in.readByte());
						}
						//处理长度域
						ByteBuf lenAre = in.readBytes(lengthFieldLength);
						
						int lenVal = 0;//data域长度
						switch (lengthFieldLength) {
						case 1:
								lenVal = lenAre.readByte() & 0xFF;
							break;
						case 2:
							if(isBigEndian){
								lenVal = lenAre.readShort();
							}else{
								lenVal = lenAre.readShortLE();
							}
							break;
						case 4:
							if(isBigEndian){
								lenVal = lenAre.readInt();
							}else{
								lenVal = lenAre.readIntLE();
							}
							break;
						default:
							CommonUtil.releaseByteBuf(byteBuf);
							break;
						}
						lenAre.readerIndex(0);
						byteBuf.writeBytes(lenAre);
						if(isDataLenthIncludeLenthFieldLenth){
							lenVal = lenVal - lengthFieldLength;
						}
						
						if(in.readableBytes() >= (lenVal+exceptDataLenth) && lenVal>0){
							for(int i = 0 ; i < (lenVal+exceptDataLenth) ; i++ ){
								byteBuf.writeByte(in.readByte());
							}
							Channel channel = ctx.channel();
							InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
							String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
							String clientIpAddress = ipAddress;
							SocketData data = new SocketData(byteBuf);
							data.setpId(pId);
							ChannelData channelData =  new ChannelData(clientIpAddress, data);
							arrayList.add(channelData);
							continue;
						}else{
							//还原
							in.readerIndex(beginReader+1);
							CommonUtil.releaseByteBuf(byteBuf);
							break;
						}
					}
				}else{
					int size = arrayList.size();
					if(size == 1){
						out.add(arrayList.get(0));
					}else if(size > 1){
						ArrayList<Object> arrayList2 = new ArrayList<>(size);
						for (int i = 0; i < size; i++) {
							arrayList2.add(arrayList.get(i));
						}
						out.add(arrayList2);
					}
					arrayList.recycle();
					break;
				}
			}
		}
	}
}
