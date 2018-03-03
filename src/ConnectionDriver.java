import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;

public class ConnectionDriver {
	static class ConnectionHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 当代理对象执行的是commit方法时，休眠100毫秒。模拟连接的提交过程
			if (method.getName() == "commit") {
				TimeUnit.MILLISECONDS.sleep(100);
			}
			return null;
		}

	}

	// 创建一个connection的代理 在commit时休眠100毫秒
	public static Connection createConnection() {
		return (Connection) Proxy.newProxyInstance(ConnectionDriver.class.getClassLoader(),
				new Class[] { Connection.class }, new ConnectionHandler());
	}

}
