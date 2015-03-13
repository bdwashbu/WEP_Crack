package com.bdwashbu.cat.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

class Perspective extends IPerspectiveFactory {

	override def createInitialLayout(layout: IPageLayout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);		
	}
}
