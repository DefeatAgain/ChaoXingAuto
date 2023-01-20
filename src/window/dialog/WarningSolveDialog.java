package window.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class WarningSolveDialog {

	public static void open(Shell shell, String title, String warning) {
		//
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				MessageBox mg = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				mg.setText(title);
				mg.setMessage(warning);
				if(SWT.OK ==mg.open())
					return;
			}
		});
		//
	}

}
