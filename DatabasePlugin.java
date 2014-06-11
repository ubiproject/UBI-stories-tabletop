import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Hashtable;

/**
 * Class for creating and managing database connection and processing
 * queries and updates to the database
 */
public class DatabasePlugin
{
	// Parameters
	private Hashtable<String, String> properties = null;
	// The database connection
	private Connection conn;
	
	/**
	 * Constructor
	 * 
	 * @param properties
	 */
	public DatabasePlugin(Hashtable properties)
	{
		this.properties = properties;
	}
	
	/**
	 * Attempts to connect to the database
	 * 
	 * @return TRUE if connection was successfull (probably unnecessary)
	 * @throws Exception
	 */
	public boolean connect() throws Exception
	{
		try
		{
			// Get connection parameter read from the settings.properties-file
			String ipAddress = properties.get("DB_IP_ADDRESS");
			String dbName = properties.get("DB_NAME");
			String userName = properties.get("DB_USER_NAME");
			String password = properties.get("DB_PASSWORD");
			
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			Class.forName("com.mysql.jdbc.Driver");
			
			// Setup connection with the DB
			conn = DriverManager
					.getConnection("jdbc:mysql://" + ipAddress + ":3306/" + dbName + "?" + "user=" + userName + "&password=" + password);
			
			// Test query
			PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM stories;");
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				log("NUMBER OF STORIES IN DATABASE: " + rs.getInt(1));
			}
			log("Connected to database");
			
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Gets user's password for comparison when user is trying to log in
	 * 
	 * @param userId
	 * @return password
	 */
	public String getUserPassword(String userId)
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String passWord = null;
		try
		{
			ps = conn.prepareStatement("SELECT password FROM users WHERE nickname = ?;");
			ps.setString(1,  userId);
			rs = ps.executeQuery();
			if(rs.next())
			{
				passWord = rs.getString(1);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				rs.close();
				ps.close();
			}
			catch(Exception ex)
			{
				
			}
		}
		return passWord;
	}
	
	/**
	 * Saves contribution information of one user and one writing turn
	 * 
	 * @param userName
	 * @param contributedText
	 * @param trashed How many characters were cleared from the text area by the user
	 * @param storyId
	 * @return
	 */
	public void saveAddedContribution(String userName, String contributedText, int trashed, int storyId)
	{
		log("saveAddedContribution, userName: " + userName +", contr.: " + contributedText + ", trash: " + trashed + ", StoryID: " + storyId);

		PreparedStatement ps = null;
		ResultSet rs = null;
		String passWord = null;
		
		int userStoriesID = 1;
		
		try
		{
			// First check if this user already has contributed to this story
			ps = conn.prepareStatement("SELECT userStory_ID FROM user_stories WHERE user_FK = (SELECT user_ID FROM users WHERE " +
					"nickname = ?) AND story_FK = ?");
			ps.setString(1, userName);
			ps.setInt(2, storyId);
			rs = ps.executeQuery();
			
			if(rs.next())
			{
				// If yes, update data
				log("Existing user_stories entry");
				userStoriesID = rs.getInt(1);
				
				ps = conn.prepareStatement("UPDATE user_stories SET chars_trashed = chars_trashed + ?, "
						+ "chars_contributed = chars_contributed + ? WHERE userStory_ID = ?");
				ps.setInt(1,  trashed);
				ps.setInt(2,  contributedText.length());
				ps.setInt(3,  userStoriesID);
				ps.executeUpdate();
			}
			else
			{
				// If not, create row. First select user's id
				ps = conn.prepareStatement("SELECT user_ID from users WHERE nickname = ?");
				ps.setString(1, userName);
				rs = ps.executeQuery();
				
				int userId = -1;
				if(rs.next())
				{
					userId = rs.getInt(1);
				}
				
				log("New user_stories entry");
				
				// Stores data into database and returns an unique identifier for the story
				ps = conn.prepareStatement("INSERT INTO user_stories (user_FK, story_FK, chars_trashed, chars_contributed) VALUES " +
						"( ?, ?, ?, ?);" 
						, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, userId);
				ps.setInt(2, storyId);
				ps.setInt(3, trashed);
				ps.setInt(4, contributedText.length());
				
				// Insert into database and read the new identifier
				userStoriesID = ps.executeUpdate();
			}
			
			// Replace line breaks with HTML <br>
			contributedText = contributedText.replace("\n", "<br>");
			
			// Add text to the story
			ps = conn.prepareStatement("UPDATE stories SET body_text = CONCAT(body_text, ?) WHERE story_ID = ?");
			ps.setString(1, contributedText);
			ps.setInt(2, storyId);
			ps.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				ps.close();
				rs.close();
			}
			catch(Exception ex)
			{
			}
		}
	}

	/**
	 * Creates a new story in the database
	 * 
	 * @param storyName
	 * @return Unique identifier for the story
	 */
	public int createStory(String storyName)
	{
		int storyId = -1;
		PreparedStatement ps = null;

		ResultSet rs = null;
		try
		{
			ps = conn.prepareStatement("INSERT INTO stories values (null, ?, '', null, 0, 0, 0);", Statement.RETURN_GENERATED_KEYS); 
			ps.setString(1, storyName);
			ps.executeUpdate();
			
			// Apparently this is another way to get the created unique id
			rs = ps.getGeneratedKeys();
			if(rs.next())
			{
				storyId = rs.getInt(1);
			}
			
			// Create a default story tag for this story
			ps = conn.prepareStatement("INSERT INTO story_tags values(null, ?, ?, 1);");
			ps.setInt(1, storyId);
			ps.setInt(2, 99); // 99 is fixed "all categories" identifier
			ps.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				ps.close();
			}
			catch(Exception ex)
			{
				
			}
		}
		return storyId;
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
