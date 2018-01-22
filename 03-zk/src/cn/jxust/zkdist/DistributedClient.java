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
 * ��̬�����������߸�֪-�ͻ���
 */
public class DistributedClient {
	private ZooKeeper zk=null;
	private static final String connectString="111.230.243.106:2181";//����������master
	private static final int sessionTimeout=30000;
	private static final String paraentNode="/servers";

	//volatile�����壺��ÿ���̶߳�дͬһ��serverList�������ǿ���������ȥ�޸�
	private volatile List<String> serverList;
	/*
	 * ��ȡ����
	 */
	public void getConnection() throws IOException{
		zk=new ZooKeeper(connectString, sessionTimeout, new Watcher(){
			//������
			public void process(WatchedEvent event) {
				System.out.println("�¼����ͣ�"+event.getType()+"���¼������ڣ�"+event.getPath());
				try {
					//�������������ı�ʱ�����¸��·������б���Ϣ����ע�����
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
	 * ��ȡservers�������ڵ��µ��ӽڵ���Ϣ�����Ը��ڵ����
	 */
	public void getServerList() throws KeeperException, InterruptedException{
		List<String> children = zk.getChildren(paraentNode, true);//��ȡ�ӽڵ�
		//�ȴ���һ���ֲ���list�������������Ϣ
		List<String> servers=new ArrayList<String>();
		//��ȡÿ������������Ϣ
		for(String child:children){
			byte[] data = zk.getData(paraentNode+"/"+child, false, null);//���ö��ӽڵ���У����ݣ�������ֻ��������ڵ㣨servers�����ɸ�֪�ӽڵ��Ƿ�仯�������ؾ���ʱ���Լ����ӽڵ�����ݱ仯����Ϊ���пͻ������ӵ�������ʱ��������������ݻᷢ���ı�
			servers.add(new String(data));
		}
		//��servers����serverList����ҵ���߳�ʹ��
		serverList=servers;//һ���Ը�ֵ����ò�Ҫһ��һ�µĸ�
		
		System.out.println(serverList);
	}
	
	/*
	 * ҵ���߳�
	 */
	public void handleBussiness() throws InterruptedException{
		System.out.println("client  start working.....");
		//����zk�󣬻Ὺ��һ���̣߳���ʱ���Բ�дThread.sleep��������ʹmain���������߳��˳��ˣ�����Ҳ��һֱ���С�����������������û�취����������zk������س�����Ϊ���ػ��̣߳�ֻҪ���߳̽���������߳̾ͻ���Źرգ���������ҪThread.sleep.
		/*
		 * Thread t=new Thread( Runnable );
			t.setDaemon(true);
			t.start();
		 */
		Thread.sleep(Long.MAX_VALUE);//�ø÷��񲻶ϵ�
	}
	
	/*��ɿ�ִ��runable jar Ҫ��main����
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
		//��ȡzk����
		DistributedClient client=new DistributedClient();
		client.getConnection();
		
		//��ȡservers�ӽڵ���Ϣ�����������л�ȡ��������Ϣ�б�
		client.getServerList();
		
		//ҵ���߳�����
		client.handleBussiness();
	}
	
	
	
}
