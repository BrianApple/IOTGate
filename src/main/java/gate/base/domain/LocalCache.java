package gate.base.domain;
/**
 * 本地缓存统一接口
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public interface LocalCache {
	
	Object get(Object key);

	void set(Object key ,Object value);
	
	boolean del(Object key);
}
