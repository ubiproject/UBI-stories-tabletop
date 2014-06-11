import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



/**
 * Class for prompting login id and password from the user
 * CURRENTLY NOT IN USE!
 * 
 * If a more refined rotating text field is available this should be rewritten
 *
 */
public class LoginInputDialog extends JDialog
{
	private static final int DIALOG_WIDTH = 270;
	private static final int DIALOG_HEIGHT = 300;

	private static final int BUTTON_WIDTH = 230;
	private static final int BUTTON_HEIGHT = 40;
	private boolean flipped = false;
	JPanel panel;
	KeyboardSet kb = null;
	RotatingButton infoButton;
	RotatingButton loginIdButton;
	RotatingButton passWordButton;
	RotatingButton okButton;
	RotatingButton cancelButton;
	LoginInputListener listener;
	JTextArea rta;
	DatabasePlugin db;
	LoginDialog parent;
	
	public LoginInputDialog(KeyboardSet kb, LoginDialog parent, DatabasePlugin db, boolean flipped)
	{
		this.kb = kb;
		this.flipped = flipped;
		this.db = db;
		this.parent = parent;
		
		rta = (JTextArea)kb.getTextComponent();
		
		// TEST ONLY
		listener = new LoginInputListener(rta, loginIdButton);
		rta.getDocument().addDocumentListener(listener);
		
		try
		{
			panel = new JPanel();//new FlowLayout());
			panel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 8));
			//panel = new JPanel();
			//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBackground(Color.BLACK);
			
			infoButton = createButton("LOGIN");
			loginIdButton = createButton("ID");
			passWordButton = createButton("PWD");
			okButton = createButton("OK");
			cancelButton = createButton("CANCEL");
			
			infoButton.setBackground(new Color(110, 160, 255));
			loginIdButton.setBackground(Color.WHITE);	
			passWordButton.setBackground(Color.WHITE);
			okButton.setBackground(Color.GREEN);
			cancelButton.setBackground(Color.YELLOW);
			
			/*infoButton = new RotatingButton("LOGIN", flipped);
			infoButton.setBackground(new Color(110, 160, 255));
			
			loginIdButton = new RotatingButton("ID", flipped);
			loginIdButton.setBackground(Color.WHITE);			
			loginIdButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			}); 

			passWordButton = new RotatingButton("PASSWORD", flipped);
			passWordButton.setBackground(Color.WHITE);
			passWordButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});
			
			okButton = new RotatingButton("OK", flipped);
			okButton.setBackground(Color.GREEN);
			okButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});

			cancelButton = new RotatingButton("CANCEL", flipped);
			cancelButton.setBackground(Color.YELLOW);
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e)
				{
					buttonPressed(e.getSource());
				}
			});*/

			/*infoButton.setForeground(Color.BLACK);
			loginIdButton.setForeground(Color.BLACK);
			passWordButton.setForeground(Color.BLACK);
			okButton.setForeground(Color.BLACK);
			cancelButton.setForeground(Color.BLACK);
			
			infoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			loginIdButton.setForeground(Color.BLACK);
			passWordButton.setForeground(Color.BLACK);
			okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			cancelButton.setForeground(Color.BLACK);*/
			
			if(!flipped)
			{
				panel.add(infoButton);
				//((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(loginIdButton);
				//((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(passWordButton);
				//((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(okButton);
				//((FlowLayout)panel.getLayout()).setVgap(8);
				panel.add(cancelButton);
						
			}
			else
			{
				panel.add(cancelButton);
				//((FlowLayout)panel.getLayout()).setVgap(2);
				panel.add(okButton);
				panel.add(passWordButton);
				panel.add(loginIdButton);
				panel.add(infoButton);
			}
			/*infoButton.setBounds(infoButton.getX(), infoButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			loginIdButton.setBounds(loginIdButton.getX(), loginIdButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			passWordButton.setBounds(passWordButton.getX(), passWordButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			okButton.setBounds(okButton.getX(), okButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
			cancelButton.setBounds(cancelButton.getX(), cancelButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);*/
			
			this.add(panel);
			this.pack();
			this.repaint();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Could not create action buttons: " + ex.getMessage());
		}
	}


	private RotatingButton createButton(String text)
	{
		RotatingButton b = new RotatingButton(text, flipped);
		//b.setBackground(Color.WHITE);
		b.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				buttonPressed(e.getSource());
			}
		}); 
		b.setForeground(Color.BLACK);
		//b.setAlignmentX(Component.CENTER_ALIGNMENT);
		b.setBounds(b.getX(), b.getY(), BUTTON_WIDTH, BUTTON_HEIGHT);
		b.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
		b.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

		return b;
	}

	void buttonPressed(Object o)
	{
		RotatingButton b = (RotatingButton)o;
		log(" ---> " + b.getText());

	}

	private void log(String s)
	{
		System.out.println(s);
	}
	
	private static class LoginInputListener implements DocumentListener {
	    private JTextArea textArea;
	    private RotatingButton targetButton;	    
	    
	    public LoginInputListener(JTextArea textArea, RotatingButton button) {
	    //public LoginInputListener(RotatingButton loginIdButton, RotatingButton passWordButton) {
	        this.textArea = textArea;
	        this.targetButton = button;
	    }
	    @Override
	    public void insertUpdate(DocumentEvent e) {
	    	//textArea.setBackground(Color.red);
	    	targetButton.setText(textArea.getText());//setBackground(Color.RED);
	    }
	    @Override
	    public void removeUpdate(DocumentEvent e) {
	    	//textArea.setBackground(Color.red);
	    	targetButton.setText(textArea.getText());
	    }
	    @Override
	    public void changedUpdate(DocumentEvent e) {
	    	//textArea.setBackground(Color.red);
	    }
	}
}


