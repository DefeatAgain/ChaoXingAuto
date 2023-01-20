package window.dialog;

import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class ErrorSolveDialog {

	public static void open(Shell shell, String title, String error, boolean exit, Callable<?> call) {
		//
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				MessageBox mg = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mg.setText(title);
				mg.setMessage(error);
				if (SWT.OK == mg.open() && exit)
					System.exit(1);
				else if (call != null) {
					try {
						call.call();
					} catch (Exception e) {
						System.exit(1);
					}
				}

			}
		});
		//
	}

}
