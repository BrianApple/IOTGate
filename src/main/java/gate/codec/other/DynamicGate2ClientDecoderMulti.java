package gate.codec.other;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import gate.base.domain.ChannelData;
import gate.base.domain.SocketData;
import gate.util.CommonUtil;
import gate.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
/**
 * 动态 多规约解码器---高级功能
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月26日
 */
public class DynamicGate2ClientDecoderMulti  extends ByteToMessageDecoder{
	private int pId;//规约类型--[0,127]共128种规约
	private boolean isBigEndian ;//大小端
	private int beginHexVal;//暂时传-1
	private int lengthFieldOffset;
	private boolean isDataLenthIncludeLenthFieldLenth ;//长度域长度值是否包含长度域本身长度
	private int exceptDataLenth;
	private int initialBytesToStrip;//默认为0
	private LengthParser lengthParser;
	
	
	/**
	 * 
	 * @param pId
	 * @param isBigEndian
	 * @param beginHexVal
	 * @param lengthFieldOffset
	 * @param isDataLenthIncludeLenthFieldLenth
	 * @param exceptDataLenth
	 * @param initialBytesToStrip
	 * @param lengthParser
	 */
	public DynamicGate2ClientDecoderMulti(int pId, boolean isBigEndian, int beginHexVal, int lengthFieldOffset,
			boolean isDataLenthIncludeLenthFieldLenth, int exceptDataLenth, int initialBytesToStrip,
			LengthParser lengthParser) {
		super();
		this.pId = pId;
		this.isBigEndian = isBigEndian;
		this.beginHexVal = -1;
		this.lengthFieldOffset = lengthFieldOffset;
		this.isDataLenthIncludeLenthFieldLenth = isDataLenthIncludeLenthFieldLenth;
		this.exceptDataLenth = exceptDataLenth;
		this.initialBytesToStrip = initialBytesToStrip;
		this.lengthParser = lengthParser;
	}



	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

		
		
		
		int baseLen = lengthFieldOffset  + exceptDataLenth + initialBytesToStrip;
		if(in.readableBytes()> baseLen){
			int beginReader;
			while (true) {
				
				
				ByteBuf byteBuf = CommonUtil.getDirectByteBuf();
				
				if(initialBytesToStrip == 0){
				}else{
					byteBuf.writeBytes(in.readBytes(initialBytesToStrip));
				}
				byteBuf.writeBytes(in.readBytes(lengthFieldOffset));
				int beReaderIndex = in.readerIndex();
				ArrayList<Integer> datas = new ArrayList<Integer>();
				lengthParser.parseLength(in,datas);
				in.readerIndex(beReaderIndex);//还原长度域
				int lenAreaLength = datas.get(0);
				int lenVal = datas.get(1);
				// 获取包头开始的index
				beginReader = in.readerIndex();
				// 标记包头开始的index
				in.markReaderIndex();
				
				
				//处理长度域
				byteBuf.writeBytes(in.readBytes(lenAreaLength));
				
				if(isDataLenthIncludeLenthFieldLenth){
					lenVal = lenVal - lenAreaLength;
				}
				
				if(in.readableBytes() >= (lenVal+exceptDataLenth)  && lenVal>0){
					byteBuf.writeBytes(in.readBytes(lenVal+exceptDataLenth));
					in.markReaderIndex();
					Channel channel = ctx.channel();
					InetSocketAddress insocket = (InetSocketAddress)channel.remoteAddress();
					String ipAddress = StringUtils.formatIpAddress(insocket.getHostName(), String.valueOf(insocket.getPort()));
					String clientIpAddress = ipAddress;
					SocketData data = new SocketData(byteBuf);
					data.setpId(pId);
					ChannelData channelData =  new ChannelData(clientIpAddress, data);
					out.add(channelData);
					break;
				}else{
					//还原
					in.readerIndex(beginReader+1);
					CommonUtil.releaseByteBuf(byteBuf);
					break;
				}
			}
		}
	}
}
