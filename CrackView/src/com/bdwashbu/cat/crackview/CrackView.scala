package com.bdwashbu.cat.crackview;

import org.eclipse.ui.part.ViewPart
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.jface.dialogs.InputDialog
import org.eclipse.swt.widgets.Display
import org.eclipse.jface.window.Window
import org.eclipse.swt.widgets.DirectoryDialog
import org.eclipse.swt.widgets.Text
import org.eclipse.swt.layout.GridData
import java.io.File
import com.bdwashbu.cat.crack.fms.FMS
import com.bdwashbu.cat.model.IEEE_80211
import java.io.OutputStream
import java.io.PrintStream
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.DisposeEvent

class CrackView extends ViewPart {

  override def createPartControl(parent: Composite) = {
        
    val composite = new Composite(parent, SWT.BORDER);
    //val layout = new AbsoluteLayout(1, true)
    //composite.setLayout(layout);
    
    val display = Display.getCurrent();
    val white = display.getSystemColor(SWT.COLOR_WHITE);
    
    composite.setBackground(white)

    // Create a label to display what the user typed in
    val label = new Label(composite, SWT.TOP)
    label.setText("Please enter the path of a directory containing WEP packet captures:");
    label.setBackground(white)
    label.setLocation(10, 10)
    label.setSize(380, 30)
    label.setAlignment(SWT.TOP)

    val attackDirectory = new Text(composite, SWT.BORDER);
    attackDirectory.setLocation(10, 50)
    attackDirectory.setSize(300, 20)
    
    val button = new Button(composite, SWT.PUSH);
    button.setText("Browse...");
    button.setLocation(320, 50)
    button.setSize(100, 20)
    button.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(event: SelectionEvent) = {
        val dlg = new DirectoryDialog(composite.getShell());

        // Set the initial filter path according
        // to anything they've selected or typed in
        dlg.setFilterPath(attackDirectory.getText());

        // Change the title bar text
        dlg.setText("Packet capture location");

        // Customizable message displayed in the dialog
        dlg.setMessage("Select a directory");

        // Calling open() will open and run the dialog.
        // It will return the selected directory, or
        // null if user cancels
        val dir = dlg.open();
        if (dir != null) {
          // Set the text box to the new selection
          attackDirectory.setText(dir);
        }
      }
    });

    // Create the button to launch the error dialog
    val show = new Button(composite, SWT.PUSH);
    show.setText("Crack!");
    show.setLocation(10, 80)
    show.setSize(100, 20)
    show.addSelectionListener(new SelectionAdapter() {
      override def widgetSelected(event: SelectionEvent) {
        
        val dir = new File(attackDirectory.getText())
        new Thread {
          override def run() = {
            val myAP = IEEE_80211.MACAddress(Array(0x00,0x24,0xb2,0x4e,0xfe,0x6c).map(_.toByte))
	        
	        for (file <- dir.listFiles()) {
	          FMS.extractIVs(file, myAP)
	        }
	        
	        FMS.attack(4, 13)
          }
        }.start()
  
      }
    });

    val text = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    text.setSize(720, 180)
    text.setLocation(10, 110)
    text.setBackground(white)
    
    val pwLabel = new Label(composite, SWT.TOP)
    pwLabel.setText("Password:");
    pwLabel.setBackground(white)
    pwLabel.setLocation(10, 300)
    pwLabel.setSize(70, 30)
    
    val password = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER);
    password.setSize(200, 20)
    password.setLocation(80, 300)
    password.setBackground(white)
    
  val out = new OutputStream() {

   override def write(b: Int)  {
    if (text.isDisposed()) return;
    
    Display.getDefault.asyncExec(new Runnable {
      override def run() = {
        val passwordText = text.getText().lines.foreach{line => 
          if (line.startsWith("cracked password:") && line.endsWith(")")) {
            val pword = line.substring(23).dropRight(1)
            password.setText(pword)
          }
        }
	   
	    text.append(String.valueOf(b.asInstanceOf[Char]));
     }
    })
    
   }
  };
  
  val oldOut: PrintStream = System.out;
  System.setOut(new PrintStream(out));
  text.addDisposeListener(new DisposeListener() {
   def widgetDisposed(e:DisposeEvent) {
    System.setOut(oldOut);
   }
  });
    
    parent.pack();
  }

  override def setFocus() = {
  }
} 

object CrackView {
  val ID = "de.vogella.jface.treeviewer.view";
}