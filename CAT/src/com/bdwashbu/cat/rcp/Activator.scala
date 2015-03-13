package com.bdwashbu.cat.rcp

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

class Activator extends AbstractUIPlugin {

  override def start(context: BundleContext) {
    super.start(context)
    Activator.plugin = this
  }
  
  override def stop(context: BundleContext) {
    super.stop(context)
    Activator.plugin = null
  }

}

object Activator {

  val PLUGIN_ID = "com.bdwashbu.CAT"

  private var plugin: Activator = null

  def getDefault(): Activator = plugin

  def getImageDescriptor(path: String): ImageDescriptor = {
    AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path)
  }

}