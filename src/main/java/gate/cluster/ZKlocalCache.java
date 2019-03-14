package gate.cluster;

import java.util.concurrent.ConcurrentHashMap;

import gate.base.domain.LocalCache;
/**
 * 缓存zookeeper中master的节点信息
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月14日
 */
public class ZKlocalCache implements LocalCache{
	
	private  ConcurrentHashMap<String, String> zknodeCache = null;
	
	private ZKlocalCache(){
		if(inner.zkLocalCache != null){
			throw new IllegalStateException("禁止创建gate.cluster.ZKlocalCache对象！");
		}
		zknodeCache = new ConcurrentHashMap<>();
	}
	
	
	static class inner{
		static ZKlocalCache zkLocalCache = new ZKlocalCache();
		
	}
	

	@Override
	public Object get(Object key) {
		return zknodeCache.get(key);
	}

	@Override
	public void set(Object key, Object value) {
		zknodeCache.put(key.toString(), value.toString());
	}
	
	@Override
	public boolean del(Object key) {
		return zknodeCache.remove(key) == null;
	}
	
	public static ZKlocalCache getInstance(){
		return ZKlocalCache.inner.zkLocalCache;
	}

	
	

}
