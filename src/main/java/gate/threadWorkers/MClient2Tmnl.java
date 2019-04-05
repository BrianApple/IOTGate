package gate.threadWorkers;

import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gate.base.cache.ClientChannelCache;
import gate.base.chachequeue.CacheQueue;
import gate.base.domain.ChannelData;
import gate.concurrent.ThreadFactoryImpl;
import gate.util.StringUtils;
import io.netty.channel.Channel;

/**
 * 下行报文中转
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class MClient2Tmnl implements DataTransfer{

	private BlockingQueue<ChannelData> down2TmnlQueue;
	private final int poolSize;
	ExecutorService exService;
	public MClient2Tmnl(BlockingQueue<ChannelData> down2TmnlQueue ,int poolSize) {
		super();
		this.down2TmnlQueue = down2TmnlQueue;
		this.poolSize = poolSize;
		exService = Executors.newFixedThreadPool(poolSize,new ThreadFactoryImpl("msgTransWorker_Down_", false));
	}



	public void run() {
		
			
			for (int i=0 ; i < poolSize ; i++ ){
				exService.execute(new Runnable() {
					
					@Override
					public void run() {
						while(true){
							ChannelData channelData = null;
							try {
								channelData = down2TmnlQueue.take();//获取从Server4Terminal发送过来的上行报文对象
								if(channelData == null){
									continue;
								}
								Channel channel = ClientChannelCache.get(channelData.getIpAddress());//性能提升点
								if(channel != null){
									
									if(channel.isWritable()){
//										int len = channelData.getSocketData().getByteBuf().readableBytes();
//										byte[] car =  new byte[len];
//										channelData.getSocketData().getByteBuf().readBytes(car);
//										channelData.getSocketData().getByteBuf().readerIndex(0);
//										System.out.println("Gate Down = "+StringUtils.encodeHex(car));
										channel.writeAndFlush(channelData);
									}
									
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
