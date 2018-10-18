package gate.threadWorkers;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import gate.base.chachequeue.CacheQueue;
import gate.base.domain.ChannelData;
import gate.util.CommonUtil;
import gate.util.StringUtils;
import io.netty.channel.Channel;

/**
 * 通过up2MasterQueue对列中获取从Server4Terminal发送过来的上行报文对象，
 * 并通过缓存的master的channel发送出去
 * @author BriansPC
 *
 */
public class TServer2MClient implements Runnable{

	private LinkedBlockingQueue<ChannelData> up2MasterQueue;
	
	
	
	public TServer2MClient(LinkedBlockingQueue<ChannelData> up2MasterQueue) {
		super();
		this.up2MasterQueue = up2MasterQueue;
	}



	public void run() {
		while(true){
			ChannelData channelData = null;
			try {
				channelData = up2MasterQueue.take();//获取从Server4Terminal发送过来的上行报文对象
				//获取前置与网关连接channel
				Channel masterChannel = CacheQueue.choiceMasterChannel();
				/**
				 * 1.通过channel发送数据--会执行编码器等handler
				 * 2.发送数据前判断masterChannel是否可写，因为配置了高水位和低水位，有可能channel为“不可写”状态
				 */
				if(masterChannel != null  &&  masterChannel.isWritable()){
					
					masterChannel.writeAndFlush(channelData);
					
				}else{
					System.out.println("masterChannel为空或者masterChannel不可写");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}
