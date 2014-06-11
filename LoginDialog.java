

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
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

/**
 * Class for prompting users for their login information.
 * CURRENTLY IT IS ONLY POSSIBLE TO LOG IN AS A GUEST!
 *
 */
public class LoginDialog extends JDialog
{
	JPanel panel;

	// Buttons for this dialog
	RotatingButton guestButton;
	RotatingButton userButton;
	RotatingButton cancelButton;

	private static final int DIALOG_WIDTH = 270;
	private static final int DIALOG_HEIGHT = 160;

	private static final int BUTTON_WIDTH = 250;
	private static final int BUTTON_HEIGHT = 40;
	private boolean flipped = false;
	
	String defaultUserName = null;

	// The keyboard that opened this dialog
	KeyboardSet keyboard = null;
	
	DatabasePlugin db = null;
	
	private void log(String text)
	{
		System.out.println(text);
	}
	
	/**
	 * Contructor
	 * 
	 * @param keyboard
	 * @param db Database plugin
	 * @param defaultUserName
	 * @param flipped
	 */
	public LoginDialog(KeyboardSet keyboard, DatabasePlugin db, String defaultUserName, boolean flipped)
	{
		log("LOGIN: " + defaultUserName);
		this.flipped = flipped;
		this.keyboard = keyboard;
		this.defaultUserName = defaultUserName;
		this.db = db;
		
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
		this.setUndecorated(true);
		this.setPreferredSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
		
		// Add components and listeners for button selections
		try
		{
			panel = new JPanel(new FlowLayout());
			panel.setBackground(Color.BLACK);

			guestButton = new RotatingButton("LOGIN AS A GUEST", flipped);
			guestButton.setBackground(Color.GREEN);
			guestButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			}); 

			userButton = new RotatingButton("LOGIN WITH A USER ACCOUNT", flipped);
			userButton.setBackground(new Color(110, 160, 255));
			userButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});
			userButton.setEnabled(false);  // TODO enable once a decent way to type login id and password is available

			cancelButton = new RotatingButton("CANCEL", flipped);
			cancelButton.setBackground(Color.YELLOW);
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});
			cancelButton.setEnabled(false); // TODO TEMPORARILY DISABLED
			
			guestButton.setForeground(Color.BLACK);
			userButton.setForeground(Color.BLACK);
			cancelButton.setForeground(Color.BLACK);
			
			if(!flipped)
			{

				panel.add(guestButton);
				((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(userButton);
				panel.add(cancelButton);
			}
			else
			{
				panel.add(cancelButton);
				((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(userButton);
				panel.add(guestButton);
			}
			guestButton.setBounds(guestButton.getX(), guestButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			userButton.setBounds(userButton.getX(), userButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			cancelButton.setBounds(cancelButton.getX(), cancelButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
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
	 * Handles button selections
	 * 
	 * @param button
	 */
	void buttonPressed(Object button)
	{
		if(button.equals(guestButton))
		{
			this.setVisible(false);
			this.dispose();
			keyboard.loginSelection(defaultUserName);
		}
		else if(button.equals(userButton)) // TODO currently not available
		{
			// Show login dialog
			boolean ok = false;
			String userId = null;
			while(!ok)
			{
				String[] loginData = promptRegisteredLogin();
				if(loginData == null)
				{
					// Cancelled
					break;
				}
				else
				{
					// Check login id and password given by the user
					userId = loginData[0];
					String passWord = loginData[1];
					String dbPassWord = db.getUserPassword(userId);
					if(passWord.equals(dbPassWord))
					{
						ok = true;
					}
				}
			}
			
			// Signal keyboard that login was successfull
			if(ok)
			{
				this.setVisible(false);
				this.dispose();
				keyboard.loginSelection(userId);
			}
			else
			{
				keyboard.loginSelection(null);
			}
		}
		else if(button.equals(cancelButton))
		{
			this.setVisible(false);
			this.dispose();
			keyboard.loginSelection(null);
		}
	}
	
	// Method currently not called
	private String[] promptRegisteredLogin()
	{
		try
		{
			LoginInputDialog login = new LoginInputDialog(keyboard, this, db, flipped);
			login.pack();

			/*int x = posX;
			int y = posY;

			if(flipped)
			{
				x = actionDialogLeft.getX() + actionDialogLeft.getWidth() - login.getWidth();
				y = actionDialogLeft.getY() + actionDialogLeft.getHeight() -  login.getHeight();
			}
			else
			{
				x = actionDialogLeft.getX();
				y = actionDialogLeft.getY();
			}*/

			login.setLocation(this.getX(), this.getY());
			login.setModal(true);
			login.setVisible(true);
			return null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
}
