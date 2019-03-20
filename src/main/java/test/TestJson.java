package test;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月20日
 */
public class TestJson {
	public static void main(String[] args) {
		Stu s = new Stu();
		s.setName("yc");
		s.setClazz(new Class<?>[]{String.class});
		String str = JSON.toJSONString(s);
		System.out.println("str"+str);

		Stu stu = JSON.parseObject(str, Stu.class);
		System.out.println(stu.getName());
	}
	
	
	
	

}

class Stu implements Serializable{
	private static final long serialVersionUID = 4155716943529879886L;
	
	String name;
	Class<?>[] clazz ;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class<?>[] getClazz() {
		return clazz;
	}
	public void setClazz(Class<?>[] clazz) {
		this.clazz = clazz;
	}
	
}
