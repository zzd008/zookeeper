package cn.jxust.zkdist;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

/*
 * 动态服务器上下线感知-服务器端
 */
public class DistributedServer {
	private ZooKeeper zk=null;
	private static final String connectString="111.230.243.106:2181";//连接云主机master
	private static final int sessionTimeout=30000;//超时时间 当停掉一个服务器时，在30秒之后才会认为服务器挂掉了
	private static final String paraentNode="/servers";
	
	
	/*
	 * 获取连接
	 */
	public void getConnection() throws IOException{
		zk=new ZooKeeper(connectString, sessionTimeout, new Watcher(){
			//监听器
			public void process(WatchedEvent event) {
				System.out.println("事件类型："+event.getType()+"，事件发生在："+event.getPath());
				try {
					zk.getChildren("/", true);//重复监听
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	
	/*
	 * 向zk注册信息
	 */
	public void registerServer(String hostName) throws KeeperException, InterruptedException{
		//判断父节点是否存在
		Stat stat = zk.exists(paraentNode, false);
		if(stat==null){
//			System.out.println("父节点不存在！");
			zk.create(paraentNode, "pararent".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		String node=zk.create(paraentNode+"/server", hostName.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//-e 加上序列号避免重复
		System.out.println("服务器："+hostName+"上线了！  创建临时节点："+node);
	}
	
	/*
	 * 业务逻辑
	 */
	public void handleBussiness(String hostName) throws InterruptedException{
		System.out.println(hostName+"  start working.....");
		//连接zk后，会开启一个线程，这时可以不写Thread.sleep，这样即使main函数的主线程退出了，程序也会一直运行。但是这样这个程序就没办法结束，所以zk把这个县城设置为了守护线程，只要主线程结束，这个线程就会跟着关闭，所以这里要Thread.sleep
		/*
		 * Thread t=new Thread( Runnable );
			t.setDaemon(true);
			t.start();
		 */
		Thread.sleep(Long.MAX_VALUE);//让该服务不断掉
	}
	
	/*
	 * 打成可执行runable jar 要有main函数
	 */
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		DistributedServer server=new DistributedServer();
		server.getConnection();
		//指定运行时参数
		server.registerServer(args[0]);
		server.handleBussiness(args[0]);		
	}
	
	
	/*
	 * 三台服务器
	 */
	@Test
	public void test1() throws IOException, InterruptedException, KeeperException{
		//获取zk连接
		DistributedServer server=new DistributedServer();
		server.getConnection();

		//利用zk连接注册服务器信息
		server.registerServer("192.168.110.111");
		
		//启动业务逻辑
		server.handleBussiness("192.168.110.111");			
	}
	
	@Test
	public void test2() throws IOException, InterruptedException, KeeperException{
		//获取zk连接
		DistributedServer server=new DistributedServer();
		server.getConnection();

		//利用zk连接注册服务器信息
		server.registerServer("192.168.110.112");
		
		//启动业务逻辑
		server.handleBussiness("192.168.110.112");			
	}
	
	@Test
	public void test3() throws IOException, InterruptedException, KeeperException{
		//获取zk连接
		DistributedServer server=new DistributedServer();
		server.getConnection();

		//利用zk连接注册服务器信息
		server.registerServer("192.168.110.113");
		
		//启动业务逻辑
		server.handleBussiness("192.168.110.113");			
	}
}
