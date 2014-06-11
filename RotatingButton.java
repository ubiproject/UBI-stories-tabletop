

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Class representing a button that can be rotated
 *
 */
public class RotatingButton extends JButton {

	private static final long serialVersionUID = 1L;

	protected static final RenderingHints qualityHints;

	static {
		qualityHints = new RenderingHints(null);
		qualityHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		qualityHints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		qualityHints.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		qualityHints.put(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		qualityHints.put(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		qualityHints.put(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
	}

	// Angle of rotation
	private double rotation = 0;
	
	// Tells whether this button is rotated upside down or not
	private boolean flipped = false;
	
	// Possible image for this button (if no text is shown)
	private BufferedImage image;

	/**
	 * Constructor
	 * 
	 * @param flipped Defines whether this button is rotated upside down or not
	 */
	public RotatingButton(boolean flipped) {
		super();
		this.flipped = flipped;
		
		// Set chosen rotation angle
		if(flipped)
		{
			rotation = -Math.PI/1;
		}
		else
		{
			rotation = 0;
		}
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param text Button text
	 * @param flipped Defines whether this button is rotated upside down or not
	 */
	public RotatingButton(String text, boolean flipped) {
		super(text);
		// TODO add handling for flipped image-buttons
		this.flipped = flipped;
		if(flipped)
		{
			rotation = -Math.PI/1;
		}
		else
		{
			rotation = 0;
		}
		init();
	}

	// Overridden method
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		Area area = new Area(new Rectangle(size.width, size.height));
		area.transform(AffineTransform.getRotateInstance(rotation, size
				.getWidth() / 2.0, size.getHeight() / 2.0));
		Rectangle bounds = area.getBounds();
		size.setSize(bounds.width, bounds.height);
		return size;
	}

	// Overridden method
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		this.setPreferredSize(new Dimension(width, height));
		this.setMargin(new Insets(0, 5, 0, 5));
	}

	/**
	 * Return angle of rotation
	 * 
	 * @return double
	 */
	public double getRotation() {
		return rotation;
	}

	// Overridden method
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(qualityHints);
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.rotate(rotation, getWidth() / 2.0, getHeight() / 2.0);
		g2.drawImage(image, null, (getWidth() - image.getWidth()) / 2,
				(getHeight() - image.getHeight()) / 2);
	}

	/**
	 * Sets rotation for this button
	 * 
	 * @param rotation
	 */
	public void setRotation(double rotation) {
		this.rotation = rotation;
		this.updateImage();
	}

	/**
	 * Initalizes this button
	 */
	private void init() {
		updateImage();
		this.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				updateImage();
			}
		});
		this.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent event) {
				updateImage();
			}
		});
	}

	/**
	 * Update image contained by this button
	 */
	private void updateImage() {
		this.setSize(super.getPreferredSize());

		image = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig = image.createGraphics();
		super.paint(ig);
		ig.dispose();
		this.setSize(this.getPreferredSize());
		if (this.getParent() instanceof JComponent) {
			((JComponent) this.getParent()).revalidate();
			((JComponent) this.getParent()).repaint();
		}
	}

}