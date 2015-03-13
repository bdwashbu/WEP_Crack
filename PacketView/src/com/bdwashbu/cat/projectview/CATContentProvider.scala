package com.bdwashbu.cat.projectview

import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.Viewer
import org.eclipse.jface.viewers.IStructuredContentProvider
import org.eclipse.jface.viewers.ILazyTreeContentProvider
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.jface.viewers.ILazyContentProvider
import org.eclipse.jface.viewers.TableViewer
import com.bdwashbu.cat.model.Packet
import org.eclipse.jface.viewers.ViewerSorter

class CATContentProvider(viewer: TableViewer) extends IStructuredContentProvider with ILazyContentProvider {

  var input = Array[Packet]()
  var lastSorter: ViewerSorter = _

  override def dispose() {
  }
  
  override def inputChanged(viewer: Viewer, oldInput: Object , newInput: Object ) = {
    input = newInput.asInstanceOf[Array[Packet]];
  }

  def updateElement(index: Int) = {
     val sorter = viewer.getSorter()
     if (sorter != lastSorter) {
    	 input = input.sortWith((p1: Packet, p2: Packet) => if (sorter.compare(viewer, p1,p2) == 1) true else false)
    	 viewer.replace(input(index), index);
    	 lastSorter = sorter
     }
     else
       viewer.replace(input(index), index);
  }
  
  def getElements(inputElement: Object): Array[Object] = {
	 input.asInstanceOf[Array[Object]];
  }

} 