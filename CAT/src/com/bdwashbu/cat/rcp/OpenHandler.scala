package com.bdwashbu.cat.rcp

import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import org.eclipse.ui.PlatformUI
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.SWT
import org.eclipse.core.commands.IHandler
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.FileLocator
import java.io.File
import com.bdwashbu.cat.model.CAPLoader
import com.bdwashbu.cat.projectview.CATView
import com.bdwashbu.cat.projectview.CATLabelProvider

class OpenHandler extends AbstractHandler with IHandler {

  override def execute(event: ExecutionEvent): Object = {
    
    val shell = PlatformUI.getWorkbench().
                    getActiveWorkbenchWindow().getShell();

    val dialog = new FileDialog(shell, SWT.OPEN);
    dialog.setFilterExtensions(Array[String]("*.*", "*.cap", "*.ncf"));
    dialog.setFilterNames(Array[String]("All Files", "Capture File", "CommView File"));
    val fileSelected = dialog.open();

    if (fileSelected != null) {
    
	    val page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    val packetview = page.showView("com.bdwashbu.packetview").asInstanceOf[CATView];
	    
//	    val bundle = Platform.getBundle("com.bdwashbu.CAT")
//	    val url = bundle.getEntry(fileSelected)
//	    val fileUrl = FileLocator.toFileURL(url)
//	    val file = new File(FileLocator.resolve(fileUrl).toURI())
	
		val input = CAPLoader.load(new File(fileSelected))
		
	    packetview.viewer.getLabelProvider().asInstanceOf[CATLabelProvider].startTime = input.head.header.time 
	    packetview.viewer.setInput(input)
	    packetview.viewer.setItemCount(input.length); 
	    packetview.viewer.refresh()
    }
    
    null;
  }
}