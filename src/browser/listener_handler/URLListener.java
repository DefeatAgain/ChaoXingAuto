package browser.listener_handler;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.event.NavigationAdapter;

public class URLListener extends NavigationAdapter {
	private CTabItem item;
	
	private Logger a = Logger.getLogger(URLListener.class);
	public URLListener(CTabItem item) {
		this.item = item;
	}

	@Override
	public void mainDocumentCompleted(Browser arg0, String arg1) {
		if (arg1.contains("passport2")) {
			arg0.stop();
			arg0.navigate("about:blank");
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					item.dispose();
					a.error("Cookie设置失败");
//					ErrorSolve.open(item.getParent().getShell(), "登陆失败", "Cookie设置失败", false,null);
				}
			});
		}

	}

	@Override
	public void frameDocumentCompleted(Browser arg0, String arg1) {
		if (arg1.contains("passport2")) {
			arg0.stop();
			arg0.navigate("about:blank");
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					item.dispose();
					a.error("Cookie设置失败");
//					ErrorSolve.open(item.getParent().getShell(), "登陆失败", "Cookie设置失败", false,null);
				}
			});
		}
	}
}
