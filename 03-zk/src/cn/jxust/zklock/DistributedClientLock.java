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
 * �ֲ�ʽ������-�ͻ��ˣ�zk��Ϊ��������
 */
public class DistributedClientLock {
	private ZooKeeper zk=null;
	private static final String connectString="111.230.243.106:2181";//����������master
	private static final int sessionTimeout=10000;
	private static final String groupNode="/locks";//���ڵ�
	private static final String subNode="sub";//�ӽڵ�
	private boolean haveLock=false;//�Ƿ��õ�������
	
	private volatile String thisPath;//��¼�Լ��������ӽڵ�·��
	
	/*
	 * ����zk
	 */
	public void connectZk() throws IOException, KeeperException, InterruptedException{
		zk=new ZooKeeper(connectString, sessionTimeout, new Watcher(){
			//������
			public void process(WatchedEvent event) {
				try {
					//�ж��¼����ͣ�ֻ�����ӽڵ�仯ʱ��
					if(event.getType()==EventType.NodeChildrenChanged&&event.getPath().equals(groupNode)){
						System.out.println("�¼����ͣ�"+event.getType()+"���¼������ڣ�"+event.getPath());
						//��ȡ�ӽڵ㣬���Ը��ڵ���м���
						List<String> childrenNodes = zk.getChildren(groupNode, true);
						//�Ƚϴ�С
						String thisNode = thisPath.substring((groupNode+"/").length());//��ȡ�Լ��ڵ������
						Collections.sort(childrenNodes);//�������ӽڵ��������Ĭ�ϴ�С����
						//���Լ��Ƿ��ǵ�һ����С��
						if(childrenNodes.indexOf(thisNode)==0){//����С��
							//ȥ������Դ�����ʺ�ɾ��������
							doSomething();
							//����ע��һ���µ���
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
		
		//�жϸ��ڵ��Ƿ����
		if(zk.exists(groupNode, false) == null){
			zk.create(groupNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		//1�����������ע��һ������zk sub0001 sub0002 sub0003
		thisPath=zk.create(groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		//waitһ�ᣬ���ڹ۲�
		Thread.sleep(new Random().nextInt(2000));
		//��zk����Ŀ¼�»�ȡ���е��ӽڵ㲢�������ڵ�
		List<String> childrenNodes = zk.getChildren(groupNode, true);
		if(childrenNodes.size()==1){//ֻ���Լ�һ̨��û��ȥ����Դ
			//ȥ������Դ�����ʺ�ɾ��������
			doSomething();
			//����ע��һ���µ���
			thisPath=zk.create(groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		}
		else{
			//�ȴ�
//			Thread.sleep(Long.MAX_VALUE);
		}
	}

	/*
	 * ������Դ
	 */
	private void doSomething() throws InterruptedException, KeeperException {
		try {
			System.out.println("���õ�������"+thisPath);
			Thread.sleep(2000);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("��Դ�ѷ��ʽ�����");
			//�ͷ���
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
