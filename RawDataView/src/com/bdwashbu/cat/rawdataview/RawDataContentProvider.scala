package com.bdwashbu.cat.rawdataview

import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.Viewer
import org.eclipse.jface.viewers.IStructuredContentProvider
import org.eclipse.jface.viewers.ILazyTreeContentProvider
import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.jface.viewers.ILazyContentProvider
import org.eclipse.jface.viewers.TableViewer
import com.bdwashbu.cat.model.Packet

class RawDataContentProvider(viewer: TableViewer) extends IStructuredContentProvider with ILazyContentProvider {

  var input = Array[Packet]()

  override def dispose() {
  }
  
  override def inputChanged(viewer: Viewer, oldInput: Object , newInput: Object ) = {
    input = newInput.asInstanceOf[Array[Packet]];
  }

  def updateElement(index: Int) = {
     viewer.replace(input(index), index);
  }
  
  def  getElements(inputElement: Object): Array[Object] = {
	 input.asInstanceOf[Array[Object]];
  }

} 