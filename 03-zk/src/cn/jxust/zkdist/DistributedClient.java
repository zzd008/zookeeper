package cn.jxust.zkdist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

/*
 * 动态服务器上下线感知-客户端
 */
public class DistributedClient {
	private ZooKeeper zk=null;
	private static final String connectString="111.230.243.106:2181";//连接云主机master
	private static final int sessionTimeout=30000;
	private static final String paraentNode="/servers";

	//volatile的意义：让每个线程读写同一个serverList，而不是拷贝副本再去修改
	private volatile List<String> serverList;
	/*
	 * 获取连接
	 */
	public void getConnection() throws IOException{
		zk=new ZooKeeper(connectString, sessionTimeout, new Watcher(){
			//监听器
			public void process(WatchedEvent event) {
				System.out.println("事件类型："+event.getType()+"，事件发生在："+event.getPath());
				try {
					//当服务器发生改变时，重新更新服务器列表信息，并注册监听
					getServerList();
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	
	/*
	 * 获取servers（父）节点下的子节点信息，并对父节点监听
	 */
	public void getServerList() throws KeeperException, InterruptedException{
		List<String> children = zk.getChildren(paraentNode, true);//获取子节点
		//先创建一个局部的list来存服务器的信息
		List<String> servers=new ArrayList<String>();
		//获取每个服务器的信息
		for(String child:children){
			byte[] data = zk.getData(paraentNode+"/"+child, false, null);//不用对子节点进行（数据）监听，只需监听父节点（servers）即可感知子节点是否变化。做负载均衡时可以监听子节点的数据变化，因为当有客户端连接到服务器时，服务器存的数据会发生改变
			servers.add(new String(data));
		}
		//把servers赋给serverList，供业务线程使用
		serverList=servers;//一次性赋值，最好不要一下一下的改
		
		System.out.println(serverList);
	}
	
	/*
	 * 业务线程
	 */
	public void handleBussiness() throws InterruptedException{
		System.out.println("client  start working.....");
		//连接zk后，会开启一个线程，这时可以不写Thread.sleep，这样即使main函数的主线程退出了，程序也会一直运行。但是这样这个程序就没办法结束，所以zk把这个县城设置为了守护线程，只要主线程结束，这个线程就会跟着关闭，所以这里要Thread.sleep.
		/*
		 * Thread t=new Thread( Runnable );
			t.setDaemon(true);
			t.start();
		 */
		Thread.sleep(Long.MAX_VALUE);//让该服务不断掉
	}
	
	/*打成可执行runable jar 要有main函数
	 * 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
		DistributedClient client=new DistributedClient();
		client.getConnection();
		client.getServerList();
		client.handleBussiness();
	}
	
	@Test
	public void test1() throws KeeperException, InterruptedException, IOException{
		//获取zk连接
		DistributedClient client=new DistributedClient();
		client.getConnection();
		
		//获取servers子节点信息并监听，从中获取服务器信息列表
		client.getServerList();
		
		//业务线程启动
		client.handleBussiness();
	}
	
	
	
}
