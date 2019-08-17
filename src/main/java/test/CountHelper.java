package test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
/**
 * 测试使用的一些计数的变量--要合理考虑并发
 * 
 * @author BriansPC
 *
 */
public class CountHelper {
	public static int ThreadNum = 1;
	
	public static AtomicInteger clientRecieveCount ;
	public static AtomicInteger masterRecieveCount ;
	public static AtomicLong startTimeLong ;
	
	public static Long masterRecieveStartTime;//记录前置接收到第一条数据的时间
	
	static{
		startTimeLong = new AtomicLong(0);
		clientRecieveCount = new AtomicInteger(0);
		masterRecieveCount = new AtomicInteger(0);
	}

	
	
}
