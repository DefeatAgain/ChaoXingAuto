package browser.listener_handler;

import java.awt.Dimension;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.teamdev.jexplorer.Browser;
import com.teamdev.jexplorer.EventsHandler;

import browser.studyHandler.VideoHandler;
import window.dialog.ErrorSolveDialog;

public class LoadHandler implements EventsHandler {
	private Shell shell;
	
	private Logger a = Logger.getLogger(LoadHandler.class);

	public LoadHandler(Shell shell) {

		this.shell = shell;
	}

	@Override
	public boolean beforeFileDownload() {
		ErrorSolveDialog.open(shell, "Warning", "不可下载其它内容，请使用文件功能", false, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				Menu menu =shell.getMenuBar();
				MenuItem[] mis = menu.getItems();
				for (int i = 0; i < mis.length; i++) {
					if(mis[i].getText().contains("文"))
						mis[i].setSelection(true);
				}
				return null;
			}
		});
		return true;
	}

	@Override
	public boolean beforeNavigate(Browser arg0, String arg1, String arg2, byte[] arg3, String arg4) {
		a.debug(arg1);
		a.debug(arg2);
		a.debug(new String(arg3));
		a.debug(arg4);
		if(arg1.contains("richvideo")&&VideoHandler.video_topic) {
			ErrorSolveDialog.open(shell, "videoTopic", arg1, false, null);
			return true;
		}
		return false;
	}

	@Override
	public Dimension clientAreaSizeRequested(Dimension arg0) {
		return arg0;
	}

	@Override
	public boolean navigationErrorOccured(Browser arg0, String arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean windowClosing(boolean arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
