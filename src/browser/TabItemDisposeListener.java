package browser;

import java.awt.Frame;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.teamdev.jexplorer.Browser;

import threadpool.ThreadPool;

public class TabItemDisposeListener implements DisposeListener {

	public TabItemDisposeListener() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		CTabItem tab = (CTabItem) e.widget;
		Composite com = (Composite) tab.getControl();
		Frame f = SWT_AWT.getFrame(com);
		Browser b = (Browser) f.getComponents()[0];
		ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
			@Override
			public void run() {
				b.stop();
				b.navigate("about:blank");
				b.dispose();
			}
		});
	}

}
