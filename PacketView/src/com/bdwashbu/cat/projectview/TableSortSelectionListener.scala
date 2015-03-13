package com.bdwashbu.cat.projectview

import org.eclipse.swt.events.SelectionListener
import org.eclipse.jface.viewers.TableViewer
import org.eclipse.swt.widgets.TableColumn
import org.eclipse.jface.viewers.ViewerSorter
import org.eclipse.jface.viewers.Viewer
import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionEvent

abstract class InvertableSorter extends ViewerSorter {
  def compare(viewer: Viewer, e1: Object, e2: Object): Int
  def getInverseSorter(): InvertableSorter;
  def getSortDirection(): Int;
}

abstract class AbstractInvertableTableSorter extends InvertableSorter {
  val inverse = new InvertableSorter() {
    override def compare(viewer: Viewer, e1: Object, e2: Object): Int = {
      (-1) * AbstractInvertableTableSorter.this.compare(viewer, e1, e2);
    }

    def getInverseSorter(): InvertableSorter = AbstractInvertableTableSorter.this
    def getSortDirection(): Int = SWT.DOWN
  };

  def getInverseSorter(): InvertableSorter = inverse
  def getSortDirection(): Int = SWT.UP
}

case class TableSortSelectionListener(val viewer: TableViewer, val column: TableColumn, sorter: InvertableSorter) extends SelectionListener {

  def chooseColumnForSorting() = {
    viewer.getTable.setSortColumn(column);
    viewer.getTable.setSortDirection(sorter.getSortDirection());
    viewer.getControl.setRedraw(false);
    viewer.setSorter(sorter);
    viewer.getControl.setRedraw(true);
  }

  def widgetSelected(e: SelectionEvent) = {
    val newSorter = if (viewer.getTable().getSortColumn() == column) {
      sorter.getInverseSorter();
    } else sorter

    column.removeSelectionListener(this);

    val newListener = copy(sorter = newSorter)
    column.addSelectionListener(newListener);

    newListener.chooseColumnForSorting();
  }

  def widgetDefaultSelected(e: SelectionEvent) = {
    widgetSelected(e);
  }
}