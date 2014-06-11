
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Class for viewing a set of three rotatable buttons.
 *
 */
public class ActionButtonDialog extends JDialog {

	// Main panel
	JPanel panel;

	// Buttons
	RotatingButton topButton;
	RotatingButton middleButton;
	RotatingButton bottomButton;

	// Fixed sizes for the dialog and buttons, could be moved to properties file
	private static final int DIALOG_WIDTH = 142;
	private static final int DIALOG_HEIGHT = 155;
	private static final int BUTTON_WIDTH = 132;
	private static final int BUTTON_HEIGHT = 40;
	
	// Flag telling whether this dialog is flipped upside down or not
	private boolean flipped = false;

	// Keyboard set to which this dialog is connected to
	KeyboardSet keyboard = null;

	/**
	 * Contructor
	 * 
	 * @param keyboard Associated KeyboardSet
	 * @param buttonNames 
	 * @param buttonColors
	 * @param flipped Whether this dialog is flipped upside down or not
	 */
	public ActionButtonDialog(KeyboardSet keyboard, String[] buttonNames, Color[] buttonColors, boolean flipped)
	{
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// Remove decorations from dualog borders
		this.setUndecorated(true);
		this.setPreferredSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
		this.keyboard = keyboard;
		this.flipped = flipped;

		if(buttonNames == null || buttonNames.length != 3)
		{
			showError("Action buttons not correctly named");
		}
		if(buttonColors == null || buttonColors.length != 3)
		{
			showError("Action button colors not correctly defined");
		}

		// Create buttons with given button names and colors
		// Also add listeners to handle button selections (when a user selects/pushes the button)
		try
		{
			panel = new JPanel(new FlowLayout());
			panel.setBackground(Color.BLACK);

			topButton = new RotatingButton(buttonNames[0], flipped);
			topButton.setBackground(buttonColors[0]);
			topButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			}); 

			middleButton = new RotatingButton(buttonNames[1], flipped);
			middleButton.setBackground(buttonColors[1]);
			middleButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});

			bottomButton = new RotatingButton(buttonNames[2], flipped);
			bottomButton.setBackground(buttonColors[2]);
			bottomButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});

			topButton.setForeground(Color.BLACK);
			middleButton.setForeground(Color.BLACK);
			bottomButton.setForeground(Color.BLACK);
			
			// Add buttons depending on this dialog's orientation
			if(!flipped)
			{
				panel.add(topButton);
				((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(middleButton);
				panel.add(bottomButton);
			}
			else
			{
				panel.add(bottomButton);
				((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(middleButton);
				panel.add(topButton);

			}
			topButton.setBounds(topButton.getX(), topButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			middleButton.setBounds(middleButton.getX(), middleButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			bottomButton.setBounds(bottomButton.getX(), bottomButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			
			this.add(panel);
			this.repaint();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			showError("Could not create action buttons: " + ex.getMessage());
		}
	}

	/**
	 * Handles selection of each button
	 * 
	 * @param button
	 */
	void buttonPressed(Object button)
	{
		String selection = null;
		if(button.equals(topButton))
		{
			selection = ((RotatingButton)topButton).getText();
		}
		else if(button.equals(middleButton))
		{
			selection = ((RotatingButton)middleButton).getText();
		}
		else if(button.equals(bottomButton))
		{
			selection = ((RotatingButton)bottomButton).getText();
		}
		// Signal keyboard of the selected button
		keyboard.buttonPressed(selection);
	}

	/**
	 * Used to alter button's text and whether it is selectable by the user
	 * 
	 * @param position Which button's state is altered
	 * @param buttonText
	 * @param active Whether the button can be selected by the user
	 */
	public void setButtonState(int position, String buttonText, boolean active)
	{
		RotatingButton button = null;

		switch(position)
		{
		case 1:
			button = topButton;
			break;
		case 2:
			button = middleButton;
			break;
		case 3:
			button = bottomButton;
			break;
		default:
			System.out.println("ERROR: unknown button position: " + position);
		}
		if(buttonText != null)
		{
			button.setText(buttonText);
		}
		button.setEnabled(active);
	}

	/**
	 * Shows a simple error message dialog.
	 * Dialog is currently non-rotatable.
	 * 
	 * @param msg 
	 */
	private void showError(String errorMessage)
	{
		JOptionPane optionPane = new JOptionPane(errorMessage, JOptionPane.ERROR_MESSAGE);
		JDialog dialog = optionPane.createDialog("Error");
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}
}
