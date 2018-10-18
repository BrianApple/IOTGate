package gate.base.chachequeue;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import gate.base.domain.ChannelData;
import gate.base.domain.SocketData;
import io.netty.channel.Channel;
/**
 * 数据流转容器
 * @author BriansPC
 *
 */
public class CacheQueue {
	
	/**
	 * 记录集中器对应的连接次数
	 */
	public static ConcurrentHashMap<String, Integer> ipCountRelationCache ;
	/**
	 * 缓存前置channel
	 * 网关连接到前置之后 将对应的channel缓存起来 通过ip获取
	 */
	public static ConcurrentHashMap<String, Channel> masterChannelCache ;
	/**
	 * Server4Terminel接收到消息之后 将消息存放到up2MasterQueue队列中
	 */
	public static LinkedBlockingQueue<ChannelData> up2MasterQueue;
	
	static{
		
		ipCountRelationCache = new ConcurrentHashMap<String, Integer>();
		masterChannelCache = new ConcurrentHashMap<String, Channel>();
		up2MasterQueue = new LinkedBlockingQueue<ChannelData>();
		System.out.println("GATE 初始化CacheQueue完成......");
		
	}
	
	
	
	/**
	 * 获取缓存中的master的channel对象
	 * @return
	 */
	public static Channel choiceMasterChannel(){
		//后续可以考虑轮寻策略
		//目前只考虑连接一个前置
		if(!CacheQueue.masterChannelCache.isEmpty()){
			Iterator<Entry<String, Channel>> it=CacheQueue.masterChannelCache.entrySet().iterator();
			if(it.hasNext()){
				Entry<String, Channel> entry = it.next();
				String masterIp = entry.getKey();
				return entry.getValue();
			}
		}
		return null;
	}
	/**
	 * 清空  终端连接序号缓存
	 */
	public static void clearIpCountRelationCache(){
		ipCountRelationCache.clear();
	}
	/**
	 * 清空 前置连接缓存
	 */
	public static void clearMasterChannelCache(){
		masterChannelCache.clear();
	}
}
