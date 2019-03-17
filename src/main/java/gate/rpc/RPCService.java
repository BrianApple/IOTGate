package gate.rpc;

import gate.rpc.dataBridge.RequestData;
import gate.rpc.dataBridge.ResponseData;

/**
 * RPC服务接口
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月17日
 */
public interface RPCService {

	void exportService ();
	
	ResponseData executeService(RequestData requestData);
}
