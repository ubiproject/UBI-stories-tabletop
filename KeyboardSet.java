

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * Class for one rotatable keyboard, its action buttons and its text field
 *
 */
public class KeyboardSet {

	// Keyboard states
	public static final int NO_USER 					= 11;
	public static final int INACTIVE 					= 12;
	public static final int WAITING_FOR_TURN 			= 15;
	public static final int WRITING_TURN 				= 16;

	// Tags for locations on the screen
	public static final int BOTTOM_RIGHT 	= 101; 	// Not flipped upside-down on the screen
	public static final int BOTTOM_LEFT 	= 102; 	// Not flipped
	public static final int TOP_LEFT 		= 103; 	// Flipped
	public static final int TOP_RIGHT 		= 104; 	// Flipped

	private String userName = null;

	// Default user name for this keyboard if logged in as a guest user 
	private String defaultUserName = null;
	
	private int position = -1;
	private int posX = -1;
	private int posY = -1;
	private boolean flipped = false;
	private int keyboardState = -1;
	private int trashedChars = 0;

	// Parent application
	iUbiStoriesApplication app = null;
	
	private JFrame f = null;
	
	// Small text area for contributed text
	private RotatedTextArea rta = null;
	
	// The keyboard
	private DialogVirtualKeyboardReal keyBoardDialog = null;
	
	// Action buttons
	private ActionButtonDialog actionDialogLeft = null;
	private ActionButtonDialog actionDialogRight = null;
	
	// Database plugin
	private DatabasePlugin db = null;
	
	// Setting read from settings.properties-file
	private Hashtable<String, String> settings = null;
	
	// Margins from corners of the visible screen
	private int xMargins = 0;
	private int yMargins = 0; 


	/**
	 * Constructor
	 * 
	 * @param app Parent application
	 * @param db Database plugin
	 * @param settings
	 * @param userName
	 * @param position Where this keyboard set should be placed on the screen
	 */
	public KeyboardSet(iUbiStoriesApplication app, DatabasePlugin db, Hashtable<String, String> settings, String userName, int position)//, int locX, int locY)
	{
		// Guest user name, unique for each keyboard set
		this.defaultUserName = userName;
		this.position = position;
		this.app = app;
		this.db = db;
		this.settings = settings;

		// Rotate keyboard upside down if its position is at the top of the screen
		if(position == TOP_LEFT || position == TOP_RIGHT)
		{
			flipped = true;
		}
		xMargins = Integer.parseInt(settings.get("MARGINS_X"));
		yMargins = Integer.parseInt(settings.get("MARGINS_Y"));
		
		// Create the rest of the components
		createComponents(position);
		
		setState(NO_USER);
	}

	/**
	 * Return user name for this keyboard
	 * 
	 * @return String
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Returns the action button dialog at the right hand side of the keyboard
	 * 
	 * @return ActionButtonDialog
	 */
	public ActionButtonDialog getRightActionDialog()
	{
		return this.actionDialogRight;
	}

	/**
	 * Sets state for this keyboard, updating buttons in action dialogs to
	 * correct states and texts
	 * 
	 * @param state 
	 */
	public void setState(int state)
	{
		keyboardState = state;

		switch(state)
		{
			case NO_USER:
				keyBoardDialog.setVisible(false);
				actionDialogRight.setVisible(false);
				actionDialogLeft.setVisible(true);
				actionDialogLeft.setButtonState(1, null, false);
				actionDialogLeft.setButtonState(2, null, false);
				actionDialogLeft.setButtonState(3, "PARTICIPATE", true);
				f.setVisible(false);
				break;
			case WAITING_FOR_TURN:
				keyBoardDialog.setVisible(false);
				actionDialogRight.setVisible(false);
				actionDialogLeft.setVisible(true);
				
				// Check if this keyboard is waiting for its user to log in
				if(this.userName == null || this.userName.length() == 0)
				{
					// Disable Logout-button
					actionDialogLeft.setButtonState(1, null, false);
				}
				else
				{
					// Enable Logout-button
					actionDialogLeft.setButtonState(1, null, true);
				}
				
				actionDialogLeft.setButtonState(2, null, false);
				actionDialogLeft.setButtonState(3, "WAIT", false);
				f.setVisible(false);
				break;				
			case WRITING_TURN:
				log("WRITING_TURN: " + this.defaultUserName + " - " + this.userName);
				keyBoardDialog.setVisible(true);
				actionDialogRight.setVisible(true);
				actionDialogRight.setButtonState(1, null, true);
				actionDialogRight.setButtonState(2, null, true);
				actionDialogRight.setButtonState(3, null, true);
				actionDialogLeft.setVisible(true);
				actionDialogLeft.setButtonState(1, null, true);
				actionDialogLeft.setButtonState(2, null, true);
				actionDialogLeft.setButtonState(3, "PARTICIPATE", false);
				f.setVisible(true);
				rta.getCaret().setVisible(true);
				break;			
			case INACTIVE:
				keyBoardDialog.setVisible(false);
				actionDialogRight.setVisible(false);
				actionDialogLeft.setVisible(true);
				actionDialogLeft.setButtonState(1, null, true);
				actionDialogLeft.setButtonState(2, null, false);
				actionDialogLeft.setButtonState(3, "PARTICIPATE", true);
				f.setVisible(false);
				break;
			default:
				System.out.println("ERROR: unknow state for KeyboardSet: " + state);
				break;
		}
	}

	/**
	 * Activates this keyboard, giving its user the writing turn
	 */
	public void activate()
	{
		// If user hasn't logged in yet, do it now
		if(this.userName == null || this.userName.length() == 0)
		{
			promptLogin();
		}
		// If no story exists yet, prompt for its name
		if(app.getCurrentStoryName() == null || app.getCurrentStoryName().length() == 0)
		{
			promptStoryName();
		}
		else // TODO check if this is correct
		{
			// Depending of keyboard orientation, turn the shared main text field
			this.setState(this.WRITING_TURN);
			if(flipped)
			{
				app.setSharedTextViewFlipped(flipped);
			}
			else
			{
				app.setSharedTextViewFlipped(flipped);
			}
		}
	}

	/**
	 * Sets name for a new story
	 * 
	 * @param name
	 */
	public void setStoryName(String name)
	{
		app.setCurrentStoryName(name);
	}
	
	/**
	 * Handles user login
	 * 
	 * @param userName
	 */
	public void loginSelection(String userName)
	{
		if(userName == null || userName.length() == 0)
		{
			log("LOGIN CANCELLED");
			this.setState(NO_USER);
			app.done(this);
		}
		else
		{
			this.userName = userName;
			log("LOGGED IN AS: " + userName);
			// Depending of keyboard orientation, turn the shared main text field
			app.setSharedTextViewFlipped(flipped);

			this.setState(WRITING_TURN);
		}
	}

	/**
	 * Returns the small text area connected to this keyboard
	 * 
	 * @return JTextComponent
	 */
    public JTextComponent getTextComponent() {
        return keyBoardDialog.getTextComponent();
    }

    /**
     * Return the keyboard component
     * 
     * @return DialogVirtualKeyboardReal
     */
    public DialogVirtualKeyboardReal getKeyboard()
    {
    	return this.keyBoardDialog;
    }

	/**
	 * Creates and initalizes all user interface components for this keyboard set
	 * 
	 * @param position
	 */
	private void createComponents(int position)
	{
		try
		{
			keyBoardDialog = new DialogVirtualKeyboardReal(new javax.swing.JFrame(), false, flipped);
			keyBoardDialog.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					System.exit(0);
				}
			});
			keyBoardDialog.setFocusableWindowState(false);

			// Action buttons, left and right
			String[] buttonNames = new String[]{"LOGOUT", "SKIP TURN", "PARTICIPATE"};
			Color[] buttonColors = new Color[]{new Color(204, 70, 70), new Color(127, 20, 255), new Color(70, 204, 70),};

			actionDialogLeft = new ActionButtonDialog(this, buttonNames, buttonColors, flipped);
			actionDialogLeft.pack();
			actionDialogLeft.setVisible(true);

			buttonNames = new String[]{"FINISH STORY", "CLEAR TEXT", "SUBMIT TEXT" };
			buttonColors = new Color[]{new Color(204, 70, 70), new Color(127, 20, 255), new Color(70, 204, 70)};

			actionDialogRight = new ActionButtonDialog(this, buttonNames, buttonColors, flipped);
			actionDialogRight.pack();
			actionDialogRight.setVisible(true);	

			// Create small rotatable text area for this keyboard
			rta = new RotatedTextArea(flipped);

			rta.setText("");
			rta.setPreferredSize(new Dimension(keyBoardDialog.getWidth() - 2*actionDialogLeft.getWidth(),
					keyBoardDialog.getWidth() - 2*actionDialogLeft.getWidth()));
			rta.setLineWrap(true);  
			rta.setWrapStyleWord(true);  
			rta.setCaretVisible(true);
			rta.setCaretPosition(rta.getText().length());

			// Create a small border for the text area
			Border compound;
			Border raisedbevel = BorderFactory.createRaisedBevelBorder();
			Border loweredbevel = BorderFactory.createLoweredBevelBorder();

			compound = BorderFactory.createCompoundBorder(
					raisedbevel, loweredbevel);
			rta.setBorder(compound);
			
			// Set font to the text area
			Font myFont = new Font("Courier", Font.BOLD | Font.ITALIC, 16);
			rta.setFont(myFont);
			
			rta.getDocument().addDocumentListener((RotatedTextArea)rta);
			f = new JFrame("RotatedTextArea");

			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().add(rta, BorderLayout.CENTER);

			f.setUndecorated(true);
			f.pack();  
			f.setVisible(true);  
			
			// Pass text area to the keyboard for showing the text
			keyBoardDialog.setTextComponent(rta);

			// Set this keyboard to its correct position on the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int screenHeight = (int)screenSize.getHeight();
			int screenWidth = (int)screenSize.getWidth();
			switch(position)
			{
				case BOTTOM_LEFT:
					posX = 0 + xMargins;
					posY = screenHeight - keyBoardDialog.getHeight() - yMargins;
					break;
				case BOTTOM_RIGHT:
					posX = screenWidth-keyBoardDialog.getWidth() - xMargins;
					posY = screenHeight - keyBoardDialog.getHeight() - yMargins;
					break;
				case TOP_LEFT:
					posX = 0 + xMargins;
					posY = 0 + yMargins;
					break;
				case TOP_RIGHT:
					posX = screenWidth-keyBoardDialog.getWidth() - xMargins;;
					posY = 0 + yMargins;
					break;
				default:
					System.out.println("ERROR: Could not calculate position for KeyboardSet");
			}
			
			setDialogPositions();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	

	/**
	 * Set positions for all the components
	 * 
	 * @throws Exception
	 */
	private void setDialogPositions() throws Exception
	{
		if(!flipped)
		{
			// Set dialogs visible in their places
			actionDialogLeft.setLocation(posX, posY-actionDialogLeft.getHeight());
			f.setLocation(posX + actionDialogLeft.getWidth(), posY - f.getPreferredSize().height);
			actionDialogRight.setLocation(posX+keyBoardDialog.getWidth()-actionDialogRight.getWidth(), posY-actionDialogRight.getHeight());

			// Set the keyboard visible in its place
			keyBoardDialog.setLocation(posX, posY);
		
		}
		else
		{
			// Set the keyboard visible in its place
			keyBoardDialog.setLocation(posX, posY);
			
			// Set dialogs visible in their places
			actionDialogRight.setLocation(posX, posY+keyBoardDialog.getHeight());
			f.setLocation(posX + actionDialogRight.getWidth(), posY+keyBoardDialog.getHeight());
			f.repaint();
			actionDialogLeft.setLocation(posX + actionDialogLeft.getWidth() + rta.getWidth(), posY+keyBoardDialog.getHeight());			
		}
	}
	
	/**
	 * Asks the user for a new story name and sets it
	 */
	private void promptStoryName()
	{
		try
		{
			// Create a (clumsy) dialog for asking the user for the story name
			StoryNameDialog snd = new StoryNameDialog(this, rta, db, flipped);
			snd.pack();

			int x = posX;
			int y = posY;

			if(flipped)
			{
				x = actionDialogLeft.getX() + actionDialogLeft.getWidth() - snd.getWidth();
				y = actionDialogLeft.getY() + actionDialogLeft.getHeight() -  snd.getHeight();
			}
			else
			{
				x = actionDialogLeft.getX();
				y = actionDialogLeft.getY();
			}

			snd.setLocation(x, y);
			//snd.setModal(true); // Should this be uncommented
			snd.setVisible(true);		
		}

		catch(Exception ex)
		{
			ex.printStackTrace();
		}	
	}
	
	/**
	 * Asks the user to log in
	 */
	private void promptLogin()
	{
		try
		{
			LoginDialog login = new LoginDialog(this, db, defaultUserName, flipped);
			login.pack();

			int x = posX;
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
			}

			login.setLocation(x, y);
			login.setModal(true);
			login.setVisible(true);	
		}

		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Handles action button selections by the user
	 * 
	 * @param selection
	 */
	public void buttonPressed(String selection)
	{
		log("-> " + selection + " - " + userName);
		
		// Texts in selections should be replaced with constants instead! 
		if(selection.equalsIgnoreCase("PARTICIPATE"))
		{
			this.setState(WAITING_FOR_TURN);
			app.requestParticipation(this);
		}
		else if(selection.equalsIgnoreCase("LOGOUT"))
		{
			// No confirmation dialog showed?
			
			// Clear the text area for this keyboard, move text to the shared text area
			String addedText = rta.getText();
			rta.setText("");

			// Store the number of contributed characters to this user's account
			if(addedText.length() > 0)
			{
				app.addContributedText(addedText, userName, trashedChars);
			}
			trashedChars = 0;

			this.userName = null;
			
			this.setState(NO_USER);
			app.done(this);
			app.requestLogout(this);
		}
		else if(selection.equalsIgnoreCase("SKIP TURN"))
		{
			// Save nothing, skip writing turn
			rta.setText("");
			this.setState(this.INACTIVE);
			app.done(this);
		}
		// Finishes the current story and stores it into database
		else if(selection.equalsIgnoreCase("FINISH STORY"))
		{
			// TODO create flippable confirmation-dialog, set position over user's text area
			
			// Store the story in the database
			app.finishStory(rta.getText(), userName, trashedChars);
			rta.setText("");
			trashedChars = 0;
			this.setState(this.INACTIVE);
			app.done(this);
		}
		
		// Clear user's text area
		else if(selection.equalsIgnoreCase("CLEAR TEXT"))
		{
			// Count the number of characters to be deleted and eventually store it to the database
			// TODO Why not add characters deleted by backspace-button too?
			trashedChars += rta.getText().length();

			// Clear user's text area
			rta.setText("");
		}
		// Submit text to the story
		else if(selection.equalsIgnoreCase("SUBMIT TEXT"))
		{
			// Clear the text area for this keyboard, move text to the shared text area
			String addedText = rta.getText();

			// Store contributed tet
			app.addContributedText(addedText, userName, trashedChars);
			rta.setText("");
			trashedChars = 0;
			
			this.setState(INACTIVE);
			
			// Pass writing turn to the next logged user in writing queue
			app.done(this);
			
		}
		else
		{
			log("ERROR: unknow button selection: " + selection);
		}
	}
	
	/**
	 * Simple convenience method for logging text to console output.
	 * Should be replaced with a log file in the future
	 * 
	 * @param log 
	 */
	private void log(String text)
	{
		System.out.println(text);
	}

}
