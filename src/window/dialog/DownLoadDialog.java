package window.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamdev.jexplorer.Browser;

import browser.httpclients.VideoDownload;
import threadpool.ThreadPool;

public class DownLoadDialog extends Dialog {
	protected Object result;
	protected Shell shell;
	private Text text;
	private String mode;
	private String dir;
	private Browser origin;
	private int fid;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public DownLoadDialog(Shell parent, int style, Browser b, int fid) {
		super(parent, style);
		setText("下载");
		this.origin = b;
		this.fid = fid;
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(621, 304);
		shell.setText(getText());

		Group group = new Group(shell, SWT.NONE);

		group.setText("清晰度");
		group.setBounds(26, 84, 422, 49);

		Button btnRadioButton = new Button(group, SWT.RADIO);
		btnRadioButton.setToolTipText("current");
		btnRadioButton.setLocation(9, 22);
		btnRadioButton.setSize(98, 20);
		btnRadioButton.setText("目前所选");

		Button button = new Button(group, SWT.RADIO);
		button.setToolTipText("md");
		button.setLocation(113, 22);
		button.setSize(65, 20);
		button.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 9, SWT.NORMAL));
		button.setText("流畅");

		Button button_1 = new Button(group, SWT.RADIO);
		button_1.setToolTipText("sd");
		button_1.setLocation(184, 22);
		button_1.setSize(65, 20);
		button_1.setText("标清");

		Button button_2 = new Button(group, SWT.RADIO);
		button_2.setToolTipText("hd");
		button_2.setLocation(255, 20);
		button_2.setSize(65, 24);
		button_2.setText("高清");

		Button button_3 = new Button(group, SWT.RADIO);
		button_3.setToolTipText("shd");

		button_3.setLocation(326, 20);
		button_3.setSize(65, 24);
		button_3.setText("超高清");

		Button button_4 = new Button(shell, SWT.NONE);
		Label lblErrors = new Label(shell, SWT.NONE);
		text = new Text(shell, SWT.BORDER);
		ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
		VideoDownload vd = new VideoDownload(origin, fid, progressBar, lblErrors, shell);
		button_4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				lblErrors.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				lblErrors.setVisible(false);
				dir = text.getText();
				Control[] buttuons = group.getChildren();
				for (int i = 0; i < buttuons.length; i++) {
					Button radio = (Button) buttuons[i];
					if (radio.getSelection()) {
						mode = radio.getToolTipText();
					}
				}
				if (dir == null || dir.trim().equals("")) {
					lblErrors.setText("目录未选择");
					lblErrors.setVisible(true);
				} else if (mode == null) {
					lblErrors.setText("清晰度未选择");
					lblErrors.setVisible(true);
				} else {
					ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
						
						@Override
						public void run() {
							vd.download(mode,dir);
						}
					});			
				}
			}
		});
		button_4.setBounds(474, 104, 91, 30);
		button_4.setText("下载");

		text.setBounds(26, 38, 422, 26);

		Button button_5 = new Button(shell, SWT.NONE);
		button_5.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				DirectoryDialog folderdlg = new DirectoryDialog(shell, SWT.CLOSE);
				folderdlg.setText("目录选择");
				folderdlg.setFilterPath("SystemDrive");
				folderdlg.setMessage("请选择相应的文件夹");
				String selecteddir = folderdlg.open();
				if (selecteddir != null)
					text.setText(selecteddir);
			}
		});
		button_5.setBounds(474, 36, 91, 30);
		button_5.setText("选择目录");

		progressBar.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		progressBar.setBounds(28, 173, 541, 21);
		progressBar.setVisible(false);

		lblErrors.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblErrors.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 13, SWT.NORMAL));
		lblErrors.setBounds(26, 217, 236, 30);
		lblErrors.setText("Errors");
		lblErrors.setVisible(false);

		/*
		 * Label label = new Label(shell, SWT.NONE);
		 * label.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		 * label.setBounds(26, 12, 150, 20); label.setText("仅可下载本页面的视频");
		 */

	}
}
