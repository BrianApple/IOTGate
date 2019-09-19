package gate.client;


import io.netty.channel.ChannelHandler;

/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年9月15日
 */
public interface IHolderHanders {

	ChannelHandler[] getChannelHandlers();
}
