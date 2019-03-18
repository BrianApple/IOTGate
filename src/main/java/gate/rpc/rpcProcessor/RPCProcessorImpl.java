package gate.rpc.rpcProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		Class<?> clazz = RPCCache.getClass(requestData.getClassName());
		try {
			Method method = clazz.getMethod(requestData.getMethodName(), requestData.getParamTyps());
			ResponseData responseData = (ResponseData) method.invoke(clazz, requestData.getArgs());
			return responseData;
		} catch (NoSuchMethodException | SecurityException e) {
			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	

}
