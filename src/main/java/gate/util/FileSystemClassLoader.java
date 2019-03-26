package gate.util;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月26日
 */
public class FileSystemClassLoader extends ClassLoader{
	
	
	String dirRoot = null;
	public FileSystemClassLoader(String dirRoot) {
		this.dirRoot = dirRoot;
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if(clazz != null ){
			return clazz;
		}else{
			try{
				clazz = super.loadClass(name);
			}catch(Exception e){
			}
			
			if(clazz != null){
				return clazz;
			}else{
				byte[] bytes = getBytesByName(name);
				clazz = defineClass(name, bytes, 0, bytes.length);
				if(clazz == null){
					throw new ClassNotFoundException();
				}else{
					return clazz;
				}
			}
		}
		
	}
	
	
	
	public byte[] getBytesByName(String name){
		String path = dirRoot+File.separator+name.replace(".", "/")+".class";
		
		InputStream is = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			is  = new FileInputStream(path);
			
			byte[] buffer = new byte[1024];
			int temp=0;
			while((temp=is.read(buffer))!=-1){
				baos.write(buffer, 0, temp);
			}
			
			return baos.toByteArray();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try {
				if(is!=null){
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(baos!=null){
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}

}
