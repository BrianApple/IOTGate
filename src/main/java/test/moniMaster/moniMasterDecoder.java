package test.moniMaster;

import java.util.List;

import gate.base.cache.ClientChannelCache;
import gate.base.constant.ConstantValue;
import gate.base.domain.ChannelData;
import gate.base.domain.SocketData;
import io.netty.buffer.ByteBuf;
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
		
		//需要先去掉解报头  即A8开头的报头
		ByteBuf gateHeadData = Unpooled.directBuffer();
		boolean flag = decodeGateHeaderbyByteBuf(in,gateHeadData);
		if(flag){
			/**
			 * 是网关登录报文，模拟前置暂时没有处理，真实前置需要写缓存等.....
			 */
			System.out.println("网关登录成功");
		}else{
			//解码真实下行报文体（68...16）信息到ChannelData对象
			ByteBuf contentBuf = decodeSocketData(in);
			gateHeadData.writeBytes(contentBuf);
			out.add(gateHeadData);
		}
		
	}
	
	/**
	 * 解析网关报文头  返回终端ip（包含端口）
	 * @param in
	 * @return
	 */
	public String decodeGateHeader(ByteBuf in){
		if(in.readableBytes()>31){
			//网关头固定为28位  加SocketData至少3位
			StringBuilder clientIpAddress ;
			int beginReader;
			
			while (true) {
				beginReader = in.readerIndex();
				int gateHeader = in.readByte() & 0xFF;
				if(gateHeader == ConstantValue.GATE_HEAD_DATA){
					//获取到网关头A8
					int socketDataLen = readLenArea(in);
					if(in.readableBytes() >= (socketDataLen+25) ){
						//报文完整
						in.skipBytes(15);//直接将读指针跳到终端ip处
						clientIpAddress = new StringBuilder();
						clientIpAddress.append(in.readByte());
						clientIpAddress.append(".");
						clientIpAddress.append(in.readByte());
						clientIpAddress.append(".");
						clientIpAddress.append(in.readByte());
						clientIpAddress.append(".");
						clientIpAddress.append(in.readByte());
						clientIpAddress.append(":");
						clientIpAddress.append(readLenArea(in));
						
						return clientIpAddress.toString();
					}else{
						//报文不完整，复原读指针 结束本次读取
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
	 * 解析网关报文头 返回网关报头字节数据
	 * @param in 传输数据的buf
	 * @param butebuf 用于存放解析之后数据的buf
	 * @return true：是网关登录报文  false：不是网关登录报文
	 */
	public boolean decodeGateHeaderbyByteBuf(ByteBuf in,ByteBuf butebuf){
		if(in.readableBytes()>3){
			//网关头固定为28位  加SocketData至少3位
			StringBuilder clientIpAddress ;
			int beginReader;
			
			while (true) {
				beginReader = in.readerIndex();
				int gateHeader = in.readByte() & 0xFF;
				if(gateHeader == ConstantValue.GATE_HEAD_DATA){
					butebuf.writeByte(gateHeader);
					// 获取到网关头A8
					byte[] lenArea = new byte[2];
					butebuf.writeBytes(lenArea);
					int socketDataLen = readLenArea(in,lenArea);
					if(socketDataLen == 0){
						//网关登录报文判断 
						if(in.readableBytes() == 25 ){
							// 网关登录报文长度完整
							for(int i = 0; i < 25;i++){
								butebuf.writeByte(in.readByte() & 0xFF);
							}
							return true;
						}else{
							//报文不完整，复原读指针
							in.readerIndex(beginReader);
							break;
						}
					}else{
						//不是网关登录报文
						if(in.readableBytes() >= (socketDataLen+25) ){
							//报文完整
							for(int i = 0; i < 25;i++){
								butebuf.writeByte(in.readByte() & 0xFF);
							}
							return false;
						}else{
							//报文不完整，复原读指针
							in.readerIndex(beginReader);
							break;
						}
					}
					
				}else{
					if (in.readableBytes() <= 31) {
						//如果当前缓存中剩余未读字节数小于31则直接跳出循环
						return true;
					}
					continue ;
				}
			}
		}
		
		return true;
	}
	public ByteBuf decodeSocketData(ByteBuf in){
		if(in.readableBytes()>3){
			//报文头
			 byte header;
			//报文长度
			 byte[] lenArea = new  byte[2];
			//报文体
			 byte[] content;
			//结尾16
			 byte end = 0x16;
		
			int beginReader;
				
			while (true) {
				// 获取包头开始的index
				beginReader = in.readerIndex();
				// 标记包头开始的index
				in.markReaderIndex();
				
				// 读到了协议的开始标志，结束while循环
				header = in.readByte();
				if (header == ConstantValue.HEAD_DATA) {
					
					//读取长度域 ,标记读取之后的读取指针位置
					int contLength = readLenArea(in,lenArea);
					if( (contLength > 2 ) && (in.readableBytes() > contLength-2)){
						
						in.markReaderIndex();
						//跳过长度域指定长度  获取帧尾 判断是否为0X16
						in.skipBytes(contLength-2);//当前指针已经不包含长度域的2个字节了
						if(isEnd(in)){
							//当报文是以0x16结尾的，读取报文体
							in.resetReaderIndex();
							content = readContent(in,contLength-2);
							
							ByteBuf butebuf = Unpooled.directBuffer();
							
							butebuf.writeByte(header & 0xFF);
							butebuf.writeBytes(lenArea);
							butebuf.writeBytes(content);
							butebuf.writeByte(end);
							return butebuf;
							
						}else{
							// 当报文不是以0x16结尾则当前帧为错误帧 ，丢弃
							break;
						}
					}else{
						//不是一个完整帧 表示byteBuf中报文还没有获取完整 则将byteBuf的读指针还原   
						
						in.readerIndex(beginReader);
						break;
					}
				}else{
					if (in.readableBytes() <= 3) {
						return null;
					}
					continue ;
				}
	 
	
					
	
				
			}
		}
		return null;
	}
	/**
	 * ByteBuf获取读指针后两个字节的数据，并计算对应长度值并返回---小端模式
	 * @param in byteBuf
	 * @return
	 */
	public int readLenArea(ByteBuf in){
		
		ByteBuf buf = in.readBytes(2);//两个字节的长度域
//		lenArea = buf.array();//不能使用.array  因为默认是零拷贝
		byte left = buf.readByte();
		byte right = buf.readByte();
		int count = (left & 0xFF) + ((right & 0xFF) << 8 );
		return count;
	}
	/**
	 * 获取报文长度，并且获取报文长度大小的byte[]  正常获取返回长度int值
	 * @param in byteBuf
	 * @param lenArea 存储长度域2个字节的数据
	 * @return 
	 */
	public int readLenArea(ByteBuf in,byte[] lenArea){
		
		ByteBuf buf = in.readBytes(2);//两个字节的长度域
//		lenArea = buf.array();//不能使用.array  因为默认是零拷贝
		byte left = buf.readByte();
		byte right = buf.readByte();
		lenArea[0] = left;
		lenArea[1] = right;
		int count = (left & 0xFF) + ((right & 0xFF) << 8 );
		return count;
	}
	/**
	 * 获取报文的结束标识  正常获取返回结果
	 * @param in byteBuf
	 * @param len 读取区间
	 * @return
	 */
	public byte[] readContent(ByteBuf in,int len){
//		in.readBytes(len).array();
		byte[] bs = new byte[len];//in.readableBytes()-1 获取的长度是除了帧尾16的
		//因为还有帧尾
		if(in.readableBytes()>len){
			
			ByteBuf buf= in.readBytes(len);
			buf.getBytes(0, bs);
			return bs;
		}
		return null;
	}
	
	/**
	 * 获取报文的结束标识  正常获取返回true
	 * @param in byteBuf
	 * @return boolean
	 */
	public boolean isEnd(ByteBuf in){
		
		return in.readByte() == ConstantValue.END_DATA;
	}


}
