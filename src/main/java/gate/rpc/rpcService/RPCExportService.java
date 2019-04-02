package gate.rpc.rpcService;

import java.util.List;

import gate.rpc.dataBridge.ResponseData;

/**
 * rpc服务接口
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月19日
 */
public interface RPCExportService {
	/**
	 * 测试用rpc
	 * @param str
	 * @return
	 */
	ResponseData test(String str);
	
	/**
	 * 获取当前网关所支持的所有规约信息
	 * @return
	 */
	ResponseData getAllProtocal(boolean onlyRunning);
	
	
	/**
	 * 新增规约策略
	 * @param pid
	 * @param str 共8位 =pId+ isBigEndian+ beginHexVal+ lengthFieldOffset+ lengthFieldLength+ isDataLenthIncludeLenthFieldLenth+ exceptDataLenth+ port
	 * @param startAtOnce  是否立即启动服务
	 * @return
	 */
	ResponseData addNewProtocal(String pid ,List<Integer> str,boolean startAtOnce);
	
	/**
	 * 更新规约策略
	 * @param str
	 * @return
	 */
	ResponseData updateProtocalByPid(String pid,List<Integer> str);
	
	/**
	 * 删除规约，删除之后不能再有其它操作
	 * @param str
	 * @return
	 */
	ResponseData delProtocalByPid(String pId);
	
	//----------------------------------------------------------
	/**
	 * 通过指定端口开启被stop的网关服务
	 * @param str
	 * @return
	 */
	ResponseData startProtocalServiceByPid(String pId);
	
	/**
	 * 通过指定端口关闭相关网关服务
	 * @param str
	 * @return
	 */
	ResponseData stopProtocalServiceByPid(String pId);
	

}

