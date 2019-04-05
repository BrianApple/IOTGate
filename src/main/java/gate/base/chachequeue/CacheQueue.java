package gate.base.chachequeue;


import java.util.AbstractQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import gate.base.domain.ChannelData;
import io.netty.channel.Channel;
/**
 * 数据流转容器
 * @author BriansPC
 *
 */
public class CacheQueue {
	static volatile int index = 0; // 索引:指定起始位置
	/**
	 * 记录集中器对应的连接次数
	 */
	public static ConcurrentHashMap<String, Integer> ipCountRelationCache ;
	/**
	 * 缓存前置channel
	 * 网关连接到前置之后 将对应的channel缓存起来 通过ip获取
	 */
	private static ConcurrentHashMap<String, Channel> masterChannelCache ;
	/**
	 * 轮询策略
	 */
	private static CopyOnWriteArrayList<Channel> roundCache ;
	
	/**
	 * Server4Terminel接收到消息之后 将消息存放到up2MasterQueue队列中
	 */
	public static BlockingQueue<ChannelData> up2MasterQueue;
	
	public static BlockingQueue<ChannelData> down2TmnlQueue;
	static{
		
		ipCountRelationCache = new ConcurrentHashMap<String, Integer>();
		masterChannelCache = new ConcurrentHashMap<String, Channel>();
		roundCache = new CopyOnWriteArrayList<Channel>();
		up2MasterQueue = new LinkedBlockingQueue<ChannelData>();
		down2TmnlQueue = new LinkedBlockingQueue<ChannelData>();
		
		System.out.println("GATE 初始化CacheQueue完成......");
		
	}
	
	
	
	/**
	 * 获取缓存中的master的channel对象
	 * @return
	 */
	public static Channel choiceMasterChannel(){
		//TODO 轮寻策略
		int masterNum = CacheQueue.masterChannelCache.size();
		if(masterNum > 0){
			int nextIndex = (index + 1) % masterNum;
			index = nextIndex;
			return roundCache.get(nextIndex);
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
	 * 清空 前置连接以及前置会话策略缓存
	 */
	public static void clearMasterChannelCache(){
		masterChannelCache.clear();
		roundCache.clear();
	}
	
	public static void addMasterChannel2LocalCache(String key ,Channel channel ){
		masterChannelCache.put(key, channel);
		roundCache.add(channel);
		
	}
	public static void removeMasterChannelFromLocalCache(String key ){
		Channel removedChannel = masterChannelCache.remove(key);
		roundCache.remove(removedChannel);
	}
}
