package com.bdwashbu.cat.rawdataview;

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
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.events.MouseListener
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.custom.StyleRange
import org.eclipse.ui.ISelectionListener
import org.eclipse.ui.IWorkbenchPart
import org.eclipse.jface.viewers.ISelection
import com.bdwashbu.cat.model.Packet
import com.bdwashbu.cat.model.IEEE_80211
import org.eclipse.swt.graphics.Font
import org.eclipse.jface.viewers.ArrayContentProvider
import org.eclipse.jface.viewers.ViewerCell
import java.awt.GraphicsEnvironment
import org.eclipse.swt.widgets.Listener
import org.eclipse.swt.widgets.Event
import com.bdwashbu.cat.model.IEEE_80211.QoSMAC
import com.bdwashbu.cat.crack.fms.RC4

class RawDataView extends ViewPart {
  var viewer: TableViewer = _;

  override def createPartControl(parent: Composite) {
    
	getSite.getWorkbenchWindow.getSelectionService.addSelectionListener(new ISelectionListener() {
  
	  override def selectionChanged(part: IWorkbenchPart, selection: ISelection) {

	    if (!selection.isEmpty)
	    {
		    assert(selection.isInstanceOf[IStructuredSelection])
		    assert(selection.asInstanceOf[IStructuredSelection].getFirstElement.isInstanceOf[Packet])
		    
		    val dissector = selection.asInstanceOf[IStructuredSelection].getFirstElement().asInstanceOf[Packet].data
		    
	        dissector match {
	          case IEEE_80211.Wifi(x: QoSMAC, rawData) => 

	            //val key = Array(0xF6, 0xD0, 0xD7, 0x2F, 0x46, 0x9D, 0xD7, 0x0D, 0xE1, 0xBA, 0x67, 0x65, 0x1A).map(_.toByte).toArray
	            val key = Array(0x76, 0xA8, 0xCB, 0xE9, 0xF0).map(_.toByte)
	            
	            
	            val cipher = RC4(x.WEP.InitVector.toArray, key)
	            
	            val decrypted = cipher.decrypt(x.payload ++ x.FCS.toArray)

	            val lines = decrypted.toSeq.grouped(16).toArray            
	            
	            viewer.setInput(lines)
	            viewer.getTable().setRedraw(false)
	            viewer.getTable.getColumns.foreach(_.pack())
	            viewer.getTable().setRedraw(true)
	            
	          case _ =>
	            
	        }
	    }
	  }
	});  
    
     viewer = new TableViewer(parent, SWT.BORDER | SWT.VIRTUAL ) {    
	    
        val fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
		val font = if (fonts contains "Consolas")
		    		  new Font( parent.getDisplay, "Consolas", 10, SWT.SMOOTH)
		    	   else
		    		  new Font( parent.getDisplay, "Courier New", 10, SWT.SMOOTH)
       
	    val col = new TableViewerColumn(this, SWT.NONE) 
		col.getColumn().setWidth(200);
		col.getColumn().setText("Offset");
		col.getColumn().setResizable(false)
		def getRowIndex(cell: ViewerCell): Int = {
		  val row = cell.getElement
		  var result = 0
		  for (i <- 0 to this.doGetItemCount) {
		    if (getElementAt(i).equals(row)) {
		      return i
		    }
		  }
		  result
		}
				
		col.setLabelProvider(new ColumnLabelProvider() {

		  override def update(cell: ViewerCell) = {
		    val text = (getRowIndex(cell)*10).toString.reverse.padTo(4, '0').reverse
		    cell.setFont(font)
		    cell.setText(text)
		  }

		});
		
		val col2 = new TableViewerColumn(this, SWT.NONE);
		col2.getColumn().setWidth(390);
		col2.getColumn().setText("Data:");
		col2.getColumn().setResizable(false)
		col2.setLabelProvider(new ColumnLabelProvider() {

		  override def update(cell: ViewerCell) = {
		    cell.setFont(font)
		    cell.setText(getText(cell.getElement))
		  }
		  
		  override def getText(element: Object) = {
		    val hex = element.asInstanceOf[Seq[Byte]].map(_.toChar.toHexString.padTo(2, '0').takeRight(2).toUpperCase).mkString
            hex.grouped(2).reduce(_ + " " + _).grouped(24).reduce(_ + " " + _)
		  }
		});
		
		val col3 = new TableViewerColumn(this, SWT.NONE);
		col3.getColumn().setWidth(200);
		col3.getColumn().setText("Ascii");
		col3.getColumn().setResizable(false)
		col3.setLabelProvider(new ColumnLabelProvider {

		  override def update(cell: ViewerCell) = {
		    cell.setFont(font)
		    cell.setText(getText(cell.getElement))
		  }
		  
		  override def getText(element: Object) = {
		    def formatNull(byte: Byte): Char = if (byte > 0x20 && byte < 0x7F) byte.toChar else '.'
		    element.asInstanceOf[Seq[Byte]].map(x => formatNull(x)).mkString.grouped(8).reduce(_ + " " + _)
		  }
		});
		                       	    
	    setContentProvider(ArrayContentProvider.getInstance)
    }

	
	val table = viewer.getTable()
	table.setHeaderVisible(true)
	table.setLinesVisible(true)

  }

  override def setFocus() = {
  }
} 

object CATView {
  val ID = "de.vogella.jface.treeviewer.view";
}