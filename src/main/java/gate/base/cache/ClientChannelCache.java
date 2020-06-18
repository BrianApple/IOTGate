package gate.base.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * 该缓存类 用于缓存所有连接到网关的终端“ip:channel”信息,并提供了访问缓存的额方法
 * @author BriansPC
 *
 */
public class ClientChannelCache {
	
	private static  List<ConcurrentHashMap<String, Channel>> cacheList = new ArrayList<ConcurrentHashMap<String,Channel>>();
	private static  int scale = 10;
	
	static{
		initClientChannelCache();
		System.out.println("GATE 初始化ClientChannel缓存完成......");
	}
	/**
	 * 初始化存储终端channel的缓存
	 */
	public static void initClientChannelCache(){
		for(int i = 0 ; i < scale ; i++){
			cacheList.add(new ConcurrentHashMap<String, Channel>());
		}
	}
	/**
	 * 根据key  获取缓存数据的ConcurrentHashMap实体
	 * @param key
	 * @return
	 */
	private static  ConcurrentHashMap<String, Channel> getCacheInstance(String key){
		int hashCode = key.hashCode();
		hashCode = (hashCode < 0) ? -hashCode : hashCode;
		int index = hashCode % scale;
		return cacheList.get(index);
	}
	/**
	 * 插入数据
	 * @param key
	 * @param value
	 */
	public static  void set(String key , Channel value){
		
		getCacheInstance(key).put(key, value);
//		logOut("新增数据");
	}
	/**
	 * 获取数据
	 * @param key
	 */
	public static Channel get(String key){
		
		return getCacheInstance(key).get(key);
		
	}
	
	public static void removeOne(String key){
		
		Channel channel = getCacheInstance(key).remove(key);
		channel.closeFuture();
	}
	/**
	 * 清空所有缓存所有数据
	 */
	public static void clearAll(){
		for (ConcurrentHashMap<String, Channel> concurrentHashMap : cacheList) {
			concurrentHashMap.clear();
		}
	}
	private static void logOut(String msg){
		System.out.println(msg);
		for(int i = 0 ; i <cacheList.size() ; i ++){
			System.out.println("缓存监控.............ClientChannelMap"+i+"=="+cacheList.get(i).size());
		}
	}
}
