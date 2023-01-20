package browser.listener_handler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.event.NavigationAdapter;


public class BaseTitleListener extends NavigationAdapter {
	private CTabItem tabItem;

	public BaseTitleListener(CTabItem tabItem) {
		super();
		this.tabItem = tabItem;
	}
	
	@Override
	public void mainDocumentCompleted(Browser arg0, String arg1) {
		if(arg0.isDisposed())
			return;
		arg0.setZoomLevel(150);
		String title = arg0.getTitle();
		
		Display.getDefault().asyncExec(new Runnable() {	
			@Override
			public void run() {
				tabItem.setText(title);
				CTabFolder cf = tabItem.getParent();
				cf.setSelection(cf.indexOf(tabItem));
			}
		});
	}
}
