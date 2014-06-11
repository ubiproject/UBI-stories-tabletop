
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * Main class of iUbi application for writing shared stories.
 * 
 * Creates and uses components for user interface and database,
 * handles switching of writing turns between users/keyboards. 
 *
 */
public class iUbiStoriesApplication
{
	// Plugin for handling database operations
	private DatabasePlugin db;
	// Settings read from settings.properties file
	private Hashtable<String, String> settings;
	// Id for the story currently being written, given by the database
	private int currentStoryId = -1;
	// Name for the story currently being written
	private String currentStoryName = null;
	// Rotatable text screen that shows the whole story
	private MainTextScreen mainTextScreen = null;
	// Queue for keyboards/users
	private Vector<KeyboardSet> queue = new Vector();

	/**
	 * Constructor
	 * 
	 * Loads properties, creates database connection, creates keyboards and main text screen
	 */
	public iUbiStoriesApplication()
	{
		// Load properties file
		loadProperties();
		// Create database connetion
		connectToDb();

		// Create all keyboards and their components
		KeyboardSet kb1 = new KeyboardSet(this, db, settings, "Guest1", KeyboardSet.BOTTOM_LEFT);
		KeyboardSet kb2 = new KeyboardSet(this, db, settings, "Guest2", KeyboardSet.TOP_LEFT);
		KeyboardSet kb3 = new KeyboardSet(this, db, settings, "Guest3", KeyboardSet.TOP_RIGHT);
		KeyboardSet kb4 = new KeyboardSet(this, db, settings, "Guest4", KeyboardSet.BOTTOM_RIGHT);

		kb1.setState(KeyboardSet.NO_USER);
		kb2.setState(KeyboardSet.NO_USER);
		kb3.setState(KeyboardSet.NO_USER);
		kb4.setState(KeyboardSet.NO_USER);

		// Create the main text screen
		mainTextScreen = new MainTextScreen(settings);
		mainTextScreen.initialize();
	}

	/**
	 * Returns current story name
	 * 
	 * @return String
	 */
	public String getCurrentStoryName()
	{
		return currentStoryName;
	}

	/**
	 * Creates and initializes database plugin
	 */
	private void connectToDb()
	{
		db = new DatabasePlugin(settings);
		try
		{
			db.connect();
		}
		catch(Exception e)
		{
			showErrorDialog("Could not connect to database: \n" + e.getMessage());
		}
	}

	/**
	 * Finishes the current story and stores its last contribution to the database
	 * 
	 * @param text 
	 * @param userName  
	 * @param trashedChars number of chars removed by clearing the text field
	 */
	public void finishStory(String text, String userName, int trashedChars)
	{
		addContributedText(text, userName, trashedChars);
		mainTextScreen.setContentText("");
		
		currentStoryId = -1;
		currentStoryName = null;
	}	
	
	/**
	 * Adds contributed text to the current story
	 * 
	 * @param text 
	 * @param userName 
	 * @param trashedChars number of chars removed by clearing the text field
	 */
	public void addContributedText(String text, String userName, int trashedChars)
	{
		if(userName == null || userName.length() == 0)
		{
			showErrorDialog("Can't save text, no user name found");
			return;
		}
		String contents = mainTextScreen.getContentText();
		
		// Add extra space if last character wasn't a space already
		if(contents != null && contents.length() > 0 && !contents.endsWith(" "))
		{
			contents += " ";
		}
		
		contents += text;
		mainTextScreen.setContentText(contents);

		// If this is the first contribution of a new story, create it in the database
		if(currentStoryId == -1)
		{
			currentStoryId = db.createStory(currentStoryName);
		}
		
		// Store the data
		db.saveAddedContribution(userName, text, trashedChars, currentStoryId);
	}
	

	/**
	 * Handles request for writing from a single keyboard
	 * 
	 * @param kb KeyboardSet
	 */
	public void requestParticipation(KeyboardSet kb) 
	{
		if(kb.getUserName()!= null && kb.getUserName().length() > 0)
		{
			for(int i = 0; i < queue.size(); i++)
			{
				KeyboardSet kb2 = queue.elementAt(i);
				if(kb2.getUserName() != null && kb2.getUserName().equals(kb.getUserName()))
				{
					showErrorDialog("User " + kb.getUserName() + " is already logged in");
				}
			}
		}	
		
		// If no other users are in queue, give writing turn to this keyboard
		if(queue.isEmpty())
		{
			queue.add(kb);
			activateNextUser();
		}
		// Otherwise leave it waiting for its turn
		else
		{
			queue.add(kb);
		}
	}
	
	/**
	 * Sets the current story name
	 * 
	 * @param name 
	 */
	public void setCurrentStoryName(String name)
	{
		log("NEW STORY NAME: " + name);
		this.currentStoryName = name;
	}
	
	/**
	 * Handles request for logging out a user using a single keyboard
	 * 
	 * @param kb KeyboardSet
	 */
	public void requestLogout(KeyboardSet kb)
	{
		// Remove keyboard from queue if it is found in there
		boolean removed = queue.remove(kb);
		if(!removed)
		{
			log("User " + kb.getUserName() + " logged out, was not in queue");
		}
		else
		{
			log("User " + kb.getUserName() + " logged out");
		}
		// Pass turn to the next user/keyboard
		activateNextUser();
	}

	/**
	 * Signals that one user/keyboard wants to end its turn
	 * 
	 * @param kb KeyboardSet
	 */
	public void done(KeyboardSet kb)
	{
		boolean removed = queue.remove(kb);
		if(!removed)
		{
			showErrorDialog("Couldn't find user " + kb.getUserName() + " from queue");
		}
		activateNextUser();
	}


	/**
	 * Activates next user/keyboard from the queue
	 */
	public void activateNextUser()
	{
		log("activateNextUser(), QUEUE:");
		/*for(int i = 0; i < queue.size(); i++)
		{
			log(" -> " + queue.elementAt(i).getDefaultUserName());
		}*/
		
		// If no users are queued, just return
		if(queue.isEmpty())
		{
			return;
		}
		// Get the next user/keyboard in the queue and activate it
	 	KeyboardSet kb = queue.firstElement();
	 	kb.activate();
	}

	/**
	 * Main function for launching the application
	 * 
	 * @param args
	 */
	public static void main(String args[]) 
	{
		java.awt.EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				iUbiStoriesApplication app = new iUbiStoriesApplication();
			}
		});
	}

	/**
	 * Simple convenience method for logging text to console output.
	 * Should be replaced with a log file in the future
	 * 
	 * @param log 
	 */
	private void log(String log)
	{
		System.out.println(log);
	}

	/**
	 * Flips the main text view upside down if parameter is TRUE, otherwise text view
	 * is viewed in its original rotation
	 * 
	 * @param b 
	 */
	public void setSharedTextViewFlipped(boolean b) {
		mainTextScreen.setFlipped(b);
	}

	
	/**
	 * Loads the settings file for this application.
	 * Application can not function without its settings file.
	 */
	private void loadProperties()
	{
		Properties properties = new Properties();
		InputStream input = null;

		try 
		{
			input = new FileInputStream("settings.properties");

			// load a properties file
			properties.load(input);
			settings = new Hashtable(properties);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Shows a simple error message dialog.
	 * Dialog is currently non-rotatable.
	 * 
	 * @param msg 
	 */
	public void showErrorDialog(String msg)
	{
		// TODO change to log messages only?
		JOptionPane.showMessageDialog(null, msg, "Error",
                JOptionPane.ERROR_MESSAGE);	
	}

}

