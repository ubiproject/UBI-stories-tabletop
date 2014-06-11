import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Class that represents a rotatable text area
 * 
 */
public class RotatedTextArea extends JTextArea implements DocumentListener {  

	// Tells whether this dialog is flipped upside down or not
	private boolean flipped = false;
	
	double angle = 0;	
	boolean showCaret = false;

	/**
	 * Constructor
	 * 
	 * @param flipped
	 */
	public RotatedTextArea(boolean flipped)
	{
		super();
		this.setPreferredSize(new Dimension(200, 200));
		if(flipped)
		{
			angle = -Math.PI/1;
		}
		else
		{
			angle = 0;
		}
	}

	/**
	 * If parameter is TRUE, sets the caret visible in the text area
	 * 
	 * @param vis
	 */
	public void setCaretVisible(boolean vis)
	{
		showCaret = vis;
		this.getCaret().setVisible(vis);
	}

	/**
	 * Sets whether this dialog is flipped upside down or not
	 * 
	 * @param flipped
	 */
	public void setFlipped(boolean flipped)
	{
		// Set the angle and repaint this component
		if(flipped)
		{
			angle = -Math.PI/1;
		}
		else
		{
			angle = 0;
		}
		this.repaint();
	}

	// Overridden method
	public void paintComponent(Graphics g) 
	{  

		double mid = Math.min(getWidth(), getHeight())/2.0;  
		Graphics2D g2d = (Graphics2D)g;  

		g2d.rotate(angle, mid, mid);  
		super.paintComponent(g2d);  
		g2d.rotate(-angle, mid, mid);  
	}

	// Overridden method
	public void changedUpdate(DocumentEvent e) 
	{
		this.repaint();

		if(showCaret)
		{
			this.getCaret().setVisible(true);
			setCaretPosition(getDocument().getLength());
		}
	}
	
	// Overridden method	
	public void removeUpdate(DocumentEvent e) 
	{
		this.repaint();
		
		if(showCaret)
		{
			this.getCaret().setVisible(true);
			setCaretPosition(getDocument().getLength());
		}			
	}
	
	// Overridden method	
	public void insertUpdate(DocumentEvent e) 
	{
		this.repaint();
		
		if(showCaret)
		{
			this.getCaret().setVisible(true);
			setCaretPosition(getDocument().getLength());
		}			
	}

}

