package gate.rpc.dataBridge;

import java.io.Serializable;
import java.util.List;
/**
 * 封装请求参数
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月17日
 */
public class RequestData implements Serializable{
	private static final long serialVersionUID = -497072374733332517L;
	
	private String className;
	private String methodName;
	private List<Class<?>> paramTyps;
	private List<Object> args;
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public List<Class<?>> getParamTyps() {
		return paramTyps;
	}
	public void setParamTyps(List<Class<?>> paramTyps) {
		this.paramTyps = paramTyps;
	}
	public List<Object> getArgs() {
		return args;
	}
	public void setArgs(List<Object> args) {
		this.args = args;
	}
	

	
}
