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
 * ��̬�����������߸�֪-��������
 */
public class DistributedServer {
	private ZooKeeper zk=null;
	private static final String connectString="111.230.243.106:2181";//����������master
	private static final int sessionTimeout=30000;//��ʱʱ�� ��ͣ��һ��������ʱ����30��֮��Ż���Ϊ�������ҵ���
	private static final String paraentNode="/servers";
	
	
	/*
	 * ��ȡ����
	 */
	public void getConnection() throws IOException{
		zk=new ZooKeeper(connectString, sessionTimeout, new Watcher(){
			//������
			public void process(WatchedEvent event) {
				System.out.println("�¼����ͣ�"+event.getType()+"���¼������ڣ�"+event.getPath());
				try {
					zk.getChildren("/", true);//�ظ�����
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	
	/*
	 * ��zkע����Ϣ
	 */
	public void registerServer(String hostName) throws KeeperException, InterruptedException{
		//�жϸ��ڵ��Ƿ����
		Stat stat = zk.exists(paraentNode, false);
		if(stat==null){
//			System.out.println("���ڵ㲻���ڣ�");
			zk.create(paraentNode, "pararent".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		String node=zk.create(paraentNode+"/server", hostName.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//-e �������кű����ظ�
		System.out.println("��������"+hostName+"�����ˣ�  ������ʱ�ڵ㣺"+node);
	}
	
	/*
	 * ҵ���߼�
	 */
	public void handleBussiness(String hostName) throws InterruptedException{
		System.out.println(hostName+"  start working.....");
		//����zk�󣬻Ὺ��һ���̣߳���ʱ���Բ�дThread.sleep��������ʹmain���������߳��˳��ˣ�����Ҳ��һֱ���С�����������������û�취����������zk������س�����Ϊ���ػ��̣߳�ֻҪ���߳̽���������߳̾ͻ���Źرգ���������ҪThread.sleep
		/*
		 * Thread t=new Thread( Runnable );
			t.setDaemon(true);
			t.start();
		 */
		Thread.sleep(Long.MAX_VALUE);//�ø÷��񲻶ϵ�
	}
	
	/*
	 * ��ɿ�ִ��runable jar Ҫ��main����
	 */
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		DistributedServer server=new DistributedServer();
		server.getConnection();
		//ָ������ʱ����
		server.registerServer(args[0]);
		server.handleBussiness(args[0]);		
	}
	
	
	/*
	 * ��̨������
	 */
	@Test
	public void test1() throws IOException, InterruptedException, KeeperException{
		//��ȡzk����
		DistributedServer server=new DistributedServer();
		server.getConnection();

		//����zk����ע���������Ϣ
		server.registerServer("192.168.110.111");
		
		//����ҵ���߼�
		server.handleBussiness("192.168.110.111");			
	}
	
	@Test
	public void test2() throws IOException, InterruptedException, KeeperException{
		//��ȡzk����
		DistributedServer server=new DistributedServer();
		server.getConnection();

		//����zk����ע���������Ϣ
		server.registerServer("192.168.110.112");
		
		//����ҵ���߼�
		server.handleBussiness("192.168.110.112");			
	}
	
	@Test
	public void test3() throws IOException, InterruptedException, KeeperException{
		//��ȡzk����
		DistributedServer server=new DistributedServer();
		server.getConnection();

		//����zk����ע���������Ϣ
		server.registerServer("192.168.110.113");
		
		//����ҵ���߼�
		server.handleBussiness("192.168.110.113");			
	}
}
