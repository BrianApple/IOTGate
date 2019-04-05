package gate.threadWorkers;

import gate.base.domain.ChannelData;

/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年4月4日
 */
public interface DataTransfer extends Runnable {
	void start()  throws Exception ;
}
