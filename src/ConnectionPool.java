import java.sql.Connection;
import java.util.LinkedList;

public class ConnectionPool {
	private LinkedList<Connection> pool = new LinkedList<>();

	public ConnectionPool(int initialSize) {
		if (initialSize > 0) {
			for (int i = 0; i < initialSize; i++) {
				pool.addLast(ConnectionDriver.createConnection());
			}
		}
	}

	public void releaseConnection(Connection connection) {
		if (connection != null) {
			synchronized (pool) {
				pool.addLast(connection);
				// 唤醒等待在pool上的消费者
				pool.notifyAll();
			}
		}
	}

	// 在mills毫秒时间内无法获取连接 返回null
	public Connection fetchConnection(long mills) throws InterruptedException {
		synchronized (pool) {
			// 完全超时 一直等待到获取到连接为止
			if (mills <= 0) {
				while (pool.isEmpty()) {
					pool.wait();	
				}
				return pool.removeFirst();
			} else {
				long future = System.currentTimeMillis()+mills;
				long remain = mills;
				while(pool.isEmpty() && remain>0){
					pool.wait(remain);
					remain = future-System.currentTimeMillis();
				}
				
				Connection result = null;
				if(!pool.isEmpty()){
					result = pool.removeFirst();
				}
				
				return result;
			}
		}
	}
}
