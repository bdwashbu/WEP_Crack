package com.bdwashbu.cat.rcp

import org.eclipse.ui.application.IWorkbenchWindowConfigurer
import org.eclipse.ui.application.WorkbenchAdvisor
import org.eclipse.ui.application.WorkbenchWindowAdvisor

class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	override def createWorkbenchWindowAdvisor(configurer: IWorkbenchWindowConfigurer): WorkbenchWindowAdvisor = {
	  new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	override def getInitialWindowPerspectiveId(): String = {
		return ApplicationWorkbenchAdvisor.PERSPECTIVE_ID
	}

}

object ApplicationWorkbenchAdvisor {
  val PERSPECTIVE_ID = "com.bdwashbu.CAT.perspective"
}
