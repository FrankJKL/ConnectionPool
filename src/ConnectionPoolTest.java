import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolTest {
	// 初始线程池10个连接
	static ConnectionPool pool = new ConnectionPool(10);
	// 保证所有ConnectionRunner能够同时开始
	static CountDownLatch start = new CountDownLatch(1);
	static CountDownLatch end;

	public static void main(String[] args) throws InterruptedException {
		// 线程数量
		int threadCount = 20;
		end = new CountDownLatch(threadCount);

		int count = 20;// 每个ConnectionRunner线程获取连接获取20次
		// 分别用来统计获取到连接的次数 和 没有获取到连接的次数
		AtomicInteger got = new AtomicInteger();
		AtomicInteger notGot = new AtomicInteger();

		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(new ConnectionRunner(count, got, notGot), "ConnectionRunnerThread");
			thread.start();
		}
		start.countDown();// 10个线程开始运行
		end.await();// 等待10个线程运行完毕
		System.out.println("total invoke:" + threadCount * count);// 尝试获取数据库连接的总次数
		System.out.println("got connection:" + got);// 10个线程并发运行下 获取到连接的次数
		System.out.println("notGot connection:" + notGot);// 10个线程并发运行下 没有获取到连接的次数
	}

	static class ConnectionRunner implements Runnable {
		int count;
		AtomicInteger got;
		AtomicInteger notGot;

		public ConnectionRunner(int count, AtomicInteger got, AtomicInteger notGot) {
			super();
			this.count = count;
			this.got = got;
			this.notGot = notGot;
		}

		@Override
		public void run() {
			try {
				start.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 重复获取连接池中 的 数据库连接
			while (count > 0) {
				try {
					Connection connection = pool.fetchConnection(1000);
					if (connection != null) {
						try {
							connection.createStatement();
							connection.commit();
						} finally {
							pool.releaseConnection(connection);
							got.incrementAndGet();
						}

					} else {
						notGot.incrementAndGet();
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					count--;
				}
			}
			end.countDown();
		}

	}
}
