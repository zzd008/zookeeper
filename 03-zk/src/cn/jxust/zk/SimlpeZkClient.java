package cn.jxust.zk;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

/*
 * zk java客户端api
 */
public class SimlpeZkClient {
	static ZooKeeper zkClient = null;
	static CountDownLatch countDownLatchnew = new CountDownLatch(1);
	
	private static final String connectString="192.168.110.133:2181,192.168.110.134:2181,192.168.110.135:2181";//连接zk集群，当第一个连接不上会尝试连接第二个
	private static final int sessionTimeout=8000;//会话超时时间
	private static final Watcher watcher=new Watcher() {//监听器
		public void process(WatchedEvent event) {
			// TODO Auto-generated method stub
			//收到事件通知后的回调函数（我们对事件的处理逻辑）
			System.out.println("监听："+event.getType()+"----"+event.getPath());//监听事件的类型----发生的节点
			countDownLatchnew.countDown();
			try {
				zkClient.getChildren("/", true);//解决监听器只能监听一次的问题：监听一次后失效，再注册一个监听，相当于重复调用process监听方法,
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	};
	/*
	 *  当一个ZK的实例被创建时,会启动一个线程连接到ZK服务.由于init函数的调用是立即返回的,因此在使用新建的Zk对象之前一定要等待其与ZK服务之间成功建立连接（异步）.使用Java的CountDownLatch类来阻止使用新建的ZK对象,直到这个ZK对象已经准备就绪.
	  	当客户端已经与ZooKeeper服务建立连接后,Watcher的process()方法会被调用,参数是一个用于表示该连接的事件.
		在接收到一个连接事件(以Watcher.Event.KeeperState的枚举型值SyncConnected来表示)时,我们通过调用CountDownLatch的countDown()方法来递减它的计数器.
		锁存器(计数器countDownLatchnew)被创建时带有一个值为1的计数器,用于表示在他释放所有等待线程之前需要发生的事件数.
		在调用countDown()方法之后,计数器的值变为0,则await()方法返回.
		现在connect()方法已返回,然后执行创建节点的方法.
	 */
	@Before
	public void init() throws IOException, InterruptedException{//初始化客户端连接对象
		zkClient = new ZooKeeper(connectString, sessionTimeout, watcher);//上传的数据可以使任何类型，转为byte即可
		countDownLatchnew.await(); 
		//另一种解决办法
		States state = zkClient.getState();//获取zk连接状态
		while(state!=States.CONNECTED){//连接成功
			Thread.sleep(5000);
		}
//		System.out.println("客户端对象 创建成功!");
	}
	
	@After
	public void close() throws InterruptedException{//关闭连接
		zkClient.close();
		System.out.println("连接对象已经关闭！");
	}
	
	@Test
	public void testCreate() throws KeeperException, InterruptedException{//创建节点---create /aaa 123
		//参数：要创建的节点路径  节点的数据 节点的权限  节点数据的数据类型
		String node = zkClient.create("/eclispe", "hellozk".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println("节点创建成功！");
	}
	
	@Test
	public void testExisit() throws KeeperException, InterruptedException{//判断节点是否存在---get /app1
		Stat stat = zkClient.exists("/eclispe", false);//stat 如果节点存在，则返回节点的元数据，不存在则stat为null
//		System.out.println(stat==null?"不存在":stat);
		System.out.println(stat==null?"不存在":"存在");//stat.getCtime();stat.getCzxid();stat.getDataLength();
	}
	
	@Test
	public void getChildren() throws KeeperException, InterruptedException{//获取子节点----ls /
		List<String> children = zkClient.getChildren("/", true);//true表示用创建zkClient时的监听
		System.out.println("子节点有：");
		for(String child:children){
			System.out.println(child);
		}
		Thread.sleep(Long.MAX_VALUE);//让线程休眠（不让程序退出），进行监听 在linux下zkCli.sh中创建一个/aaa 这里便会有监听发生 注：只监听一次
	}
	
	/*
	 * 数据的增删改查
	 */
	
	@Test
	public void getNodeData() throws KeeperException, InterruptedException{//获取数据---get /eclispe
		byte[] data = zkClient.getData("/eclispe", false, null);
//		byte[] data = zkClient.getData("/eclispe", false, new Stat());//数据可能有很多版本（新版本/老版本），指定stat去查找数据
		System.out.println("数据为:"+new String(data));
	}
	
	@Test
	public void deleteNode() throws InterruptedException, KeeperException{//删除节点--delete /eclispe
//		 要删除的节点没有子节点
		zkClient.delete("/app2", -1);//参数二：指定要删除的版本，-1表示删除所有的版本
		System.out.println("节点删除成功！");
		
	}
	
	@Test
	public void testDeleteNodes() throws InterruptedException, KeeperException{
		deleteNodes("/app2");
	}
	
	//Junit的test方法中不能含有参数
	public void deleteNodes(String path) throws InterruptedException, KeeperException{//删除节点--rmr /eclispe
//		 zk不支持递归删除节点,所以必须先将子节点删除才能删除父节点
		List<String> children = zkClient.getChildren(path, false);
		for(String child:children){
			child=path+"/"+child;
			System.out.println(child);
			if((zkClient.getChildren(child, false)).size()==0){//该节点没有子节点了
				zkClient.delete(child, -1);
			}else{
				deleteNodes(child);
			}
		}
		zkClient.delete(path, -1);//参数二：指定要删除的版本，-1表示删除所有的版本
		System.out.println("节点递归删除成功！");
		
	}
	
	@Test
	public void setData() throws InterruptedException, KeeperException{//修改节点数据--set /eclispe aaa
		zkClient.setData("/eclispe","helloworld".getBytes(),-1);//参数3：指定要覆盖的版本，-1表示覆盖所有的版本
		System.out.println("数据修改成功！");
	}
	
	
}
