package com.bdwashbu.cat.detaildataview;

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
import org.eclipse.jface.viewers.TreeViewerFocusCellManager
import org.eclipse.jface.viewers.TreeViewerEditor
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter
import org.eclipse.jface.viewers.ColumnViewerEditor
import org.eclipse.jface.viewers.TextCellEditor
import org.eclipse.jface.viewers.TreeViewerColumn
import reflect.runtime.universe._
import reflect.runtime.universe
import com.bdwashbu.cat.reflection.Reflection

class DetailDataView extends ViewPart {
  var viewer: TreeViewer = null;
  
  override def createPartControl(parent: Composite) {
    
    val viewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
	viewer.getTree().setLinesVisible(true);
	viewer.getTree().setHeaderVisible(true);
    
    getSite.getWorkbenchWindow.getSelectionService.addSelectionListener(new ISelectionListener() {
  
	  override def selectionChanged(part: IWorkbenchPart, selection: ISelection) {

	    if (!selection.isEmpty)
	    {
		    assert(selection.isInstanceOf[IStructuredSelection])
		    assert(selection.asInstanceOf[IStructuredSelection].getFirstElement.isInstanceOf[Packet])
		    
		    val input = selection.asInstanceOf[IStructuredSelection].getFirstElement.asInstanceOf[Packet]
		    viewer.setInput(input)
		    viewer.getTree().setRedraw(false)
		    viewer.expandToLevel(3)
		    viewer.getTree().setRedraw(true)
	    }
	  }
	});

	val column = new TreeViewerColumn(viewer, SWT.NONE);
	column.getColumn.setWidth(200);
	column.getColumn.setMoveable(true);
	column.getColumn.setText("Column 1");
	column.setLabelProvider(new ColumnLabelProvider() {

		override def getText(element: Any) = {
		  
		  if (element.isInstanceOf[FieldMirror]) {
		      element.asInstanceOf[FieldMirror].symbol.name.decoded
		  }
		  else {
			  val mirror = universe.runtimeMirror(element.getClass.getClassLoader)
			  val instanceMirror = mirror.reflect(element)
			  
			  instanceMirror.symbol.name.decoded
		  }
		}

	});
	
	val column2 = new TreeViewerColumn(viewer, SWT.NONE);
	column2.getColumn.setWidth(200);
	column2.getColumn.setMoveable(true);
	column2.getColumn.setText("Column 1");
	column2.setLabelProvider(new ColumnLabelProvider() {

		override def getText(element: Any) = {
		  
		  if (element.isInstanceOf[FieldMirror]) {
		    val obj = element.asInstanceOf[FieldMirror].get
		    val fields = Reflection.getDeclaredFields(obj)
		    if (obj.isInstanceOf[Array[Byte]]) {
		      val array = obj.asInstanceOf[Array[Byte]]
		      s"(${array.size} Bytes)"
		    } else if (fields.size > 1 && !Reflection.isEnumeration(element))
		      ""
		    else {
		        element.asInstanceOf[FieldMirror].get.toString
		    }
		  }
		  else {
			  val mirror = universe.runtimeMirror(element.getClass.getClassLoader)
			  val instanceMirror = mirror.reflect(element)
			  
			  instanceMirror.symbol.name.decoded
		  }
		}

	});
	
	viewer.setContentProvider(new DetailPresenter(viewer))
	
  }

  override def setFocus() = {
  }
} 

object CATView {
  val ID = "de.vogella.jface.treeviewer.view";
}