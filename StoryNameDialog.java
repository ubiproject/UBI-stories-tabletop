
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Dialog for prompting the user for a name of a story.
 * 
 *
 */
public class StoryNameDialog extends JDialog
{
	JPanel panel;

	RotatingButton infoButton;
	RotatingButton nameButton;
	RotatingButton okButton;

	private static final int DIALOG_WIDTH = 270;
	private static final int DIALOG_HEIGHT = 160;

	private static final int BUTTON_WIDTH = 250;
	private static final int BUTTON_HEIGHT = 40;
	private boolean flipped = false;

	KeyboardSet keyboard = null;
	JTextArea rta;
	LoginInputListener lil;

	DatabasePlugin db = null;

	private void log(String text)
	{
		System.out.println(text);
	}

	/**
	 * Constructor.
	 * 
	 * Creates a dialog that consists of
	 * 1) An inactive button telling the user what to do
	 * 2) A button that shows user input from keyboard, similar to a text field
	 * 3) OK-button for submitting the story name
	 * 
	 * @param keyboard
	 * @param rta
	 * @param db
	 * @param flipped
	 */
	public StoryNameDialog(KeyboardSet keyboard, JTextArea rta, DatabasePlugin db, boolean flipped)
	{
		// Tells whether this dialog is rotated upside down or not
		this.flipped = flipped;
		
		this.keyboard = keyboard;
		
		// Database plugin
		this.db = db;
		
		// Text area for user input
		this.rta = rta;

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setUndecorated(true);
		this.setPreferredSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
		
		// Disable action buttons on the right
		keyboard.getRightActionDialog().setEnabled(false);

		// Create components
		try
		{
			panel = new JPanel(new FlowLayout());
			panel.setBackground(Color.BLACK);

			infoButton = new RotatingButton("ENTER NAME FOR A NEW STORY", flipped);
			infoButton.setBackground(new Color(110, 160, 255));

			nameButton = new RotatingButton("PRESS TO WRITE", flipped);
			nameButton.setBackground(Color.WHITE);

			nameButton.setText("");
			
			// Create listener for updating text in middle button from keyboard input
			lil = new LoginInputListener(rta, nameButton);
			keyboard.getKeyboard().setVisible(true);
			keyboard.getTextComponent().setVisible(false);
			rta.getDocument().addDocumentListener(lil);

			// Create buttons and their action listeners
			okButton = new RotatingButton("OK", flipped);
			okButton.setBackground(Color.GREEN);
			okButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});

			infoButton.setForeground(Color.BLACK);
			nameButton.setForeground(Color.BLACK);
			okButton.setForeground(Color.BLACK);

			// Position components depending on whether this dialog is flipped or not
			if(!flipped)
			{

				panel.add(infoButton);
				((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(nameButton);
				panel.add(okButton);
			}
			else
			{
				panel.add(okButton);
				((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(nameButton);
				panel.add(infoButton);
			}
			infoButton.setBounds(infoButton.getX(), infoButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			nameButton.setBounds(nameButton.getX(), nameButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			okButton.setBounds(okButton.getX(), okButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			this.add(panel);
			this.repaint();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Could not create action buttons: " + ex.getMessage());
		}
	}

	/**
	 * Handles OK-button selection
	 * 
	 * @param button
	 */
	private void buttonPressed(Object button)
	{
		if(button.equals(okButton))
		{
			String name = nameButton.getText();
			
			// If user has not begun to write the text, or is attempting to submit an empty text,
			// inform te user
			if(name.equals("PRESS TO WRITE") || name.length() < 1)
			{
				infoButton.setText("PLEASE WRITE NAME FIRST");
			}
			// Set the story name and hide this component
			else
			{
				keyboard.getKeyboard().setVisible(true);
				keyboard.getTextComponent().setText("");
				keyboard.getTextComponent().setVisible(true);
				keyboard.setStoryName(name);
				keyboard.getRightActionDialog().setEnabled(true);
				rta.getDocument().removeDocumentListener(lil);
				this.setVisible(false);
				this.dispose();
			}
		}
	}

	/**
	 * Listener for setting user input visible in storyNameButton
	 *
	 */
	private static class LoginInputListener implements DocumentListener {
		private JTextArea textArea;
		private RotatingButton storyNameButton;

		public LoginInputListener(JTextArea textArea, RotatingButton storyNameButton) {
			this.textArea = textArea;
			this.storyNameButton = storyNameButton;
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			storyNameButton.setText(textArea.getText());
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			storyNameButton.setText(textArea.getText());
		}
		@Override
		public void changedUpdate(DocumentEvent e) {

		}
	}

}
