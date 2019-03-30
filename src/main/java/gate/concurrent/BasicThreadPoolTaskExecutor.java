package gate.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * 基本线程池
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class BasicThreadPoolTaskExecutor {
	private static ExecutorService  service=null;
	private BasicThreadPoolTaskExecutor(){
		throw new AssertionError();
	}
	
	static{
		service = Executors.newCachedThreadPool(new ThreadFactoryImpl("basicExecutor_", false));
	}
	
	public static ExecutorService getBasicExecutor(){
		return service;
	}
	
}
