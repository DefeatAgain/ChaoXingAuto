package window;

import java.awt.Frame;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.logging.Level;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.BrowserFeatures;
import com.teamdev.jexplorer.BrowserMode;
import com.teamdev.jexplorer.InternetCacheEntry;
import com.teamdev.jexplorer.LoggerProvider;
import com.teamdev.jexplorer.ae;

import browser.BaseBrowser;
import browser.BrowserUtils;
import browser.studyHandler.ManualStudy;
import browser.studyHandler.VideoHandler;
import browser.studyHandler.WorkHandler;
import threadpool.ThreadPool;
import window.dialog.DownLoadDialog;
import window.dialog.ErrorSolveDialog;
import window.dialog.ManualStudyDialog;
import window.dialog.WarningSolveDialog;

public class Main {
	private Shell shell = new Shell();
	private Text text;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	private Text text_4;

	// 其他类
//	private FirstReq firstReq = new FirstReq(shell);
	private int cityId = 3;
	private Object[] school_objs;
	private String[] strs_sc;
	private int fid;
//	private User user;
	private static Logger a = Logger.getLogger(Main.class);
	
	{InitJExpolre();}
	static {
		BasicConfigurator.configure();
	}
	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Main window = new Main();
			window.open();
		} catch (Exception e) {
			a.error(e.getMessage(), e);
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		File code = new File("./code.jpg");
		if (code.exists())
			code.delete();
		
		Display display = Display.getDefault();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				try {
					clearCache();
					shell.dispose();
					ThreadPool.shutdownAndAwaitTermination();
				} finally {
					System.exit(0);
				}
			}
		});
		setLocation(display);
		shell.setSize(1086, 651);
		shell.setText("超星");
		shell.setImage(new Image(display, new ImageData(this.getClass().getResourceAsStream("logo.png"))));
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));

		CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
//		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		BaseBrowser bb = new BaseBrowser("https://passport2.chaoxing.com/login?refer=http://i.mooc.chaoxing.com"
				, "", null, null, tabFolder);
		bb.open();
		/*tabItem.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 9, SWT.NORMAL));
		tabItem.setText("登陆界面");

		Composite composite = new Composite(tabFolder, SWT.NONE); 
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabItem.setControl(composite);

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		lblNewLabel.setFont(SWTResourceManager.getFont("新宋体", 16, SWT.BOLD));
		lblNewLabel.setBounds(22, 25, 98, 27);
		lblNewLabel.setText("学  校:");

		text = new Text(composite, SWT.BORDER);
		text.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 9, SWT.BOLD));
		text.setEditable(false);
		text.setEnabled(false);
		text.setBounds(145, 29, 255, 27);

		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		lblNewLabel_1.setFont(SWTResourceManager.getFont("新宋体", 16, SWT.BOLD));
		lblNewLabel_1.setBounds(22, 74, 98, 27);
		lblNewLabel_1.setText("用户名:");

		text_1 = new Text(composite, SWT.BORDER);
		text_1.setBounds(145, 74, 255, 27);

		Label label = new Label(composite, SWT.NONE);
		label.setText("密  码:");
		label.setFont(SWTResourceManager.getFont("新宋体", 16, SWT.BOLD));
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		label.setBounds(22, 120, 98, 27);

		text_2 = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		text_2.setBounds(145, 120, 255, 27);

		Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText("验证码:");
		label_1.setFont(SWTResourceManager.getFont("新宋体", 16, SWT.BOLD));
		label_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		label_1.setBounds(22, 168, 98, 27);

		text_3 = new Text(composite, SWT.BORDER);
		text_3.setBounds(145, 168, 113, 27);

		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		// 换验证码
		lblNewLabel_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				a.info("更换验证码");
				firstReq.getPara_SWT(true);
				lblNewLabel_2.setImage(new Image(display, new ImageData("./code.jpg")));
			}
		});
		lblNewLabel_2.setText("numcode");
		lblNewLabel_2.setBounds(274, 155, 113, 40);

		firstReq.getPara_SWT(false);
		lblNewLabel_2.setImage(new Image(display, new ImageData("./code.jpg")));

		Label error = new Label(composite, SWT.NONE);
		error.setVisible(false);
		error.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		error.setFont(SWTResourceManager.getFont("新宋体", 13, SWT.BOLD));
		error.setBounds(22, 211, 236, 28);
		
		Button button_1 = new Button(composite, SWT.CHECK);
		button_1.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 10, SWT.NORMAL));
		button_1.setBounds(279, 211, 126, 22);
		button_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		button_1.setText("记住登陆信息");

		//读取用户信息
		File f = new File("./user");
		if(f.exists()) {
			user=BrowserUtils.loadUserInfo();
			button_1.setSelection(true);
			text.setText(user.getSchool());
			text_1.setText(user.getUsername());
			text_2.setTextChars(user.getPassword());
			fid=user.getFid();
		}
		Button button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				button.setEnabled(false);
				String school = text.getText();
				String uname = text_1.getText();
				String pwd = new String(text_2.getTextChars());
				String vercode = text_3.getText();
				//判断信息是否有误
				if(school==null||school.trim().equals("")||uname==null||uname.trim().equals("")||
						pwd==null||pwd.trim().equals("")||vercode==null||vercode.trim().equals("")) {
					error.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					error.setText("还有信息没有填");
					error.setVisible(true);
					button.setEnabled(true);
					return;
				}
				//如过有信息则不获取 更改学校则重新获取
				if((user!=null&&!user.getSchool().equals(school))||user==null) {
					int[] fids = (int[]) school_objs[0];
					strs_sc = (String[]) school_objs[1];
					int index = Arrays.asList(strs_sc).indexOf(school);
					fid = fids[index];
				}
				//存储信息
				if(button_1.getSelection()) {
					User u = new User(text_1.getText(), text_2.getTextChars(), text.getText(), fid);
					BrowserUtils.storeUserInfo(u);
				}
				a.info(school+"..."+fid+"..."+uname+"..."+pwd+"..."+vercode);
				login(school, fid, uname, pwd, vercode);
			}

			private void login(String school, int fid, String uname, String pwd, String vercode) {
				error.setVisible(false);
				Login login = new Login(firstReq.getCookieStore());
				String str = login.login( fid, school, uname, pwd, vercode, shell);
				if (!str.contains("http")) {
					error.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					error.setText(str);
					error.setVisible(true);
					button.setEnabled(true);
					a.info(str);
					//换验证码
					firstReq.getPara_SWT(true);
					lblNewLabel_2.setImage(new Image(display, new ImageData("./code.jpg")));
				} else {
					String headers = "Origin: http://passport2.chaoxing.com ";
					BaseBrowser bb = new BaseBrowser(str, "", headers, login.getCookieStore(), tabFolder);
					bb.open();
				}
			}
		});
		button.setFont(SWTResourceManager.getFont("Courier New Baltic", 16, SWT.BOLD));
		button.setBounds(22, 245, 262, 40);
		button.setText("登  陆");

		// 列表资源获取
		Object[] city_objs = firstReq.getCity();// 初次获取城市
		school_objs = firstReq.getSchool(cityId);// 初次获取学校
		strs_sc = (String[]) school_objs[1];
		List list = new List(composite, SWT.BORDER | SWT.V_SCROLL);// 城市列表
		List list_1 = new List(composite, SWT.BORDER | SWT.V_SCROLL);// 学校列表
		list_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str = list_1.getItem(list_1.getSelectionIndex());
				text.setText(str);
			}
		});
		list.addSelectionListener(new SelectionAdapter() {
			@Overrid
			public void widgetSelected(SelectionEvent e) {
				int select_index = list.getSelectionIndex();// 31位城市id
				int[] indexs = (int[]) city_objs[1];
				if (select_index >= 0) {
					school_objs = firstReq.getSchool(indexs[select_index]);// 获取学校
					strs_sc = (String[]) school_objs[1];// 学校名称
					list_1.setItems(strs_sc);
				}
			}
		});
		list.setBounds(452, 56, 71, 172);
		list.setItems((String[]) city_objs[0]);// 添加城市(0)
		list.select(0);

		list_1.setBounds(552, 89, 210, 139);
		list_1.setItems(strs_sc);// 首次添加学校

		text_4 = new Text(composite, SWT.BORDER);
		text_4.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String str = text_4.getText();
				java.util.List<String> temp = new ArrayList<String>();
				for (int i = 0; i < strs_sc.length; i++) {
					if (strs_sc[i].contains(str))
						temp.add(strs_sc[i]);
				}
				String[] strs_sc_aim = temp.toArray(new String[temp.size()]);
				// 改变学列表
				list_1.setItems((strs_sc_aim));
			}
		});
		text_4.setFont(SWTResourceManager.getFont("楷体", 10, SWT.ITALIC));
		text_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		text_4.setText("搜索");
		text_4.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				text_4.setFont(SWTResourceManager.getFont("楷体", 10, SWT.ITALIC));
				text_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
				text_4.setText("搜索");
			}

			@Override
			public void focusGained(FocusEvent e) {
				text_4.setText("");
				text_4.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NONE));
				text_4.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
			}
		});
		text_4.setBounds(552, 57, 210, 26);

		Label label_2 = new Label(composite, SWT.NONE);
		label_2.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		label_2.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 10, SWT.BOLD));
		label_2.setBounds(452, 25, 76, 27);
		label_2.setText("学校选择:");

		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				SearchSchoolDialog sc = new SearchSchoolDialog(shell, SWT.NONE, text, firstReq);
				sc.open();
				school_objs = sc.result;
			}
		});
		lblNewLabel_3.setBackground(SWTResourceManager.getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		lblNewLabel_3.setForeground(SWTResourceManager.getColor(SWT.COLOR_LIST_SELECTION));
		lblNewLabel_3.setBounds(777, 59, 98, 22);
		lblNewLabel_3.setText("直接搜索全部");*/
		

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("文 件");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmNewItem = new MenuItem(menu_1, SWT.NONE);
		mntmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Composite com = (Composite) tabFolder.getSelection().getControl();
				Frame f = SWT_AWT.getFrame(com);
				Browser b = (Browser) f.getComponents()[0];
				String html = b.getContent(true);
				DirectoryDialog folderdlg = new DirectoryDialog(shell, SWT.CLOSE);
				folderdlg.setText("目录选择");
				folderdlg.setFilterPath("SystemDrive");
				folderdlg.setMessage("请选择相应的文件夹");
				String selecteddir = folderdlg.open();
				try {
					final PrintStream out = new PrintStream(selecteddir);

					ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
						@Override
						public void run() {
							out.print(html);
							out.close();
						}
					});
				} catch (Exception e1) {
					ErrorSolveDialog.open(shell, "Exception", e1.getMessage(), false, null);
					a.error(e1.getMessage(), e1);
				}

			}
		});
		mntmNewItem.setText("存储本页");

		MenuItem menuItem_2 = new MenuItem(menu_1, SWT.NONE);
		menuItem_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Composite com = (Composite) tabFolder.getSelection().getControl();
				Frame f = SWT_AWT.getFrame(com);
				Browser b = (Browser) f.getComponents()[0];
				b.print(true, b.getTitle(), b.getUserAgent());
			}
		});
		menuItem_2.setText("打印本页");

		MenuItem menuItem_1 = new MenuItem(menu_1, SWT.NONE);
		menuItem_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Composite com = (Composite) tabFolder.getSelection().getControl();
				Frame f = SWT_AWT.getFrame(com);
				if (f == null) {
					WarningSolveDialog.open(shell, "注意", "请在有视频的页面打开");
					return;
				}
				Browser b = (Browser) f.getComponents()[0];
				new DownLoadDialog(shell, SWT.CLOSE, b, fid).open();
			}
		});
		menuItem_1.setText("下载视频");

		MenuItem mntmNewSubmenu_1 = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu_1.setText("控制台");

		Menu menu_2 = new Menu(mntmNewSubmenu_1);
		mntmNewSubmenu_1.setMenu(menu_2);
		
		MenuItem menuItem_3 = new MenuItem(menu_2, SWT.CHECK);
		menuItem_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (menuItem_3.getSelection()) {
					WorkHandler.work_random=true;
					WarningSolveDialog.open(shell, "注意", "没搜到答案都会随机选择");
				}
				else {
					WorkHandler.work_random=false;
				}
			}
		});
		menuItem_3.setText("随机答案(不选择可能会结束挂机)");

		MenuItem menuItem = new MenuItem(menu_2, SWT.CHECK);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (menuItem.getSelection())
					VideoHandler.video_muted=true;
				else {
					VideoHandler.video_muted=false;
				}
			}
		});
		menuItem.setText("静音(JavaScript)(须在开始视频前选择)");
		
		MenuItem mntmNewItem_1 = new MenuItem(menu_2, SWT.CHECK);
		mntmNewItem_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (mntmNewItem_1.getSelection()) {
					VideoHandler.video_topic=true;
					WarningSolveDialog.open(shell, "注意", "可能仍然无法禁止题目的出现");
				}
				else {
					VideoHandler.video_topic=false;
					WarningSolveDialog.open(shell, "注意", "会尝试填充，如果失败可能会卡在那或者刷新页面");
				}
			}
		});
		mntmNewItem_1.setText("禁止视频题目(进入目录前选择)");
		
		MenuItem mntmjavascript = new MenuItem(menu_2, SWT.NONE);
		mntmjavascript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Browser browser = BrowserUtils.getBrwoser(tabFolder.getSelection());
				if(browser.getLocationURL().contains("_from_")) {
					ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
						@Override
						public void run() {
							browser.executeScript("let read = function () {let timer = undefined;let slide = function () {if (document.body.getScrollHeight() - document.body.getHeight() <= document.documentElement.scrollTop + 40) {let next = $('.ml40.nodeItem.r');if (next.length <= 0) {alert('看完啦~');} else {next[0].click();}clearTimeout(timer);return;}document.body.scrollTop = document.documentElement.scrollTop = document.documentElement.scrollTop + common.randNumber(60, 80);timer = setTimeout(slide, common.randNumber(15, 25) * 1000);}slide();}read();");
						}
					});
				}
			}
		});
		mntmjavascript.setText("自动阅读(JavaScript)(未测试)");
		
		MenuItem menuItem_6 = new MenuItem(menu_2, SWT.SEPARATOR);
		menuItem_6.setText("---");
		
		MenuItem menuItem_5 = new MenuItem(menu_2, SWT.CHECK);
		menuItem_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Browser b = BrowserUtils.getBrwoser(tabFolder.getSelection());
				if(menuItem_5.getSelection()) 
					ManualStudy.manual=false;
				else 
					ManualStudy.manual=true;
				b.refresh();
			}
		});
		menuItem_5.setText("开启自动挂课");
		
		MenuItem menuItem_4 = new MenuItem(menu, SWT.NONE);
		menuItem_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Browser b = BrowserUtils.getBrwoser(tabFolder.getSelection());
//				b.refresh();
				new ManualStudyDialog(shell, SWT.CLOSE, b).open();
			}
		});
		menuItem_4.setText("手动挂机");

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if(!display.readAndDispatch())
				display.sleep();
		}

	}

	protected void clearCache() {
		for(Browser b:BaseBrowser.browserList) {
			if(!b.isDisposed()) {
				b.clearCache(InternetCacheEntry.COOKIE_CACHE_ENTRY);
				b.clearCache(InternetCacheEntry.EDITED_CACHE_ENTRY);
				b.clearCache(InternetCacheEntry.NORMAL_CACHE_ENTRY);
				b.clearCache(InternetCacheEntry.SPARSE_CACHE_ENTRY);
				b.clearCache(InternetCacheEntry.STICKY_CACHE_ENTRY);
				b.clearCache(InternetCacheEntry.TRACK_OFFLINE_CACHE_ENTRY);
				b.clearCache(InternetCacheEntry.TRACK_ONLINE_CACHE_ENTRY);
				b.clearCache(InternetCacheEntry.URLHISTORY_CACHE_ENTRY);
			}
		}
	}

	private void InitJExpolre() {
		//许可证
		try {
			Field e = ae.class.getDeclaredField("e");
			e.setAccessible(true);
			Field f = ae.class.getDeclaredField("f");
			f.setAccessible(true);
			Field modifersField = Field.class.getDeclaredField("modifiers");
			modifersField.setAccessible(true);
			modifersField.setInt(e, e.getModifiers() & ~Modifier.FINAL);
			modifersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			e.set(null, new BigInteger("1"));
			f.set(null, new BigInteger("1"));
			modifersField.setAccessible(false);
		} catch (Exception e1) {
			ErrorSolveDialog.open(shell, "错误", "初始化浏览器失败", true, null);
			a.error(e1.getMessage(), e1);
		}
		String version = BrowserFeatures.getIEVersion();
		int ver = Integer.parseInt(version.substring(0, version.indexOf(".")));
		if(ver<11) {
			ErrorSolveDialog.open(shell, "错误", "未发现ie11", true, null);
			a.error("IE浏览器版本过低");
		}
		
		BrowserFeatures.enableBrowserMode(BrowserMode.IE11);
		BrowserFeatures.enableGPURendering();
		LoggerProvider.getBrowserLogger().setLevel(Level.WARNING);
		LoggerProvider.getIPCLogger().setLevel(Level.WARNING);
		LoggerProvider.getProcessLogger().setLevel(Level.WARNING);
	}

	private void setLocation(Display display) {
		Rectangle displayBounds = display.getPrimaryMonitor().getBounds(); 
        Rectangle shellBounds = shell.getBounds(); 
        int x = displayBounds.x + (displayBounds.width - shellBounds.width)/2; 
        int y = displayBounds.y + (displayBounds.height - shellBounds.height)/2; 
        shell.setLocation(x, y);
	}

	public static void deleteDir(File cache) {
		File[] files = cache.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				if (!files[i].delete())
					files[i].deleteOnExit();
			} else
				deleteDir(files[i]);
		}
		cache.delete();
	}
}
