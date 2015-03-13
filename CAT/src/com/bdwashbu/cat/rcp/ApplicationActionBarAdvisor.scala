package com.bdwashbu.cat.rcp

import org.eclipse.ui.application.ActionBarAdvisor
import org.eclipse.ui.application.IActionBarConfigurer

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
class ApplicationActionBarAdvisor(configurer: IActionBarConfigurer) extends ActionBarAdvisor(configurer) {

}
