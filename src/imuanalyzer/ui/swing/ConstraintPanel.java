package imuanalyzer.ui.swing;

import imuanalyzer.data.Database;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Restriction;
import imuanalyzer.utils.math.AngleHelper;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Panel for configuring joint restrictions
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class ConstraintPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4820785871426946118L;

	Hand hand;

	JComboBox jointCB;

	Database db;

	JSpinner minXSpinner;
	JSpinner maxXSpinner;

	JSpinner minYSpinner;
	JSpinner maxYSpinner;

	JSpinner minZSpinner;
	JSpinner maxZSpinner;

	public ConstraintPanel(Hand hand) {
		this.hand = hand;
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setHgap(20);
		flowLayout.setVgap(20);
		this.setLayout(flowLayout);

		JPanel grid = new JPanel(new GridLayout(0, 2));

		grid.add(new JLabel("Joint: ", SwingConstants.RIGHT));

		jointCB = new JComboBox();

		for (JointType type : JointType.values()) {
			jointCB.addItem(Hand.jointTypeToName(type));
		}
		jointCB.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				updateRestrictions();
			}
		});
		grid.add(jointCB);

		// relation

		JPanel xminPanel = new JPanel();
		xminPanel.add(new JLabel("x-min:"));
		minXSpinner = new JSpinner(getRestrictionModel());
		minXSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				saveRestriction();
			}
		});
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(minXSpinner,
				"0.##");
		minXSpinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		Dimension d = minXSpinner.getPreferredSize();
		d.width = 85;
		minXSpinner.setPreferredSize(d);

		xminPanel.add(minXSpinner);
		grid.add(xminPanel);

		JPanel xmaxPanel = new JPanel();
		xmaxPanel.add(new JLabel("x-max:"));
		maxXSpinner = new JSpinner(getRestrictionModel());
		maxXSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				saveRestriction();
			}
		});
		editor = new JSpinner.NumberEditor(maxXSpinner, "0.##");
		maxXSpinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		d = maxXSpinner.getPreferredSize();
		d.width = 85;
		maxXSpinner.setPreferredSize(d);

		xmaxPanel.add(maxXSpinner);
		grid.add(xmaxPanel);

		JPanel yminPanel = new JPanel();
		yminPanel.add(new JLabel("y-min:"));
		minYSpinner = new JSpinner(getRestrictionModel());
		minYSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				saveRestriction();
			}
		});
		editor = new JSpinner.NumberEditor(minYSpinner, "0.##");
		minYSpinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		d = minYSpinner.getPreferredSize();
		d.width = 85;
		minYSpinner.setPreferredSize(d);

		yminPanel.add(minYSpinner);
		grid.add(yminPanel);

		JPanel ymaxPanel = new JPanel();
		ymaxPanel.add(new JLabel("y-max"));
		maxYSpinner = new JSpinner(getRestrictionModel());
		maxYSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				saveRestriction();
			}
		});
		editor = new JSpinner.NumberEditor(maxYSpinner, "0.##");
		maxYSpinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		d = maxYSpinner.getPreferredSize();
		d.width = 85;
		maxYSpinner.setPreferredSize(d);

		ymaxPanel.add(maxYSpinner);
		grid.add(ymaxPanel);

		JPanel zminPanel = new JPanel();
		zminPanel.add(new JLabel("z-min:"));
		minZSpinner = new JSpinner(getRestrictionModel());
		minZSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				saveRestriction();
			}
		});
		editor = new JSpinner.NumberEditor(minZSpinner, "0.##");
		minZSpinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		d = minZSpinner.getPreferredSize();
		d.width = 85;
		minZSpinner.setPreferredSize(d);

		zminPanel.add(minZSpinner);
		grid.add(zminPanel);

		JPanel zmaxPanel = new JPanel();
		zmaxPanel.add(new JLabel("z-max:"));
		maxZSpinner = new JSpinner(getRestrictionModel());
		maxZSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				saveRestriction();
			}
		});
		editor = new JSpinner.NumberEditor(maxZSpinner, "0.##");
		maxZSpinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		d = maxZSpinner.getPreferredSize();
		d.width = 85;
		maxZSpinner.setPreferredSize(d);

		zmaxPanel.add(maxZSpinner);
		grid.add(zmaxPanel);

		this.add(grid);

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		updateRestrictions();

	}

	private SpinnerModel getRestrictionModel() {
		SpinnerModel restrictionSpinnerModel = new SpinnerNumberModel(
				new Double(0), new Double(-180), new Double(180), new Double(0.01));
		return restrictionSpinnerModel;
	}

	private void saveRestriction() {
		double xmin = (Double) minXSpinner.getValue();
		double xmax = (Double) maxXSpinner.getValue();
		double ymin = (Double) minYSpinner.getValue();
		double ymax = (Double) maxYSpinner.getValue();
		double zmin = (Double) minZSpinner.getValue();
		double zmax = (Double) maxZSpinner.getValue();

		JointType type = JointType.values()[jointCB.getSelectedIndex()];

		Restriction constraint = new Restriction(AngleHelper.radFromDeg(xmin),
				AngleHelper.radFromDeg(xmax), AngleHelper.radFromDeg(ymin),
				AngleHelper.radFromDeg(ymax), AngleHelper.radFromDeg(zmin),
				AngleHelper.radFromDeg(zmax));

		db.setJointConstraint(type, constraint);
	}

	private void updateRestrictions() {
		JointType type = JointType.values()[jointCB.getSelectedIndex()];
		Restriction res = hand.getJoint(type).getRestriction();

		minXSpinner.setValue(AngleHelper.degFromRad(res.minRoll));
		maxXSpinner.setValue(AngleHelper.degFromRad(res.maxRoll));

		minYSpinner.setValue(AngleHelper.degFromRad(res.minPitch));
		maxYSpinner.setValue(AngleHelper.degFromRad(res.maxPitch));

		minZSpinner.setValue(AngleHelper.degFromRad(res.minYaw));
		maxZSpinner.setValue(AngleHelper.degFromRad(res.maxYaw));
	}

}
