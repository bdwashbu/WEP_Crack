package com.bdwashbu.cat.projectview;

import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.part.ViewPart
import org.eclipse.jface.viewers.IDoubleClickListener
import org.eclipse.swt.events.KeyAdapter
import org.eclipse.jface.viewers.DoubleClickEvent
import org.eclipse.swt.events.KeyEvent
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.TableViewerColumn
import org.eclipse.jface.viewers.ColumnLabelProvider
import org.eclipse.swt.widgets.TableColumn
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.FileLocator
import java.io.File
import com.bdwashbu.cat.model.CAPLoader
import org.eclipse.jface.viewers.ISelectionChangedListener
import org.eclipse.jface.viewers.SelectionChangedEvent
import org.eclipse.ui.ISelectionListener
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.ui.ISelectionListener
import org.eclipse.jface.viewers.ISelection
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.jface.viewers.ViewerComparator
import org.eclipse.jface.viewers.Viewer
import com.bdwashbu.cat.model._
import com.bdwashbu.cat.model.IEEE_80211.Wifi
import com.bdwashbu.cat.model.IEEE_80211.QoSMAC
import com.bdwashbu.cat.crack.fms.FMS
import java.nio.ByteBuffer
import scala.annotation.tailrec
import com.bdwashbu.cat.crack.fms.RC4

class CATView extends ViewPart {
  var viewer: TableViewer = _;

  override def createPartControl(parent: Composite) {
import IEEE_80211.DataPacketSubtype._
    
    viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL) {    
	    setUseHashlookup(true)
	    
	    val labelProvider = new CATLabelProvider(this)
	    
	    CATView.createTableColumn(viewer = this, 
	                              text = "#",
	                              tooltip = "Position this packet arrived in",
	                              sorter = labelProvider.getSorter(0),
	                              size = 50)
	                              
	    CATView.createTableColumn(viewer = this, 
	                              text = "Time",
	                              tooltip = "Time relative to the first packet received",
	                              sorter = labelProvider.getSorter(1),
	                              size = 70)
	                              
	    CATView.createTableColumn(viewer = this, 
	                              text = "Source",
	                              tooltip = "Column 1 tooltip",
	                              sorter = labelProvider.getSorter(2),
	                              size = 100)
	                              
	    CATView.createTableColumn(viewer = this, 
	                              text = "Destination",
	                              tooltip = "Column 1 tooltip",
	                              sorter = labelProvider.getSorter(3),
	                              size = 100)
	                              
	    CATView.createTableColumn(viewer = this, 
	                              text = "Size",
	                              tooltip = "Column 1 tooltip",
	                              sorter = labelProvider.getSorter(4),
	                              size = 70)
	                              
	    CATView.createTableColumn(viewer = this, 
	                              text = "Frame Type",
	                              tooltip = "Column 1 tooltip",
	                              sorter = labelProvider.getSorter(5),
	                              size = 70)
	                              
	    CATView.createTableColumn(viewer = this, 
	                              text = "Frame Subtype",
	                              tooltip = "Column 1 tooltip",
	                              sorter = labelProvider.getSorter(6),
	                              size = 70)   
	                              
	    CATView.createTableColumn(viewer = this, 
	                              text = "Encrypted",
	                              tooltip = "Column 1 tooltip",
	                              sorter = labelProvider.getSorter(7),
	                              size = 70)  
	    
	    setContentProvider(new CATContentProvider(this))
	    setLabelProvider(labelProvider)
    }
    
    getSite().setSelectionProvider(viewer);
	
	val table = viewer.getTable()
	table.setHeaderVisible(true)
	table.setLinesVisible(true)
  }

  override def setFocus() = {
    viewer.getControl().setFocus();
  }
} 

object CATView {
  val ID = "de.vogella.jface.treeviewer.view"
    
  def createTableColumn(viewer: TableViewer, text: String, tooltip: String, sorter: AbstractInvertableTableSorter, size: Int) = {
		val column = new TableColumn(viewer.getTable(), SWT.LEFT) 
		column.setText(text)
		column.setWidth(size)
		column.setToolTipText(tooltip)
		val sortListener = new TableSortSelectionListener(viewer, column, sorter)
		column.addSelectionListener(sortListener);
	}
    
}