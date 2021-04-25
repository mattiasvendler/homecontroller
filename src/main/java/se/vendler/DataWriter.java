package se.vendler;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class DataWriter extends Thread {
	
	private BlockingQueue<DataWrite> writeQueue;
	private Sql2o sql2o;
	
	protected class DataWrite {
		private float data;
		private long nodeId;
		
		public DataWrite(float humidity, long nodeId) {
			this.data = humidity;
			this.nodeId = nodeId;
		}
		
		public float getData() {
			return data;
		}
		
		public void setData(float data) {
			this.data = data;
		}
		
		public long getNodeId() {
			return nodeId;
		}
		
		public void setNodeId(int nodeId) {
			this.nodeId = nodeId;
		}
	}
	
	public DataWriter() {
		writeQueue = new ArrayBlockingQueue<>(100);
		
		sql2o = new Sql2o("jdbc:mysql://192.168.1.181:3306/node?useSSL=false", "homecenter", "homecenter");
		this.start();
	}
	
	public void addWrite(long nodeId, float data){
		writeQueue.add(new DataWrite(data, nodeId));
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				DataWrite dataWrite = writeQueue.take();
				Connection connection = sql2o.open();
				Query query = getQuery(dataWrite, connection);
				query.executeUpdate();
				connection.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected abstract Query getQuery(DataWrite humidityWrite, Connection open);
}
