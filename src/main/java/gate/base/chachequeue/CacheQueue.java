package gate.base.chachequeue;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
	 * master channel
	 */
	private static ConcurrentHashMap<String, Channel> masterChannelCache ;
	/**
	 * 轮询策略
	 */
	private static CopyOnWriteArrayList<CachedChannel> roundCache ;
	
	/**
	 * 内部消息总线
	 */
	public static BlockingQueue<ChannelData> up2MasterQueue;
	
	public static BlockingQueue<ChannelData> down2TmnlQueue;
	static{
		
		ipCountRelationCache = new ConcurrentHashMap<String, Integer>();
		masterChannelCache = new ConcurrentHashMap<String, Channel>();
		roundCache = new CopyOnWriteArrayList<CachedChannel>();
		up2MasterQueue = new LinkedBlockingQueue<ChannelData>();
		down2TmnlQueue = new LinkedBlockingQueue<ChannelData>();
		
		System.out.println("GATE 初始化CacheQueue完成......");
		
	}
	
	
	
	/**
	 * 获取缓存中的master的channel对象
	 * @return
	 */
	public static Channel choiceMasterChannel(){
		int masterNum = CacheQueue.masterChannelCache.size();
		if(masterNum > 0){
			int nextIndex = (index + 1) % masterNum;
			index = nextIndex;
			return roundCache.get(nextIndex).getChannel();
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
		removeMasterChannelFromLocalCache(key);
		masterChannelCache.put(key, channel);
		roundCache.add(new CachedChannel(key,channel));
		
	}
	public static void removeMasterChannelFromLocalCache(String key ){
		Channel removedChannel = masterChannelCache.remove(key);
		roundCache.remove(new CachedChannel(key,removedChannel));
	}
}

class CachedChannel{
	
	private String sig;
	private Channel channel;
	public CachedChannel(String sig, Channel channel) {
		super();
		this.sig = sig;
		this.channel = channel;
	}
	@Override
	public boolean equals(Object obj) {
		return this.sig.equals(((CachedChannel)obj).sig);
	}
	@Override
	public int hashCode() {
		return this.sig.hashCode();
	}
	public String getSig() {
		return sig;
	}
	public void setSig(String sig) {
		this.sig = sig;
	}
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
