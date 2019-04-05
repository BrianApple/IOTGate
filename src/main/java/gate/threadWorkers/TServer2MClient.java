package gate.threadWorkers;

import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gate.base.chachequeue.CacheQueue;
import gate.base.domain.ChannelData;
import gate.concurrent.ThreadFactoryImpl;
import io.netty.channel.Channel;

/**
 * 上行报文中转
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class TServer2MClient implements DataTransfer{

	private BlockingQueue<ChannelData> up2MasterQueue;
	private final int poolSize;
	ExecutorService exService;
	public TServer2MClient(BlockingQueue<ChannelData> up2MasterQueue ,int poolSize) {
		super();
		this.up2MasterQueue = up2MasterQueue;
		this.poolSize = poolSize;
		exService = Executors.newFixedThreadPool(poolSize,new ThreadFactoryImpl("msgTransWorker_UP_", false));
	}



	public void run() {
		
			
			for (int i=0 ; i < poolSize ; i++ ){
				exService.execute(new Runnable() {
					
					@Override
					public void run() {
						while(true){
							ChannelData channelData = null;
							try {
								channelData = up2MasterQueue.take();//获取从Server4Terminal发送过来的上行报文对象
								if(channelData == null){
									continue;
								}
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
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		
	}



	@Override
	public void start() throws Exception {
		new Thread(this).start();
	}

}
