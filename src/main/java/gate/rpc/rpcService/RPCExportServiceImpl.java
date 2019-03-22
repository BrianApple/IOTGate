package gate.rpc.rpcService;

import java.util.ArrayList;
import java.util.List;

import gate.base.cache.ProtocalStrategyCache;
import gate.rpc.annotation.RPCService;
import gate.rpc.dataBridge.ResponseData;
import gate.server.Server4Terminal;
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
	
	@Override
	public ResponseData getAllProtocal() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ResponseData addNewProtocal(List<Integer> str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseData updateProtocalByPid(String pid , List<Integer> str) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseData delProtocalByPid(String port) {
		
		Server4Terminal server4Terminal = ProtocalStrategyCache.protocalServerCache.get(port);
		server4Terminal.close();
		return new ResponseData();
	}

	@Override
	public ResponseData startProtocalServiceByPid(String pid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseData stopProtocalServiceByPid(String pid) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	

}
