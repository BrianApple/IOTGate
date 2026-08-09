package gate.rpc.rpcProcessor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import gate.base.cache.RPCCache;
import gate.remoting.RemoteServer;
import gate.rpc.annotation.RPCService;
import gate.rpc.dataBridge.RequestData;
import gate.rpc.dataBridge.ResponseData;
import gate.rpc.rpcService.RPCExportServiceImpl;
import gate.util.MixAll;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class RPCProcessorImpl implements RPCProcessor {
	
	@Override
	public void exportService() throws Exception {
		// 显式注册内置RPC服务(避免依赖包扫描——jar包运行时file协议扫描会失效)
		registerService(RPCExportServiceImpl.class);
		// 保留包扫描机制，支持后续扩展其他@RPCService实现类
		List<String> result = MixAll.getClazzName("gate.rpc.rpcService",false);
		for (String className : result) {
			Class<?> clazz = Class.forName(className);
			if(clazz.isAnnotationPresent(RPCService.class)){
				RPCCache.putClass(clazz.getSimpleName(), clazz);
			}
		}
		new RemoteServer().start();
		System.out.println("发布rpc服务完毕........");
	}
	
	private void registerService(Class<?> clazz){
		if(clazz.isAnnotationPresent(RPCService.class)){
			RPCCache.putClass(clazz.getSimpleName(), clazz);
			System.out.println("已注册RPC服务: " + clazz.getName());
		}
	}

	@Override
	public ResponseData executeService(RequestData requestData) {
		Class<?> clazz = RPCCache.getClass(requestData.getClassName()+"Impl");
		ResponseData responseData = null;
		try {
			Method method = clazz.getMethod(requestData.getMethodName(), requestData.getParamTyps());
			responseData = (ResponseData) method.invoke(clazz.newInstance(), requestData.getArgs());
			//请求响应代码一一对应
			responseData.setResponseNum(requestData.getRequestNum());
			return responseData;
		} catch (NoSuchMethodException | SecurityException e) {
			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		responseData = new ResponseData();
		responseData.setResponseNum(requestData.getRequestNum());
		return null;
	}
	
	
	
	

}
