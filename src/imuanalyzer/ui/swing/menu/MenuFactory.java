package imuanalyzer.ui.swing.menu;

import imuanalyzer.data.Database;
import imuanalyzer.data.Marker;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.ui.DeviceDummy;
import imuanalyzer.ui.InfoBox;
import imuanalyzer.ui.Visual3d;
import imuanalyzer.ui.swing.charts.AccelerationChartManager;
import imuanalyzer.ui.swing.charts.FeelingChartManager;
import imuanalyzer.ui.swing.charts.JointRelationChartManager;
import imuanalyzer.ui.swing.charts.OrientationChartManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 * factory for creating some often used specific menus
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class MenuFactory {

	protected OrientationChartManager chartOrientation;
	protected AccelerationChartManager chartsAcceleration;
	protected FeelingChartManager feelingChart;
	protected JointRelationChartManager chartsRelation;

	protected Hand hand;

	protected InfoBox infoBox = null;

	protected boolean newFramesVisible;

	private Database db;

	public MenuFactory(Hand hand, OrientationChartManager _chartOrientation,
			AccelerationChartManager _chartsAcceleration,
			FeelingChartManager _feelingChart,
			JointRelationChartManager _chartsRelation, boolean newFramesVisible) {
		this.hand = hand;
		chartOrientation = _chartOrientation;
		chartsAcceleration = _chartsAcceleration;
		feelingChart = _feelingChart;
		chartsRelation = _chartsRelation;
		this.newFramesVisible = newFramesVisible;

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public JPopupMenu getDevicePopUpMenu(Visual3d visual3d,
			DeviceDummy deviceDummy) {
		JPopupMenu menu = new Visual3dDevicePopUpMenu(visual3d, deviceDummy);
		return menu;
	}

	public JPopupMenu getHandPopUpMenu(Visual3d visual3d, JointType type) {
		JPopupMenu menu = new Visual3dHandPopUpMenu(visual3d, hand, type,
				infoBox, this);
		return menu;
	}

	public JPopupMenu getChartPopUpMenu() {
		JPopupMenu popUp = new JPopupMenu();
		popUp.add(createChartMenu(null));
		return popUp;
	}

	public JPopupMenu getChartPopUpMenu(FinishListenerHandler finishHandler) {
		JPopupMenu popUp = new JPopupMenu();
		popUp.add(createChartMenu(finishHandler));
		return popUp;
	}

	public JPopupMenu getDatasetPopUpMenu(final Marker currentMarker,
			final FinishListenerHandler finishHandler) {
		JPopupMenu popUp = new JPopupMenu();

		JMenuItem item = new JMenuItem("Rename");

		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				String name = (String) JOptionPane.showInputDialog(null,
						"Change name: ", "Change name",
						JOptionPane.QUESTION_MESSAGE, null, null,
						currentMarker.getName());

				if (name != null) {

					currentMarker.setName(name);

					db.setMarker(currentMarker);
				}

				finishHandler.notifyFinished();
			}
		});

		popUp.add(item);

		item = new JMenuItem("Delete");

		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				db.removeMarker(currentMarker);
			}
		});

		popUp.add(item);

		return popUp;
	}

	public JMenu createChartMenu() {
		return createChartMenu(null);
	}

	public JMenu createChartMenu(final FinishListenerHandler finishHandler) {
		JMenu submenuChart = new JMenu("Show chart");

		JMenuItem menuitemFeeling = new JMenuItem("Feeling chart");
		menuitemFeeling.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				feelingChart.enable(newFramesVisible);
				if (finishHandler != null) {
					finishHandler.notifyFinished();
				}
			}
		});
		submenuChart.add(menuitemFeeling);

		TreeSet<JMenuItem> menuSet = new TreeSet<JMenuItem>(
				new JMenuItemComparator());

		for (Entry<JointType, Joint> entry : hand.getJointSet()) {

			Joint j = entry.getValue();

			menuSet.add(createJointGraphMenu(j, finishHandler));

		}
		for (JMenuItem item : menuSet) {
			submenuChart.add(item);
		}
		return submenuChart;
	}

	public JMenu createJointGraphMenu(Joint joint) {
		return createJointGraphMenu(joint, joint.getInfoName(), null);
	}

	public JMenu createJointGraphMenu(Joint joint,
			FinishListenerHandler finishHandler) {
		return createJointGraphMenu(joint, joint.getInfoName(), finishHandler);
	}

	public JMenu createJointGraphMenu(Joint joint, String title) {
		return createJointGraphMenu(joint, title, null);
	}

	public JMenu createJointGraphMenu(Joint joint, String title,
			final FinishListenerHandler finishHandler) {

		final JointType type = joint.getType();
		JMenu chartMenu = new JMenu(title);

		JMenuItem submenuitemAddAccelerartion = new JMenuItem("Acceleration");
		submenuitemAddAccelerartion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				chartsAcceleration.addChart(type, newFramesVisible);
				if (finishHandler != null) {
					finishHandler.notifyFinished();
				}
			}
		});

		chartMenu.add(submenuitemAddAccelerartion);

		JMenuItem submenuitemAddOrientation = new JMenuItem("Orientation");
		submenuitemAddOrientation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				chartOrientation.addDynamicChart(type, newFramesVisible);
				if (finishHandler != null) {
					finishHandler.notifyFinished();
				}
			}
		});
		chartMenu.add(submenuitemAddOrientation);

		JMenu subMenuRelation = new JMenu("Relation to ...");

		TreeSet<JMenuItem> menuItemSet = new TreeSet<JMenuItem>(
				new JMenuItemComparator());
		for (Entry<JointType, Joint> secondStageEntry : hand.getJointSet()) {
			final JointType secondType = secondStageEntry.getKey();
			if (type.equals(secondType)) {
				continue;
			}
			JMenuItem submenuitemRelation = new JMenuItem(secondStageEntry
					.getValue().getInfoName());
			submenuitemRelation.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					chartsRelation.addDynamicChart(type, secondType,
							newFramesVisible);
					if (finishHandler != null) {
						finishHandler.notifyFinished();
					}
				}
			});

			menuItemSet.add(submenuitemRelation);
		}
		for (JMenuItem item : menuItemSet) {
			subMenuRelation.add(item);
		}

		chartMenu.add(submenuitemAddAccelerartion);

		chartMenu.add(subMenuRelation);

		return chartMenu;
	}

	public InfoBox getInfoBox() {
		return infoBox;
	}

	public void setInfoBox(InfoBox infoBox) {
		this.infoBox = infoBox;
	}

	public OrientationChartManager getChartOrientation() {
		return chartOrientation;
	}

	public AccelerationChartManager getChartsAcceleration() {
		return chartsAcceleration;
	}

	public FeelingChartManager getFeelingChart() {
		return feelingChart;
	}

	public JointRelationChartManager getChartsRelation() {
		return chartsRelation;
	}

	/**
	 * compares FilterMapping by priority of its listener
	 */
	public class JMenuItemComparator implements Comparator<JMenuItem> {

		@Override
		public int compare(JMenuItem o1, JMenuItem o2) {
			return o1.getText().compareTo(o2.getText());
		}

	}
}
