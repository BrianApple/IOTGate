package gate.codec;

import gate.rpc.dataBridge.ResponseData;
import gate.util.MixAll;
import gate.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月18日
 */
public class RpcEncoder extends MessageToByteEncoder<ResponseData> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ResponseData msg, ByteBuf out) throws Exception {
		byte[] data = SerializationUtil.serialize(msg);
		out.writeShort(data.length);
		out.writeBytes(data);
	}

}
