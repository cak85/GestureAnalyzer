package imuanalyzer.ui;

import imuanalyzer.signalprocessing.Hand.JointType;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Show frequent updated key-valu pairs in a JTable
 * @author "Christopher-Eyk Hrabia"
 *
 */
public class InfoBox extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2627290249147451521L;

	private static final long SLEEP_TIME = 250;

	JTable infoTable;

	ArrayList<IInfoContent> observedObjects = new ArrayList<IInfoContent>();

	DefaultTableModel model;

	Updater updater;

	MainFrame mainFrame;

	public InfoBox(MainFrame mainFrame) {
		this.mainFrame = mainFrame;

		JScrollPane scrollPane;

		infoTable = new JTable(0, 2);
		infoTable.setCellSelectionEnabled(false);

		model = (DefaultTableModel) infoTable.getModel();
		model.setColumnIdentifiers(new String[] { "Element", "Values" });

		scrollPane = new JScrollPane(infoTable);
		scrollPane.setPreferredSize(new Dimension(350, 180));

		this.add(scrollPane);

	}

	private void startUpdateThread() {
		updater = new Updater(observedObjects, model, mainFrame);
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

		MainFrame mainFrame;

		boolean newRow = false;

		public void run() {
			while (true) {

				if (stop) {
					break;
				}
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {

							//hack for updating the view in main frame to the
							//right time
							if (newRow) {
								mainFrame.refresh();
							}

							if (tableModel.getRowCount() != observedObjects
									.size()) {
								newRow = true;
							} else {
								newRow = false;
							}

							tableModel.setRowCount(observedObjects.size());

							for (int i = 0; i < observedObjects.size(); i++) {
								IInfoContent content = observedObjects.get(i);
								tableModel.setValueAt(content.getInfoName(), i, 0);
								tableModel.setValueAt(content.getInfoValue(), i, 1);
							}
						}

					});
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		private Updater(ArrayList<IInfoContent> observedObjects,
				DefaultTableModel tableModel, MainFrame mainFrame) {
			this.observedObjects = observedObjects;
			this.tableModel = tableModel;
			this.mainFrame = mainFrame;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}
	}
}
