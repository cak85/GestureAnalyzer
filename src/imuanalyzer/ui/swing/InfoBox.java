package imuanalyzer.ui.swing;

import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.ui.IInfoContent;
import imuanalyzer.ui.swing.help.HelpManager;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

/**
 * Show frequent updated key-valu pairs in a JTable
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class InfoBox extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2627290249147451521L;

	private static final Logger LOGGER = Logger.getLogger(InfoBox.class
			.getName());

	private static final long SLEEP_TIME = 250;

	JTable infoTable;

	ArrayList<IInfoContent> observedObjects = new ArrayList<IInfoContent>();

	DefaultTableModel tableModel;

	Updater updater;

	@SuppressWarnings("serial")
	public InfoBox() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JScrollPane scrollPane;

		infoTable = new JTable(0, 2);

		HelpManager.getInstance().enableHelpKey(this, "valuetable");

		infoTable.setCellSelectionEnabled(false);

		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		infoTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
					handlePopUp(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
					handlePopUp(e);
			}

			public void handlePopUp(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int r = infoTable.rowAtPoint(e.getPoint());
					if (r >= 0 && r < infoTable.getRowCount()) {
						infoTable.setRowSelectionInterval(r, r);
					} else {
						infoTable.clearSelection();
					}

					int rowindex = infoTable.getSelectedRow();
					if (rowindex < 0) {
						return;
					}

					JPopupMenu popup = createPopUp(rowindex);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		infoTable.setModel(tableModel);
		tableModel.setColumnIdentifiers(new String[] { "Element", "Values" });

		scrollPane = new JScrollPane(infoTable);
		scrollPane.setPreferredSize(new Dimension(350, 180));

		this.add(scrollPane);

	}

	private JPopupMenu createPopUp(final int rowIndex) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem item = new JMenuItem("Delete");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeInfo(rowIndex);
			}
		});
		menu.add(item);

		return menu;
	}

	private void startUpdateThread() {
		updater = new Updater(observedObjects, tableModel);
		updater.start();
	}

	public void addInfo(IInfoContent type) {
		if (!observedObjects.contains(type)) {
			observedObjects.add(type);
		}
		if (observedObjects.size() == 1) {
			startUpdateThread();
		}
	}

	public boolean isObserved(JointType type) {
		return observedObjects.contains(type);
	}

	private void removeInfo(int idx) {
		observedObjects.remove(idx);
	}

	public void removeInfo(IInfoContent type) {
		observedObjects.remove(type);
		if (observedObjects.size() == 0) {
			updater.setStop(true);
		}
	}

	private static class Updater extends Thread {

		ArrayList<IInfoContent> observedObjects;

		DefaultTableModel tableModel;

		boolean stop = false;

		private Updater(ArrayList<IInfoContent> observedObjects,
				DefaultTableModel tableModel) {
			this.observedObjects = observedObjects;
			this.tableModel = tableModel;
		}

		public void run() {
			while (true) {

				if (stop) {
					break;
				}
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {

							tableModel.setRowCount(observedObjects.size());

							for (int i = 0; i < observedObjects.size(); i++) {
								IInfoContent content = observedObjects.get(i);
								tableModel.setValueAt(content.getInfoName(), i,
										0);
								tableModel.setValueAt(content.getInfoValue(),
										i, 1);
							}
						}

					});
				} catch (InterruptedException e1) {
					LOGGER.error(e1.toString());
				} catch (InvocationTargetException e1) {
					LOGGER.error(e1.toString());
				}

				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					LOGGER.error(e.toString());
				}
			}
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}
	}
}
