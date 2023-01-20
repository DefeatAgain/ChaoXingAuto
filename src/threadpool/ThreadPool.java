package threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
	public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

	public static void shutdownAndAwaitTermination() {
		EXECUTOR_SERVICE.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!EXECUTOR_SERVICE.awaitTermination(30, TimeUnit.SECONDS)) {
				EXECUTOR_SERVICE.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!EXECUTOR_SERVICE.awaitTermination(30, TimeUnit.SECONDS)) 
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			EXECUTOR_SERVICE.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	public static void sleep(long mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
