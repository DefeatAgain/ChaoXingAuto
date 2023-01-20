package browser.listener_handler;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.BrowserContext;
import com.teamdev.jexplorer.PopupContainer;
import com.teamdev.jexplorer.PopupHandler;

import browser.BaseBrowser;
import browser.BrowserUtils;
import browser.TabItemDisposeListener;
import browser.studyHandler.VideoWebListener;

public class PopupHandlerImpl implements PopupHandler {
	private CTabFolder cTabFolder;

	public PopupHandlerImpl(CTabFolder cTabFolder) {
		super();
		this.cTabFolder = cTabFolder;
	}

	@Override
	public PopupContainer handlePopup() {
		return new PopupContainer() {
			@Override
			public void insertBrowser(Browser _browser) {
				handle(_browser);
			}
		};
	}

	private void handle(Browser browser) {
		// 新建浏览器进程提高效率
		Browser browser0 = new Browser(new BrowserContext());
		BrowserUtils.transportCookieBrowserToBrowser(browser, browser0);
		BrowserUtils.setProperties(browser0);
		browser0.navigate(browser.getLocationURL(), "_self", "",
				"Host: mooc1-2.chaoxing.com\nReferer: http://mooc1-2.chaoxing.com/visit/courses");
		browser0.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
		BaseBrowser.browserList.add(browser0);

		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				CTabItem tabItem = new CTabItem(cTabFolder, SWT.CLOSE);
				tabItem.addDisposeListener(new TabItemDisposeListener());
				tabItem.setText("加载中。。。");
				// 监听
				browser0.addNavigationListener(new BaseTitleListener(tabItem));
				browser0.addNavigationListener(new URLListener(tabItem));
				browser0.setContextMenuProvider(new ContextMenuProviderImpl());
				// 挂课
				browser0.addNavigationListener(new VideoWebListener(tabItem));
				browser0.setEventsHandler(new LoadHandler(cTabFolder.getShell()));
				browser0.setDialogHandler(new JavasciptHandler(browser0));
				// 创建
				Composite composite = new Composite(cTabFolder, SWT.EMBEDDED | SWT.NO_BACKGROUND);
				tabItem.setControl(composite);
				tabItem.addDisposeListener(new TabItemDisposeListener());
				Frame frame = SWT_AWT.new_Frame(composite);
				frame.add(browser0);
			}
		});

		browser0.setPopupHandler(this);

		// 关闭子浏览器
		browser.stop();
		browser.navigate("about:blank");
		browser.dispose();
	}
}
