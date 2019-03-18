package gate.rpc.rpcProcessor;

import java.util.List;

import gate.base.cache.RPCCache;
import gate.rpc.annotation.RPCService;
import gate.rpc.dataBridge.RequestData;
import gate.rpc.dataBridge.ResponseData;
import gate.util.MixAll;

public class RPCProcessorImpl implements RPCProcessor {
	
	@Override
	public void exportService() throws Exception {
		List<String> result = MixAll.getClazzName("gate.rpc.rpcService",false);
		for (String className : result) {
			Class<?> clazz = Class.forName(className);
			if(clazz.isAnnotationPresent(RPCService.class)){
				RPCCache.putClass(className, clazz);
			}
		}
		System.out.println("发布rpc服务完毕........");
		
		
	}

	@Override
	public ResponseData executeService(RequestData requestData) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	

}
