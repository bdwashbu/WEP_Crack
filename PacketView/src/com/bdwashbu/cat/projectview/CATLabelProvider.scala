package com.bdwashbu.cat.projectview

import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.swt.graphics.Image
import org.eclipse.ui.ISharedImages
import org.eclipse.ui.PlatformUI
import org.osgi.framework.FrameworkUtil
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Path
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.jface.viewers.ITableLabelProvider
import org.eclipse.swt.graphics.Color
import org.eclipse.jface.viewers.DecoratingLabelProvider
import com.bdwashbu.cat.model._
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.jface.viewers.Viewer

class CATLabelProvider(val viewer: TableViewer) extends LabelProvider with ITableLabelProvider {
  
  import IEEE_80211._
  var startTime: Double = 0
  
  def getColumnData(packet: Packet, column: Int) = 
    (column, packet.data) match {
      case (0, _) => packet.index
      case (1, _) => packet.header.time - startTime
      case (2, Wifi(header, _)) => header.source
      case (3, Wifi(header, _)) => header.destination
      case (4, _) => packet.header.dataLength
      case (5, Wifi(header, _)) => header.frameControl.frameType
      case (6, Wifi(header, _)) => header.frameControl.subType
      case (7, Wifi(header, _)) => header.frameControl.encrypted
      case _ => "label not found"
    }

  
  def getSorter(column: Int) = 
    new AbstractInvertableTableSorter {
		override def compare(viewer: Viewer, e1: Object, e2: Object) = {
		  
		    val p1 = e1.asInstanceOf[Packet]
		    val p2 = e2.asInstanceOf[Packet]
		    
		    val d1 = getColumnData(p1, column)
		    val d2 = getColumnData(p2, column)

			(d1, d2) match {
		      case (s1: Int, s2: Int) => if (s1 == s2) 0 else if (s1 > s2) 1 else -1
		      case (s1: Short, s2: Short) => if (s1 == s2) 0 else if (s1 > s2) 1 else -1
		      case (s1: Double, s2: Double) => if (s1 == s2) 0 else if (s1 > s2) 1 else -1
		      case (s1: MACAddress, s2: MACAddress) => s1.toString.compareTo(s2.toString)
		      case (x,y) => {val cmp = x.toString.compareTo(y.toString); if (cmp == 0) 0 else if (cmp > 0) 1 else -1}
		    }
		}
  }
  
  
  override def getColumnText(obj: Object, column: Int): String = {
    assert(obj.isInstanceOf[Packet])
    
    val packet = obj.asInstanceOf[Packet]

    getColumnData(packet, column) match {
      case s: String => s
      case addr: MACAddress => addr.toString
      case num: Double => "%1.6f" format num
      case x => x.toString
    }
  }
  
  override def getColumnImage(obj: Object, index: Int): Image = {
    null
  }

} 

object CATLabelProvider {
  val FOLDER = getImage("folder.gif");
  val FILE = getImage("file.gif");
  
  def getImage(file: String ): Image = {
    val bundle = FrameworkUtil.getBundle(CATLabelProvider.getClass());
    val url = FileLocator.find(bundle, new Path("icons/" + file), null);
    val image = ImageDescriptor.createFromURL(url);
    return image.createImage();
  } 
}