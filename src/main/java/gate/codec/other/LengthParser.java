package gate.codec.other;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月26日
 */
public interface LengthParser {
	/**
	 * 
	 * @param byteBuf 数据来源
	 * @param rets 数据返回封装
	 */
	void parseLength(ByteBuf byteBuf,ArrayList<Integer> rets);

}
