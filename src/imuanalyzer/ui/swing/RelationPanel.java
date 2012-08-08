package imuanalyzer.ui.swing;

import imuanalyzer.data.Database;
import imuanalyzer.signalprocessing.Hand;
import imuanalyzer.signalprocessing.Hand.JointType;
import imuanalyzer.signalprocessing.Joint;
import imuanalyzer.signalprocessing.JointRelation;
import imuanalyzer.ui.HelpManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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

	JComboBox jointDependent;
	JComboBox jointIndependent;

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
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipady = 0; // reset to default
		c.weighty = 2; // request any extra vertical space
		c.weightx = 2;
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

		final JTable table = new JTable(0, 3);
		table.setCellSelectionEnabled(false);

		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				// all cells false
				return false;
			}
		};

		table.setModel(tableModel);
		tableModel.setColumnIdentifiers(new String[] { "Joint 1", "", "Factor",
				"Joint 2" });

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					handlePopUp(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				handlePopUp(e);
			}

			public void handlePopUp(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int r = table.rowAtPoint(e.getPoint());
					if (r >= 0 && r < table.getRowCount()) {
						table.setRowSelectionInterval(r, r);
					} else {
						table.clearSelection();
					}

					int rowindex = table.getSelectedRow();
					if (rowindex < 0) {
						return;
					}

					JPopupMenu popup = createYourPopUp(rowindex);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		refreshTable();

		JScrollPane scrollPane = new JScrollPane(table);
		//scrollPane.setPreferredSize(new Dimension(300,180));
		JPanel listing = new JPanel(new BorderLayout());

		listing.add(scrollPane,BorderLayout.CENTER);

		return listing;
	}

	private JPanel createManageRelationPanel() {
		JPanel panel = new JPanel(new FlowLayout());

		JLabel filterLabel = new JLabel("Joint: ", SwingConstants.LEFT);

		HelpManager.getInstance().enableHelpKey(filterLabel, "jointrelation");

		panel.add(filterLabel);

		// joint 1
		jointDependent = new JComboBox();

		for (JointType type : JointType.values()) {
			jointDependent.addItem(Hand.jointTypeToName(type));
		}

		jointDependent.setToolTipText("Select joint");

		HelpManager.getInstance().enableHelpKey(jointDependent,
				"sensorfusionalgorithm");

		panel.add(jointDependent);

		panel.add(new JLabel(" = "));

		// relation
		SpinnerModel percentSpinnerModel = new SpinnerNumberModel(new Float(0),
				new Float(0.000), new Float(1), new Float(0.001));
		factorSpinner = new JSpinner(percentSpinnerModel);
		
		final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(
				factorSpinner, "0.###");
		factorSpinner.setEditor(editor);
		editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		
		Dimension d = factorSpinner.getPreferredSize();
		d.width = 85;
		factorSpinner.setPreferredSize(d);

		panel.add(factorSpinner);

		panel.add(new JLabel(" * "));

		// joint 2
		jointIndependent = new JComboBox();

		for (JointType type : JointType.values()) {
			jointIndependent.addItem(Hand.jointTypeToName(type));
		}

		jointIndependent.setToolTipText("Select joint");

		HelpManager.getInstance().enableHelpKey(jointIndependent,
				"sensorfusionalgorithm");

		panel.add(jointIndependent);

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

	private JPopupMenu createYourPopUp(final int rowIndex) {
		JPopupMenu menu = new JPopupMenu();

		JMenuItem item = new JMenuItem("Edit");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String str1 = (String) tableModel.getValueAt(rowIndex, 0);
				String str2 = (String) tableModel.getValueAt(rowIndex, 3);

				JointType jointType1 = Hand.nameToJointType(str1);
				String number = (String) tableModel.getValueAt(
						rowIndex, 2);
				float factor = Float.valueOf(number);
				JointType jointType2 = Hand.nameToJointType(str2);

				factorSpinner.setValue(factor);
				jointDependent.setSelectedIndex(jointType1.ordinal());
				jointIndependent.setSelectedIndex(jointType2.ordinal());
			}
		});
		menu.add(item);

		item = new JMenuItem("Delete");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String str1 = (String) tableModel.getValueAt(rowIndex, 0);
				String str2 = (String) tableModel.getValueAt(rowIndex, 3);

				JointType jointType1 = Hand.nameToJointType(str1);
				JointType jointType2 = Hand.nameToJointType(str2);

				removeRelation(jointType1, jointType2);

			}
		});
		menu.add(item);

		return menu;
	}

	private void refreshTable() {
		tableModel.setRowCount(0);
		for (JointType type : JointType.values()) {
			for (JointRelation relation : hand.getJoint(type)
					.getRelationsToOtherJoints()) {

				tableModel.addRow(new Object[] {
						""
								+ Hand.jointTypeToName(relation.getDependent()
										.getType()), " = ",
						"" + (relation.getFactor()),
						"" + Hand.jointTypeToName(type) });
			}
		}
	}

	private void addRelation() {

		JointType dependent = JointType.values()[jointDependent.getSelectedIndex()];
		JointType independent = JointType.values()[jointIndependent.getSelectedIndex()];

		addRelation(dependent, independent);
	}

	protected void addRelation(JointType dependent, JointType independent) {
		if (dependent.equals(independent)) {
			JOptionPane
					.showMessageDialog(
							this,
							"It makes no sense setting a joint in relation with itself",
							"Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		JointRelation relation = hand.getJointRelation(independent, dependent);

		Float factor = (Float) factorSpinner.getValue();
		if (relation == null) {
			relation = new JointRelation(hand.getJoint(dependent),
					hand.getJoint(independent), factor);
			hand.getJoint(independent).addRelation(relation);

		} else {
			relation.setFactor(factor);
		}

		if (db != null) {
			db.setJointRelation(relation);
		}

		refreshTable();
	}

	private void removeRelation() {
		JointType joint1 = JointType.values()[jointDependent.getSelectedIndex()];
		JointType joint2 = JointType.values()[jointIndependent.getSelectedIndex()];

		removeRelation(joint1, joint2);
	}

	protected void removeRelation(JointType joint1, JointType joint2) {
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
