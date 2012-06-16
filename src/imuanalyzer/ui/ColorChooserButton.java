package imuanalyzer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.jme3.math.ColorRGBA;

/**
 * Button for color selection
 * 
 * @author "Christopher-Eyk Hrabia"
 * 
 */
public class ColorChooserButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8731153309898873291L;

	private static final Logger LOGGER = Logger
			.getLogger(ColorChooserButton.class.getName());

	/**
	 * color to edit/define
	 */
	ColorRGBA refColor;

	ColorChooserButton myInstance;

	Component comp;

	public ColorChooserButton(String text, ColorRGBA _refColor) {
		this(text, _refColor, new JFrame());
	}

	public ColorChooserButton(String text, ColorRGBA _refColor, Component _comp) {
		myInstance = this;
		this.refColor = _refColor;
		this.comp = _comp;
		final Color color = new Color(refColor.r, refColor.g, refColor.b,
				refColor.a);
		this.setText(text);

		this.setBackground(color);

		this.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Color newColor = JColorChooser.showDialog(comp,
						"Change Color", color);
				LOGGER.debug(newColor);

				myInstance.setBackground(newColor);
				float r = newColor.getRed() / 255f;
				float g = newColor.getGreen() / 255f;
				float b = newColor.getBlue() / 255f;
				float a = newColor.getAlpha() / 255f;

				refColor.set(r, g, b, a);
//				LOGGER.debug(refColor);

			}
		});
	}

}
