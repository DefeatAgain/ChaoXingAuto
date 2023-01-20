package browser.listener_handler;

import org.apache.log4j.Logger;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.DialogHandler;

import threadpool.ThreadPool;

public class JavasciptHandler implements DialogHandler {
	private Browser b;
	
	private Logger a = Logger.getLogger(JavasciptHandler.class);
	public JavasciptHandler(Browser b) {
		this.b=b;
	}

	@Override
	public int showDialog(String arg0, String arg1, int arg2) {
		a.info(arg0+"::"+arg1);
		ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
			
			@Override
			public void run() {
				ThreadPool.sleep(2000);
				b.refresh();
			}
		});
		return DialogHandler.IDOK;
	}

}
