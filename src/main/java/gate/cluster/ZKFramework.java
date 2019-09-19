package gate.cluster;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import gate.Entrance;
import gate.base.cache.Cli2MasterLocalCache;
import gate.base.cache.ZKMasterNodeLocalCache;
import gate.base.domain.LocalCache;
import gate.client.Client2Master;
import gate.concurrent.BasicThreadPoolTaskExecutor;
import gate.concurrent.ThreadFactoryImpl;
import gate.util.MixAll;


/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月7日
 */
public class ZKFramework {
	
	private ExecutorService basicTaskExecutor = BasicThreadPoolTaskExecutor.getBasicExecutor() ;
	
	private Cli2MasterLocalCache cli2MasterLocalCache = Cli2MasterLocalCache.getInstance();
	
	private CuratorFramework cf ;
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactoryImpl("zkNodeCacheUpdate_", true));
	
	private final String PARENT_PATH = "/iotGate2Master";
	
	LocalCache zkNodeCache = ZKMasterNodeLocalCache.getInstance();
	
	public void init(String zkAddr){
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		cf = CuratorFrameworkFactory.builder()
					.connectString(zkAddr)
					.sessionTimeoutMs(6000)
					.retryPolicy(retryPolicy)
					.build();
		System.out.println("zk连接中。。。。。。");
		//3 开启连接
		cf.start();
		while(cf.getState() != CuratorFrameworkState.STARTED){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("zk连接成功。。。。。");
		try {
			zNodeListener();
			addGateNode();
//			downloadData2LocalCache();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public void zNodeListener() throws Exception{
		
		if(cf.checkExists().forPath(PARENT_PATH) == null){
			// 创建根节点
			cf.create().withMode(CreateMode.PERSISTENT).forPath(PARENT_PATH,"pastoralDog init".getBytes());
		}
		PathChildrenCache cache = new PathChildrenCache(cf, PARENT_PATH, true);
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_ADDED:
					System.out.println("CHILD_ADDED :" + event.getData().getPath());
					try {
						String val = new String(event.getData().getData());
//						Entrance.masterAddrs.add(val);
						link2MasterNode(val);
						addNode2Cache(val);
						Entrance.locks.countDown();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
					break;
				case CHILD_UPDATED:
					System.out.println("CHILD_UPDATED :" + event.getData().getPath());
					System.out.println("DATA :" + new String(event.getData().getData()));
					break;
				case CHILD_REMOVED:
					System.out.println("CHILD_REMOVED :" + event.getData().getPath());
					String ip = new String(event.getData().getData());
					try {
						delNode2Cache(ip);
						Client2Master  client2Master=(Client2Master) cli2MasterLocalCache.get(ip);
						//关闭与相应前置对应的client
						client2Master.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					break;
				default:
					break;
				}
			}
		});
	}
	
	private boolean isCluster(){
		
		return true;
	}
	
	/**
	 * 定时与zookeeper同步节点信息--同步周期为1min（作为监听接口的补偿方案）
	 * @throws Exception
	 */
	@Deprecated
	private void downloadData2LocalCache() throws Exception{
		scheduledExecutor.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				List<String> dataList = null;
				try {
					List<String> pathList = cf.getChildren().forPath("/iotgate");
					dataList = new ArrayList<>();
					for (String string : pathList) {
						//遍历获取节点对应的DATA值
						String data = new String(cf.getData().forPath("/iotgate/c1"));
						dataList.add(data);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			}
		}, 1, 1, TimeUnit.MINUTES);
		
		
	}
	/**
	 * 
	 */
	public void addGateNode(){
		try {
			String addr = MixAll.linuxLocalIP();
			cf.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/iotGate2RPC/"+addr,addr.getBytes());
			System.out.println("********zookeeper注册GATE信息成功！********");
		} catch (Exception e) {
			System.err.println("zookeeper注册GATE信息失败");
		}
	}
	
	/**
	 * 新增节点
	 * @param ip
	 */
	private void addNode2Cache(String nodeIp) throws Exception{
		if(zkNodeCache.get(nodeIp) == null){
			zkNodeCache.set(nodeIp, nodeIp);
		}
		
	}
	/**
	 * 更新节点
	 */
	private void updateNode2Cache(String nodeIp)throws Exception{
		//TODO 节点更新
	}
	/**
	 * 删除节点
	 * @param nodeIp
	 * @throws Exception
	 */
	private void delNode2Cache(String nodeIp)throws Exception{
		if (zkNodeCache.get(nodeIp) == null){
			return;
		}
		
		zkNodeCache.del(nodeIp);
	}
	/**
	 * 
	 * @param addr
	 */
	private void link2MasterNode(String addr){
		new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println(String.format("！！！前置服务%s连接成功,前置端口必须为8888", addr));
					Client2Master client2Master = new Client2Master();
					client2Master.bindAddress2Client(client2Master.configClient(addr,8888,false));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		},"gate2masterThread_ip_"+addr).start();
	}
	
	public void start(String zkAddr){
		if(isCluster()){
			basicTaskExecutor.execute(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						init(zkAddr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}else{
			System.out.println("********当前前置被指定为单机版，无需注册节点信息到zookeeper********");
		}
		
		
	}
	
}
