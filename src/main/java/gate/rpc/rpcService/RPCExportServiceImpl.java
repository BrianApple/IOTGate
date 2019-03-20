package gate.rpc.rpcService;

import java.util.ArrayList;
import java.util.List;

import gate.rpc.annotation.RPCService;
import gate.rpc.dataBridge.ResponseData;
/**
 * 多规约
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月18日
 */
@RPCService
public class RPCExportServiceImpl implements RPCExportService{

	@Override
	public ResponseData test(String str) {
		
		System.out.println("............rpc 测试服务..............str="+str);
		ResponseData ret = new ResponseData();
		List<Object> list = new ArrayList<>(1);
		list.add(1111);
		ret.setData(list);
		return ret;
	}

	
	

}
