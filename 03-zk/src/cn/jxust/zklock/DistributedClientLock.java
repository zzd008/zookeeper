package cn.jxust.zklock;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.junit.Test;

/*
 * 分布式共享锁-客户端（zk作为第三方）
 */
public class DistributedClientLock {
	private ZooKeeper zk=null;
	private static final String connectString="111.230.243.106:2181";//连接云主机master
	private static final int sessionTimeout=10000;
	private static final String groupNode="/locks";//父节点
	private static final String subNode="sub";//子节点
	private boolean haveLock=false;//是否拿到锁对象
	
	private volatile String thisPath;//记录自己创建的子节点路径
	
	/*
	 * 连接zk
	 */
	public void connectZk() throws IOException, KeeperException, InterruptedException{
		zk=new ZooKeeper(connectString, sessionTimeout, new Watcher(){
			//监听器
			public void process(WatchedEvent event) {
				try {
					//判断事件类型，只处理子节点变化时间
					if(event.getType()==EventType.NodeChildrenChanged&&event.getPath().equals(groupNode)){
						System.out.println("事件类型："+event.getType()+"，事件发生在："+event.getPath());
						//获取子节点，并对父节点进行监听
						List<String> childrenNodes = zk.getChildren(groupNode, true);
						//比较大小
						String thisNode = thisPath.substring((groupNode+"/").length());//截取自己节点的名称
						Collections.sort(childrenNodes);//对所有子节点进行排序，默认从小到达
						//看自己是否是第一个最小的
						if(childrenNodes.indexOf(thisNode)==0){//是最小的
							//去访问资源，访问后删除锁对象
							doSomething();
							//重新注册一把新的锁
							thisPath=zk.create(groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
						}
						
						
						
					}
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}

		});
		
		//判断父节点是否存在
		if(zk.exists(groupNode, false) == null){
			zk.create(groupNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		//1、程序进来先注册一把锁到zk sub0001 sub0002 sub0003
		thisPath=zk.create(groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		//wait一会，便于观察
		Thread.sleep(new Random().nextInt(2000));
		//从zk的锁目录下获取所有的子节点并监听父节点
		List<String> childrenNodes = zk.getChildren(groupNode, true);
		if(childrenNodes.size()==1){//只有自己一台，没人去抢资源
			//去访问资源，访问后删除锁对象
			doSomething();
			//重新注册一把新的锁
			thisPath=zk.create(groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		}
		else{
			//等待
//			Thread.sleep(Long.MAX_VALUE);
		}
	}

	/*
	 * 访问资源
	 */
	private void doSomething() throws InterruptedException, KeeperException {
		try {
			System.out.println("我拿到了锁："+thisPath);
			Thread.sleep(2000);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("资源已访问结束！");
			//释放锁
			zk.delete(thisPath, -1);
		}
	}
	
	public static void main(String[] args) {
		for(int i=0;i<5;i++){
			new Thread(new Runnable() {
				public void run() {
					try {
						DistributedClientLock dcl=new DistributedClientLock();
						dcl.connectZk();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
	}
	
	@Test
	public void test1() throws IOException, InterruptedException, KeeperException {
		try {
			DistributedClientLock dcl=new DistributedClientLock();
			dcl.connectZk();
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test2() throws IOException, InterruptedException, KeeperException {
		try {
			DistributedClientLock dcl=new DistributedClientLock();
			dcl.connectZk();
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void test3() throws IOException, InterruptedException, KeeperException {
		try {
			DistributedClientLock dcl=new DistributedClientLock();
			dcl.connectZk();
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
