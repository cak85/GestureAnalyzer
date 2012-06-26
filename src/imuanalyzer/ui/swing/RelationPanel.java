package imuanalyzer.ui.swing;

import imuanalyzer.data.Database;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.JointRelation;
import imuanalyzer.ui.HelpManager;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Panel which includes everything to configure joint relations
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class RelationPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4820785871426946118L;

	Hand hand;

	DefaultTableModel tableModel;

	JSpinner factorSpinner;

	JComboBox jointOne;
	JComboBox jointTwo;

	Database db;

	public RelationPanel(Hand hand) {
		this.hand = hand;
		this.setLayout(new GridBagLayout());

		JPanel managePanel = createManageRelationPanel();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipady = 0; // reset to default
		c.weighty = 0.1; // request any extra vertical space
		c.weightx = 0.1;
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 0;
		c.insets = new Insets(20, 20, 20, 20);

		this.add(managePanel, c);

		JPanel listPanel = createRelationsListing();

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipady = 0; // reset to default
		c.weighty = 1; // request any extra vertical space
		c.weightx = 1;
		c.gridx = 0; // aligned with button 2
		c.gridwidth = 1; // 1 columns wide
		c.gridy = 1;
		c.insets = new Insets(20, 20, 20, 20);

		this.add(listPanel, c);

		try {
			db = Database.getInstance();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("serial")
	private JPanel createRelationsListing() {

		JTable table = new JTable(0, 3);
		table.setCellSelectionEnabled(false);

		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		table.setModel(tableModel);
		tableModel.setColumnIdentifiers(new String[] { "Joint 1", "Factor",
				"Joint 2" });

		refreshTable();

		JScrollPane scrollPane = new JScrollPane(table);
		JPanel listing = new JPanel();

		listing.add(scrollPane);

		return listing;
	}

	private JPanel createManageRelationPanel() {
		JPanel panel = new JPanel(new FlowLayout());

		JLabel filterLabel = new JLabel("Joint: ", SwingConstants.LEFT);

		HelpManager.getInstance().enableHelpKey(filterLabel, "jointrelation");

		panel.add(filterLabel);

		// joint 1
		jointOne = new JComboBox();

		for (JointType type : JointType.values()) {
			jointOne.addItem(Hand.jointTypeToName(type));
		}

		jointOne.setToolTipText("Select joint");

		HelpManager.getInstance().enableHelpKey(jointOne,
				"sensorfusionalgorithm");

		panel.add(jointOne);

		panel.add(new JLabel(" = "));

		// relation
		SpinnerModel percentSpinnerModel = new SpinnerNumberModel(new Float(0),
				new Float(0.000), new Float(1), new Float(0.001));
		factorSpinner = new JSpinner(percentSpinnerModel);
		JSpinner.NumberEditor editor = (JSpinner.NumberEditor) factorSpinner
				.getEditor();
		DecimalFormat format = editor.getFormat();
		format.setMinimumFractionDigits(3);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		Dimension d = factorSpinner.getPreferredSize();
		d.width = 85;
		factorSpinner.setPreferredSize(d);

		panel.add(factorSpinner);

		panel.add(new JLabel(" * "));

		// joint 2
		jointTwo = new JComboBox();

		for (JointType type : JointType.values()) {
			jointTwo.addItem(Hand.jointTypeToName(type));
		}

		jointTwo.setToolTipText("Select joint");

		HelpManager.getInstance().enableHelpKey(jointTwo,
				"sensorfusionalgorithm");

		panel.add(jointTwo);

		JButton addRelation = new JButton("Add");
		panel.add(addRelation);
		addRelation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				addRelation();
			}
		});
		JButton removeRelation = new JButton("Remove");
		panel.add(removeRelation);
		removeRelation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				removeRelation();
			}
		});

		return panel;
	}

	private void refreshTable() {
		tableModel.setRowCount(0);
		for (JointType type : JointType.values()) {
			for (JointRelation relation : hand.getJoint(type)
					.getRelationsToOtherJoints()) {
				tableModel.addRow(new Object[] { "" + relation.getIndependent(),
						"" + (relation.getFactor()),
						"" + Hand.jointTypeToName(type) });
			}
		}
	}

	private void addRelation() {

		JointType joint1 = JointType.values()[jointOne.getSelectedIndex()];
		JointType joint2 = JointType.values()[jointTwo.getSelectedIndex()];

		if (joint1.equals(joint2)) {
			JOptionPane
					.showMessageDialog(
							this,
							"It makes no sense setting a joint in relation with itself",
							"Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		JointRelation existingRelation = hand.getJointRelation(joint2, joint1);

		Float factor = (Float) factorSpinner.getValue();
		if (existingRelation == null) {
			JointRelation relation = new JointRelation(hand.getJoint(joint1),
					hand.getJoint(joint2), factor);
			hand.getJoint(joint2).addRelation(relation);
			if (db != null) {
				db.setJointRelation(relation);
			}
		} else {
			existingRelation.setFactor(factor);
		}

		refreshTable();
	}

	private void removeRelation() {
		JointType joint1 = JointType.values()[jointOne.getSelectedIndex()];
		JointType joint2 = JointType.values()[jointTwo.getSelectedIndex()];

		if (joint1.equals(joint2)) {
			return;
		}

		Joint joint = hand.getJoint(joint2);

		JointRelation relation = hand.getJointRelation(joint2, joint1);
		db.deleteJointRelation(relation);
		joint.removeRelation(relation);

		refreshTable();
	}
}
