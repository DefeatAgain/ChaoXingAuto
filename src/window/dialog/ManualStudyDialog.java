package window.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.teamdev.jexplorer.Browser;

import browser.studyHandler.ManualStudy;
import threadpool.ThreadPool;
import org.eclipse.swt.widgets.Text;

public class ManualStudyDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private ManualStudy ms;
	private Text txtn;
	private Text text;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ManualStudyDialog(Shell parent, int style, Browser origin) {
		super(parent, style);
		setText("手动挂机");
		ms = new ManualStudy(origin,this);
	}

	/**
	 * Open the dialog.
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
		shell = new Shell(getParent(), SWT.CLOSE | SWT.TITLE);
		ms.dialog_shell=shell;
		shell.setSize(408, 322);
		shell.setText(getText());
		
		Group group = new Group(shell, SWT.NONE);
		group.setText("视频");
		group.setBounds(39, 20, 300, 115);
		
		Button btnNewButton = new Button(group, SWT.NONE);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
					@Override
					public void run() {
						ms.doVideo();
					}
				});
			}
		});
		btnNewButton.setLocation(0, 20);
		btnNewButton.setSize(300, 42);
		btnNewButton.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 16, SWT.NORMAL));
		btnNewButton.setText("开始视频");
		
		Button btnNewButton_1 = new Button(group, SWT.NONE);
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ms.cancelVideoTimer();
			}
		});
		btnNewButton_1.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 16, SWT.NORMAL));
		btnNewButton_1.setBounds(0, 75, 300, 40);
		btnNewButton_1.setText("取消挂机");
		
		Group group_1 = new Group(shell, SWT.NONE);
		group_1.setText("答题");
		group_1.setBounds(39, 196, 300, 69);
		
		Button button = new Button(group_1, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ThreadPool.EXECUTOR_SERVICE.submit(new Runnable() {
					
					@Override
					public void run() {
						ms.queryQues();
					}
				});
				
			}
		});
		button.setText("自动答题");
		button.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 16, SWT.NORMAL));
		button.setBounds(0, 19, 300, 40);
		
		txtn = new Text(shell, SWT.BORDER | SWT.CENTER);
		txtn.setEditable(false);
		txtn.setText("**直接删除答题框**");
		txtn.setBounds(39, 141, 300, 26);
		
		text = new Text(shell, SWT.BORDER | SWT.CENTER);
		text.setEditable(false);
		text.setText("**不会随机答案**");
		text.setBounds(39, 173, 300, 26);

	}
}
