package gate.codec;

import java.util.List;

import gate.base.constant.ConstantValue;
import gate.base.domain.ChannelData;
import gate.base.domain.SocketData;
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
public class Gate2ClientDecoder  extends ByteToMessageDecoder{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
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
					if ( (header&0xFF) == ConstantValue.HEAD_DATA) {
						//当获取到帧头--0x68时
						
						//读取长度域等.......
						//读取长度域
						int contLength = readLenArea(in,lenArea);
						if( (contLength > 2 ) && (in.readableBytes() > contLength-2)){//首先 长度必须大于长度域本身占的2个字节
							
							in.markReaderIndex();
							//获取帧尾 判断是否为0X16
							in.skipBytes(contLength-2);
							if(isEnd(in)){
								//当报文是以0x16结尾的，读取报文体
								in.resetReaderIndex();
								content = readContent(in,contLength-2);
								
								String clientIpAddress = ctx.channel().remoteAddress().toString().replaceAll("\\/", "");
								SocketData data = new SocketData(header, lenArea, content, end);
								ChannelData channelData =  new ChannelData(clientIpAddress, data);
								out.add(channelData);
								
							}else{
								//当报文不是以0x16结尾则当前帧为错误帧 ，丢弃
								break;
							}
						}else{
							//不是一个完整帧 
							
							in.readerIndex(beginReader);
							break;
						}
						
						
					}else{
						//当没有获取到帧头--0x68时  继续下一次获取
						if (in.readableBytes() <= 3) {
							return;
						}
						continue ;
					}
	 

					

				
			}
		}
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
		byte[] bs = new byte[len];
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
