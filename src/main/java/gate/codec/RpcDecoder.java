package gate.codec;


import java.nio.ByteBuffer;

import gate.rpc.dataBridge.RequestData;
import gate.rpc.rpcProcessor.RPCProcessor;
import gate.rpc.rpcProcessor.RPCProcessorImpl;
import gate.util.MixAll;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月18日
 */
public class RpcDecoder extends LengthFieldBasedFrameDecoder{

	RPCProcessor processor = new RPCProcessorImpl();
	
	public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
			int initialBytesToStrip) {
		super(10240, 0, 2, 0, 0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf buff =  (ByteBuf) super.decode(ctx, in);
		if(buff == null){
			return null;
		}
		ByteBuffer byteBuffer = buff.nioBuffer();
		int dataAllLen = byteBuffer.limit();
		int lenArea = byteBuffer.getShort();
		int dataLen = dataAllLen - lenArea;
		byte[] contentData = new byte[dataLen];
        byteBuffer.get(contentData);//报头数据
        RequestData requestData = MixAll.decode(contentData, RequestData.class);
        return processor.executeService(requestData);
	}

	
}
