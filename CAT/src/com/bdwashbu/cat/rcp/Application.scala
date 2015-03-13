package com.bdwashbu.cat.rcp

import org.eclipse.equinox.app.IApplication
import org.eclipse.equinox.app.IApplicationContext
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
class Application extends IApplication {

	override def start(context: IApplicationContext): Object = {
		val display = PlatformUI.createDisplay();
		try {
			val returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART
			}
			return IApplication.EXIT_OK
		} finally {
			display.dispose()
		}
	}

	override def stop() = {
		if (!PlatformUI.isWorkbenchRunning()) {
			val workbench = PlatformUI.getWorkbench()
			val display = workbench.getDisplay()
			display.syncExec(new Runnable() {
				override def run() = {
					if (!display.isDisposed())
						workbench.close()
				}
			});
		}
	}
}
