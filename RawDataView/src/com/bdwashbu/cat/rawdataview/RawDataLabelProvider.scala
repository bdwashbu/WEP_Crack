package com.bdwashbu.cat.rawdataview

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

class RawDataLabelProvider extends LabelProvider with ITableLabelProvider {
  
  import IEEE_80211._
  
  def format(mac: MACAddress) = {
    mac.addr.map(x => (x.toChar & 0xFF).toHexString.padTo(2, '0')).reduce(_ + ":" + _)
  }
  
  override def getColumnImage(obj: Object, index: Int): Image = {
    null
  }
  
  override def getColumnText(obj: Object, index: Int) = {
    "hi"
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