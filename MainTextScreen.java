import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;


/**
 * Class for the shared main text screen in the middle of the screen
 *
 */
public class MainTextScreen {

	// Parameters
	private Hashtable<String, String> settings = null;
	
	// Rotatable text area
	private RotatedTextArea rta;
	private JFrame f;
	private JScrollPane jsp = null;
	private JViewport jvp = null;
	private String wholeText = null;
	
	// Position of visible text if whole text doesn't fit into view
	private int textPosition = 0;
	
	// An estimate of how many characters can be viewed at once
	private int showableTextLength;
	
	// Flag defining whether text area is upside down or not
	private boolean flipped = false;

	/**
	 * Constructor
	 * 
	 * @param settings
	 */
	public MainTextScreen(Hashtable<String, String> settings)
	{
		this.settings = settings;
	}

	/**
	 * Creates and initializes all components
	 */
	public boolean initialize()
	{
		this.showableTextLength = Integer.parseInt(settings.get("STORY_SCREEN_SCROLL"));
		rta = new RotatedTextArea(false);

		rta.setText("");
		int size = Integer.parseInt(settings.get("STORY_SCREEN_SIZE"));
		
		// RotatedTextArea size and width must be identical for it work correctly! 
		rta.setPreferredSize(new Dimension (size, size));
		rta.setLineWrap(true);  
		rta.setWrapStyleWord(true);  
		rta.setCaretVisible(false);

		Border compound;
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();

		//This creates a frame.
		compound = BorderFactory.createCompoundBorder(
				raisedbevel, loweredbevel);
		rta.setBorder(compound);

		Font myFont = new Font("Courier", Font.BOLD | Font.ITALIC, 16);
		rta.setFont(myFont);

		rta.getDocument().addDocumentListener((RotatedTextArea)rta);
		f = new JFrame("RotatedTextArea");

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add button listeners for handling text area scrolling
		// TODO text area scrolling should be rewritten
		
		// Create button for scrolling text up
		JButton upButton = new JButton();
		upButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/shift.gif")));
		upButton.setSize(40,40);
		upButton.setPreferredSize(new Dimension(40,40));
		upButton.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {  
				if(!flipped)
				{
					int pos = textPosition-showableTextLength;
					if(pos < 0)
					{
						pos = 0;
					}
				
					setScrollPosition(pos, showableTextLength);
				}
				else
				{
					int length = wholeText.length();
					int pos = textPosition+showableTextLength;
					if(pos > length-showableTextLength)
					{
						pos = length-showableTextLength;
					}

					setScrollPosition(pos, showableTextLength);
				}
			}
		});

		// Create button for scrolling text down
		JButton downButton = new JButton();
		downButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/shiftInvert.gif")));
		downButton.setSize(40,40);
		downButton.setPreferredSize(new Dimension(40,40));
		downButton.addActionListener(new ActionListener() {  
			public void actionPerformed(ActionEvent e) {
				if(!flipped)
				{
					int length = wholeText.length();
					int pos = textPosition+showableTextLength;
					if(pos > length-showableTextLength)
					{
						pos = length-showableTextLength;
					}

					setScrollPosition(pos, showableTextLength);
				}
				else
				{
					int pos = textPosition-showableTextLength;
					if(pos < 0)
					{
						pos = 0;
					}
				
					setScrollPosition(pos, showableTextLength);		
				}
			}
		}); 

		f.getContentPane().add(upButton, BorderLayout.NORTH);

		f.getContentPane().add(rta, BorderLayout.CENTER);
		f.getContentPane().add(downButton, BorderLayout.SOUTH);

		f.setUndecorated(true);
		f.pack();  

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenHeight = (int)screenSize.getHeight();
		int screenWidth = (int)screenSize.getWidth();

		// Set this text area at the middle of the screen
		f.setLocation(screenWidth / 2 - size / 2, screenHeight / 2 - size / 2);

		f.setVisible(true);

		setContentText("");

		return true;
	}

	/**
	 * Sets text for this text area
	 * 
	 * @param text
	 */
	public void setContentText(String text)
	{
		if(text.length() < showableTextLength)
		{
			this.wholeText = text;
			textPosition = 0;
			rta.setText(text);
		}
		// 
		else
		{
			this.wholeText = text;
			textPosition = wholeText.length() - showableTextLength;
			rta.setText(wholeText.substring(textPosition, wholeText.length()));			
		}
	}

	// Return the whole text of this text area
	public String getContentText()
	{
		return rta.getText();
	}
	
	/**
	 * Sets scroll position of the text area
	 * This functionability should be rewritten
	 * 
	 * @param pos Position in characters
	 * @param showableTextLength Estimated number of viewable characters, from settings-file
	 */
	private void setScrollPosition(int pos, int showableTextLength)
	{
		textPosition = pos;
		if(wholeText.length() < showableTextLength)
		{
			rta.setText(wholeText);
		}
		else
		{
			String viewText = wholeText.substring(textPosition, textPosition + showableTextLength);
			rta.setText(viewText);
		}
	}
	
	/**
	 * Parameter boolean tells whether this dialog is flipped upside down or not
	 * 
	 * @param b
	 */
	public void setFlipped(boolean b)
	{
		this.flipped = b;
		
		// Set selected rotation
		rta.setFlipped(b);
		f.repaint();
	}
}
