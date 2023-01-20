package browser;

import java.awt.Frame;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.BrowserProperties;

public class BrowserUtils {
	/**
	 * 通过选项卡获取浏览器实例
	 * @param item 选项卡
	 * @return browser 当前选项卡的浏览器实例
	 */
	public static Browser getBrwoser(CTabItem item) {
		Composite com = (Composite) item.getControl();
		Frame f = SWT_AWT.getFrame(com);
		if (f == null)
			return null;
		return (Browser) f.getComponents()[0];
	}

	/**
	 * 通过浏览器实例获取选项卡
	 * @param browser 浏览器实例
	 * @return item 选项
	 * @see getBrwoser
	 */
	public static CTabItem getCTabItem(Browser browser) {
		Frame f = (Frame) browser.getParent();
		Shell[] allShell = Display.getDefault().getShells();
		Shell shell = null;
		for (int i = 0; i < allShell.length; i++) {
			if(allShell[i].getText().equals("超星")) {
				shell=allShell[i];
				break;
			}
		}
		if(shell!=null) {
			CTabFolder tabFolder = (CTabFolder) shell.getTabList()[0];
			CTabItem[] items = tabFolder.getItems();
			for (int i = 0; i < items.length; i++) {
				Composite com = (Composite) items[i].getControl();
				Frame frame = SWT_AWT.getFrame(com);
				if(frame.equals(f)) {
					return items[i];
				}
			}
		}
		return null;
	}

	/**
	 * 转换cookie至浏览器
	 * @param target 目标浏览器
	 * @param origin httpclient的cookiestore
	 * @param url 所要设置的URL，一般设置为http(https)://+domain
	 * @see convertCookieToHttpClient
	 */
	public static void convertCookieToBrowser(Browser target, BasicCookieStore origin, String url) {
		List<org.apache.http.cookie.Cookie> list = origin.getCookies();
		int index1 = url.indexOf("//");
		int index2 = url.indexOf("/",index1+2);
		String uri = null;
		if(index2<0)
			uri = url;
		else
			uri = url.substring(0, index2+1);
		for (org.apache.http.cookie.Cookie c : list) {
			target.setCookie(uri, setCookie(c));
		}
	}

	private static com.teamdev.jexplorer.Cookie setCookie(Cookie c) {
		com.teamdev.jexplorer.Cookie cookie = new com.teamdev.jexplorer.Cookie();
		cookie.setDomain(c.getDomain());
		cookie.setName(c.getName());
		cookie.setValue(c.getValue());
		cookie.setSecure(c.isSecure());
		return cookie;
	}

	/**
	 * 转换cookie至httlclient
	 * @param origin 原浏览器
	 * @param bcs	httlclient的cookiestore
	 * @param url	原浏览器取得cookie的url
	 * @see convertCookieToBrowser
	 */
	public static void convertCookieToHttpClient(Browser origin, BasicCookieStore bcs, String url) {
		List<com.teamdev.jexplorer.Cookie> list = origin.getCookies(url);
		for (com.teamdev.jexplorer.Cookie c : list) {
			bcs.addCookie(_setCookie(c));
		}
	}

	private static Cookie _setCookie(com.teamdev.jexplorer.Cookie c) {
		BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
		int index1 = c.getDomain().indexOf("//") + 2;
		String domain = c.getDomain().substring(index1, c.getDomain().length() - 1);
		cookie.setDomain(domain);
		cookie.setPath(c.getPath());
		cookie.setSecure(c.isSecure());
		return cookie;
	}

	/**
	 * 转换浏览器实例的cookie之新的浏览器实例
	 * 不应使用BrowserContex，使用BrowserContex会导致无法关闭网页
	 * @param origin 原浏览器
	 * @param target 目标浏览器
	 */
	public static void transportCookieBrowserToBrowser(Browser origin, Browser target) {
		String url = origin.getLocationURL();
		int index1 = url.indexOf("//") + 2;
		int index2 = url.indexOf("/", index1);
		String finalUrl = url.substring(0, index2 + 1);
		List<com.teamdev.jexplorer.Cookie> list = origin.getCookies(finalUrl);
		for (com.teamdev.jexplorer.Cookie c : list) {
			target.setCookie(finalUrl, b_setCookie(c));
		}
	}

	private static com.teamdev.jexplorer.Cookie b_setCookie(com.teamdev.jexplorer.Cookie c) {
		com.teamdev.jexplorer.Cookie cookie = new com.teamdev.jexplorer.Cookie();
		// 原domain错误
		int index1 = c.getDomain().indexOf("//") + 2;
		String domain = c.getDomain().substring(index1, c.getDomain().length() - 1);
		cookie.setDomain(domain);
		cookie.setName(c.getName());
		cookie.setValue(c.getValue());
		cookie.setPath(c.getPath());
		cookie.setSecure(c.isSecure());
		return cookie;
	}

	/**
	 * unicode转中文
	 * @param unicode
	 * @return 所翻译的出的String
	 */
	public static String unicode2String(String dataStr) {
		try {
			final StringBuilder buffer = new StringBuilder(dataStr == null ? "" : dataStr);
			if (StringUtils.isNotBlank(dataStr) && dataStr.contains("\\u")) {
				buffer.delete(0, buffer.length());
				int start = 0;
				int end = 0;
				while (start > -1) {
					end = dataStr.indexOf("\\u", start + 2);
					String a = "#";// 如果夹着非unicode编码的字符串，存放在这
					String charStr = "";
					if (end == -1) {
						if (dataStr.substring(start + 2, dataStr.length()).length() > 4) {
							charStr = dataStr.substring(start + 2, start + 6);
							a = dataStr.substring(start + 6, dataStr.length());
						} else {
							charStr = dataStr.substring(start + 2, dataStr.length());
						}
					} else {
						charStr = dataStr.substring(start + 2, end);
					}
					char letter = (char) Integer.parseInt(charStr.trim(), 16); // 16进制parse整形字符串。
					buffer.append(new Character(letter).toString());
					if (StringUtils.isNotBlank(a)) {
						buffer.append(a);
					}
					start = end;
				}
			}
			return buffer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void setProperties(Browser baseBrowser) {
		BrowserProperties bp = new BrowserProperties();
		bp.setAllowJavaApplets(false);
		baseBrowser.setBrowserProperties(bp);
	}
	
	
}
