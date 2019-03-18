package gate.rpc.rpcProcessor;

import gate.rpc.dataBridge.RequestData;
import gate.rpc.dataBridge.ResponseData;

/**
 * RPC服务接口
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月17日
 */
public interface RPCProcessor {
	/**
	 * 发布rpc服务
	 * @throws Exception
	 */
	void exportService() throws Exception ;
	
	/**
	 * 调用rpc服务
	 * @param requestData
	 * @return
	 */
	ResponseData executeService(RequestData requestData);
}
