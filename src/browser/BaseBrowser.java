package browser;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.BasicCookieStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

import com.teamdev.jexplorer.Browser;

import browser.listener_handler.BaseTitleListener;
import browser.listener_handler.ContextMenuProviderImpl;
import browser.listener_handler.JavasciptHandler;
import browser.listener_handler.LoadHandler;
import browser.listener_handler.PopupHandlerImpl;
import browser.listener_handler.URLListener;

public class BaseBrowser extends Browser {

	private static final long serialVersionUID = 1L;
	private String loadURL;
	private String postData;
	private String headers;

//	private BasicCookieStore http_bcs;
	private Composite composite;
	private CTabFolder cTabFolder;

	public static List<Browser> browserList = new ArrayList<>();	

	public BaseBrowser(String loadURL, String postData, String headers, BasicCookieStore http_bcs,
			CTabFolder cTabFolder) {
		super();
		this.loadURL = loadURL;
//		this.http_bcs = http_bcs;
		this.cTabFolder = cTabFolder;
		this.postData = postData;
		this.headers = headers;
	}

	public void open() {
//		BrowserUtils.convertCookieToBrowser(this, http_bcs, loadURL);
		
		browserList.add(this);
		CTabItem tabItem = new CTabItem(cTabFolder, SWT.CLOSE);
		composite = new Composite(cTabFolder, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		Frame frame = SWT_AWT.new_Frame(composite);
		tabItem.setControl(composite);
		frame.add(this);
		tabItem.setText("加载中。。。");
		tabItem.addDisposeListener(new TabItemDisposeListener());
		
		this.setZoomLevel(150);
		this.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
		this.setContextMenuProvider(new ContextMenuProviderImpl());
		this.setEventsHandler(new LoadHandler(cTabFolder.getShell()));
		this.addNavigationListener(new BaseTitleListener(tabItem));
//		this.addNavigationListener(new URLListener(tabItem));
		this.setPopupHandler(new PopupHandlerImpl(cTabFolder));
		this.setDialogHandler(new JavasciptHandler(this));
		BrowserUtils.setProperties(this);
		this.navigate(loadURL, "_top", postData, headers);
		
	}

}
