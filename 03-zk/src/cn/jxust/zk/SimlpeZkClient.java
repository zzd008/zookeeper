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
 * zk java�ͻ���api
 */
public class SimlpeZkClient {
	static ZooKeeper zkClient = null;
	static CountDownLatch countDownLatchnew = new CountDownLatch(1);
	
	private static final String connectString="192.168.110.133:2181,192.168.110.134:2181,192.168.110.135:2181";//����zk��Ⱥ������һ�����Ӳ��ϻ᳢�����ӵڶ���
	private static final int sessionTimeout=8000;//�Ự��ʱʱ��
	private static final Watcher watcher=new Watcher() {//������
		public void process(WatchedEvent event) {
			// TODO Auto-generated method stub
			//�յ��¼�֪ͨ��Ļص����������Ƕ��¼��Ĵ����߼���
			System.out.println("������"+event.getType()+"----"+event.getPath());//�����¼�������----�����Ľڵ�
			countDownLatchnew.countDown();
			try {
				zkClient.getChildren("/", true);//���������ֻ�ܼ���һ�ε����⣺����һ�κ�ʧЧ����ע��һ���������൱���ظ�����process��������,
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	};
	/*
	 *  ��һ��ZK��ʵ��������ʱ,������һ���߳����ӵ�ZK����.����init�����ĵ������������ص�,�����ʹ���½���Zk����֮ǰһ��Ҫ�ȴ�����ZK����֮��ɹ��������ӣ��첽��.ʹ��Java��CountDownLatch������ֹʹ���½���ZK����,ֱ�����ZK�����Ѿ�׼������.
	  	���ͻ����Ѿ���ZooKeeper���������Ӻ�,Watcher��process()�����ᱻ����,������һ�����ڱ�ʾ�����ӵ��¼�.
		�ڽ��յ�һ�������¼�(��Watcher.Event.KeeperState��ö����ֵSyncConnected����ʾ)ʱ,����ͨ������CountDownLatch��countDown()�������ݼ����ļ�����.
		������(������countDownLatchnew)������ʱ����һ��ֵΪ1�ļ�����,���ڱ�ʾ�����ͷ����еȴ��߳�֮ǰ��Ҫ�������¼���.
		�ڵ���countDown()����֮��,��������ֵ��Ϊ0,��await()��������.
		����connect()�����ѷ���,Ȼ��ִ�д����ڵ�ķ���.
	 */
	@Before
	public void init() throws IOException, InterruptedException{//��ʼ���ͻ������Ӷ���
		zkClient = new ZooKeeper(connectString, sessionTimeout, watcher);//�ϴ������ݿ���ʹ�κ����ͣ�תΪbyte����
		countDownLatchnew.await(); 
		//��һ�ֽ���취
		States state = zkClient.getState();//��ȡzk����״̬
		while(state!=States.CONNECTED){//���ӳɹ�
			Thread.sleep(5000);
		}
//		System.out.println("�ͻ��˶��� �����ɹ�!");
	}
	
	@After
	public void close() throws InterruptedException{//�ر�����
		zkClient.close();
		System.out.println("���Ӷ����Ѿ��رգ�");
	}
	
	@Test
	public void testCreate() throws KeeperException, InterruptedException{//�����ڵ�---create /aaa 123
		//������Ҫ�����Ľڵ�·��  �ڵ������ �ڵ��Ȩ��  �ڵ����ݵ���������
		String node = zkClient.create("/eclispe", "hellozk".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println("�ڵ㴴���ɹ���");
	}
	
	@Test
	public void testExisit() throws KeeperException, InterruptedException{//�жϽڵ��Ƿ����---get /app1
		Stat stat = zkClient.exists("/eclispe", false);//stat ����ڵ���ڣ��򷵻ؽڵ��Ԫ���ݣ���������statΪnull
//		System.out.println(stat==null?"������":stat);
		System.out.println(stat==null?"������":"����");//stat.getCtime();stat.getCzxid();stat.getDataLength();
	}
	
	@Test
	public void getChildren() throws KeeperException, InterruptedException{//��ȡ�ӽڵ�----ls /
		List<String> children = zkClient.getChildren("/", true);//true��ʾ�ô���zkClientʱ�ļ���
		System.out.println("�ӽڵ��У�");
		for(String child:children){
			System.out.println(child);
		}
		Thread.sleep(Long.MAX_VALUE);//���߳����ߣ����ó����˳��������м��� ��linux��zkCli.sh�д���һ��/aaa �������м������� ע��ֻ����һ��
	}
	
	/*
	 * ���ݵ���ɾ�Ĳ�
	 */
	
	@Test
	public void getNodeData() throws KeeperException, InterruptedException{//��ȡ����---get /eclispe
		byte[] data = zkClient.getData("/eclispe", false, null);
//		byte[] data = zkClient.getData("/eclispe", false, new Stat());//���ݿ����кܶ�汾���°汾/�ϰ汾����ָ��statȥ��������
		System.out.println("����Ϊ:"+new String(data));
	}
	
	@Test
	public void deleteNode() throws InterruptedException, KeeperException{//ɾ���ڵ�--delete /eclispe
//		 Ҫɾ���Ľڵ�û���ӽڵ�
		zkClient.delete("/app2", -1);//��������ָ��Ҫɾ���İ汾��-1��ʾɾ�����еİ汾
		System.out.println("�ڵ�ɾ���ɹ���");
		
	}
	
	@Test
	public void testDeleteNodes() throws InterruptedException, KeeperException{
		deleteNodes("/app2");
	}
	
	//Junit��test�����в��ܺ��в���
	public void deleteNodes(String path) throws InterruptedException, KeeperException{//ɾ���ڵ�--rmr /eclispe
//		 zk��֧�ֵݹ�ɾ���ڵ�,���Ա����Ƚ��ӽڵ�ɾ������ɾ�����ڵ�
		List<String> children = zkClient.getChildren(path, false);
		for(String child:children){
			child=path+"/"+child;
			System.out.println(child);
			if((zkClient.getChildren(child, false)).size()==0){//�ýڵ�û���ӽڵ���
				zkClient.delete(child, -1);
			}else{
				deleteNodes(child);
			}
		}
		zkClient.delete(path, -1);//��������ָ��Ҫɾ���İ汾��-1��ʾɾ�����еİ汾
		System.out.println("�ڵ�ݹ�ɾ���ɹ���");
		
	}
	
	@Test
	public void setData() throws InterruptedException, KeeperException{//�޸Ľڵ�����--set /eclispe aaa
		zkClient.setData("/eclispe","helloworld".getBytes(),-1);//����3��ָ��Ҫ���ǵİ汾��-1��ʾ�������еİ汾
		System.out.println("�����޸ĳɹ���");
	}
	
	
}
