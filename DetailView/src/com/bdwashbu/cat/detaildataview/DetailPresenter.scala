package com.bdwashbu.cat.detaildataview

import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.IStructuredSelection
import org.eclipse.jface.viewers.Viewer
import com.bdwashbu.cat.model.IEEE_80211.Wifi
import org.eclipse.jface.viewers.TreeViewer
import com.bdwashbu.cat.model.Packet
import reflect.runtime.universe
import reflect.runtime.universe._

import com.bdwashbu.cat.reflection.Reflection

class DetailPresenter(viewer: TreeViewer) extends ITreeContentProvider {

	val mirror = universe.runtimeMirror(getClass.getClassLoader)
  
	def getElements(inputElement: Any): Array[Object] = {
		val results: Array[Object] = inputElement match {
		  case packet: Packet => {

		    Array(packet.data)
		  }
		  case _ => Array()
		}

	    results
	}

	def dispose() = {
	}

	def inputChanged(viewer: Viewer, oldInput: Object, newInput: Object) = {
	  if (newInput != null)
		  assert(newInput.isInstanceOf[Packet])
	}

	def getChildren(parent: Any): Array[Object] = {
	  if (parent.isInstanceOf[FieldMirror]) {

		if (Reflection.isEnumeration(parent)) {
		  Array()
		} else {
		  val actualObject = parent.asInstanceOf[FieldMirror].get
		  if (actualObject != null) {
			  val decl = Reflection.getDeclaredFields(actualObject)
				if (decl.size == 1)
				  Array()
				else {
				  decl.toArray
				}
		  }
		  else
		    Array()
		}
	  }
	  else {
	    Reflection.getDeclaredFields(parent).toArray
	  }
	}

	def getParent(element: Any) = {
		null
	}

	def hasChildren(element: Any) = {
	    val children = getChildren(element)
	    children.size > 0
	}

}