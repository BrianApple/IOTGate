package gate.base.domain;

public interface LocalCache {
	
	Object get(Object key);

	void set(Object key ,Object value);
	
	boolean del(Object key);
}
