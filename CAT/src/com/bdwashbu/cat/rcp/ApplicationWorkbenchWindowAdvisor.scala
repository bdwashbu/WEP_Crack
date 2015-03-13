package com.bdwashbu.cat.rcp

import org.eclipse.swt.graphics.Point
import org.eclipse.ui.application.ActionBarAdvisor
import org.eclipse.ui.application.IActionBarConfigurer
import org.eclipse.ui.application.IWorkbenchWindowConfigurer
import org.eclipse.ui.application.WorkbenchWindowAdvisor
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.IWorkbenchPreferenceConstants

class ApplicationWorkbenchWindowAdvisor(val configurer: IWorkbenchWindowConfigurer) extends WorkbenchWindowAdvisor(configurer) {

	override def createActionBarAdvisor(
			configurer: IActionBarConfigurer): ActionBarAdvisor = {
		return new ApplicationActionBarAdvisor(configurer)
	}

	override def preWindowOpen() = {
		val configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1200, 700))
		
		PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
		
		configurer.setShowCoolBar(false)
		configurer.setShowStatusLine(false)
		configurer.setTitle("Crack Analysis Tool")
	}
}
