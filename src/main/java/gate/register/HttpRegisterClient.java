package gate.register;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import com.alibaba.fastjson.JSONObject;

/**
 * 网关注册客户端
 *
 * -c 参数语义：网关主动注册到前端管理服务(IOTGateConsole)
 *  - 启动时 POST /gate/register 注册，失败每5s重试
 *  - 注册成功后每10s POST /gate/heartbeat 心跳
 *  - 心跳返回 retSig=404 说明Console侧已无该节点(如Console重启)，立即重新注册
 *  - JVM关闭时 POST /gate/unregister 反注册(尽力而为)
 *
 * 基于 JDK HttpURLConnection + fastjson(项目已有依赖)，不引入第三方HTTP库。
 * master(前置)数据通道仍走 -m 参数静态直连，本类不涉及任何前置通信代码。
 *
 * @author yangcheng
 * @date:   2026年8月10日
 */
public class HttpRegisterClient {

	/** 网关RPC服务端口(与RemoteServer一致) */
	public static final int RPC_PORT = 10916;

	/** Console默认端口 */
	private static final int DEFAULT_CONSOLE_PORT = 8686;

	/** 注册失败重试间隔(ms) */
	private static final long REGISTER_RETRY_INTERVAL_MS = 5000L;

	/** 心跳周期(ms) */
	private static final long HEARTBEAT_INTERVAL_MS = 10000L;

	/** 连续心跳失败次数达到该值触发重新注册(排除偶发网络抖动) */
	private static final int HEARTBEAT_FAIL_LIMIT = 3;

	/** HTTP连接/读取超时(ms) */
	private static final int HTTP_TIMEOUT_MS = 3000;

	/** Console地址 http://ip:port */
	private final String consoleBaseUrl;

	/** 本机IP(注册时上报) */
	private final String localIp;

	/** 网关编号 */
	private final int gateNum;

	private final Object lock = new Object();

	private volatile boolean running = false;
	private volatile boolean registered = false;
	private int heartbeatFailCount = 0;
	private Thread workerThread;

	/**
	 * @param consoleAddr Console地址，支持 ip / ip:port / http://ip:port，默认端口8686
	 * @param gateNum     网关编号(-n 参数)
	 */
	public HttpRegisterClient(String consoleAddr, int gateNum) {
		this.consoleBaseUrl = "http://" + normalizeAddr(consoleAddr);
		this.localIp = getLocalIp();
		this.gateNum = gateNum;
	}

	/**
	 * 启动注册+心跳工作线程(守护线程，不阻塞主流程)
	 */
	public void start() {
		synchronized (lock) {
			if (running) {
				return;
			}
			running = true;
		}
		workerThread = new Thread(this::run, "gate-register-client");
		workerThread.setDaemon(true);
		workerThread.start();
		System.out.println(String.format("网关主动注册已启动: console=%s localIp=%s rpcPort=%d gateNum=%d",
				consoleBaseUrl, localIp, RPC_PORT, gateNum));
	}

	/**
	 * 停止注册客户端(关闭时反注册)
	 */
	public void stop() {
		running = false;
		unregister();
	}

	/**
	 * 工作线程：未注册成功则重试注册，注册成功后周期性心跳
	 */
	private void run() {
		while (running) {
			if (!registered) {
				registered = doRegister();
				if (!registered) {
					sleep(REGISTER_RETRY_INTERVAL_MS);
					continue;
				}
				System.out.println(String.format("网关注册成功: %s -> %s", localIp, consoleBaseUrl));
			}
			boolean ok = doHeartbeat();
			if (ok) {
				heartbeatFailCount = 0;
			} else if (++heartbeatFailCount >= HEARTBEAT_FAIL_LIMIT) {
				System.out.println(String.format("连续%d次心跳失败，触发重新注册: %s", HEARTBEAT_FAIL_LIMIT, consoleBaseUrl));
				registered = false;
				heartbeatFailCount = 0;
			}
			sleep(HEARTBEAT_INTERVAL_MS);
		}
	}

	/**
	 * 注册：POST /gate/register
	 */
	private boolean doRegister() {
		JSONObject body = new JSONObject();
		body.put("ip", localIp);
		body.put("rpcPort", RPC_PORT);
		body.put("gateNum", gateNum);
		return post("/gate/register", body.toJSONString()) != null;
	}

	/**
	 * 心跳：POST /gate/heartbeat
	 * 返回 retSig=404 说明Console侧已无本节点(如Console重启)，立即触发重新注册
	 */
	private boolean doHeartbeat() {
		JSONObject body = new JSONObject();
		body.put("ip", localIp);
		String resp = post("/gate/heartbeat", body.toJSONString());
		if (resp == null) {
			return false;
		}
		try {
			JSONObject json = JSONObject.parseObject(resp);
			if (json != null && json.getIntValue("retSig") == 404) {
				System.out.println("Console侧已无本节点记录，立即重新注册: " + consoleBaseUrl);
				registered = false;
				return true;
			}
			return true;
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * 反注册：POST /gate/unregister（尽力而为，失败由心跳超时判定兜底）
	 */
	public void unregister() {
		try {
			JSONObject body = new JSONObject();
			body.put("ip", localIp);
			post("/gate/unregister", body.toJSONString());
			System.out.println("网关已向Console反注册: " + localIp);
		} catch (Exception e) {
			// 忽略：关闭场景下网络可能已不可用
		}
	}

	/**
	 * HTTP POST JSON
	 * @return 响应体，失败返回 null
	 */
	private String post(String path, String json) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(consoleBaseUrl + path);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(HTTP_TIMEOUT_MS);
			conn.setReadTimeout(HTTP_TIMEOUT_MS);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			try (OutputStream os = conn.getOutputStream()) {
				os.write(json.getBytes(StandardCharsets.UTF_8));
			}
			int code = conn.getResponseCode();
			if (code == 200) {
				try (InputStream is = conn.getInputStream()) {
					int size = is.available() > 0 ? is.available() : 256;
					byte[] buf = new byte[size];
					int len = is.read(buf);
					return len > 0 ? new String(buf, 0, len, StandardCharsets.UTF_8) : "";
				}
			}
			if (running) {
				System.err.println(String.format("注册HTTP请求失败: %s%s code=%d", consoleBaseUrl, path, code));
			}
			return null;
		} catch (Exception e) {
			if (running) {
				System.err.println(String.format("注册HTTP请求异常: %s%s %s", consoleBaseUrl, path, e.getMessage()));
			}
			return null;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 归一化Console地址：去掉 http:// 前缀，纯IP则补默认端口8686
	 */
	private static String normalizeAddr(String addr) {
		String a = addr.trim();
		if (a.startsWith("http://")) {
			a = a.substring("http://".length());
		}
		if (a.startsWith("https://")) {
			a = a.substring("https://".length());
		}
		// 纯IPv4地址(不含端口)补默认端口
		if (a.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
			a = a + ":" + DEFAULT_CONSOLE_PORT;
		}
		return a;
	}

	/**
	 * 获取本机第一个非回环IPv4地址，失败兜底127.0.0.1
	 */
	private static String getLocalIp() {
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				if (!ni.isUp() || ni.isLoopback()) {
					continue;
				}
				Enumeration<InetAddress> addrs = ni.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
						return addr.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			// 忽略，兜底127.0.0.1
		}
		return "127.0.0.1";
	}
}
