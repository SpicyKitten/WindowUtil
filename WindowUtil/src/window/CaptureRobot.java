package window;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;

/**
 * Singleton capture robot, which can capture regions of the screen
 * 
 * @author ratha
 */
enum CaptureRobot
{
	INSTANCE;
	private final Robot _robot;
	
	/**
	 * Initializes the value of the singleton
	 */
	private CaptureRobot()
	{
		Robot temp = null;
		try
		{
			temp = new Robot();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
			System.err.println("CaptureRobot::new failed");
		}
		_robot = temp;
	}
	
	/**
	 * Takes a screenshot of a screen region
	 * @param r The region to screenshot
	 * @return The screenshot
	 */
	public BufferedImage screenshot(Rectangle r)
	{
		if (_robot == null)
			return null;
		return _robot.createScreenCapture(r);
	}
	
	/**
	 * Gets the pixel at (x,y) on the screen
	 * @return The pixel at (x,y) as a Color
	 */
	public Color getPixel(int x, int y)
	{
		if (_robot == null)
			return Color.BLACK;
		return _robot.getPixelColor(x, y);
	}
	
	/**
	 * Gets the pixel at (x,y) on the screen
	 * @return The pixel at (x,y) as RGB int
	 */
	public int getPixelRGB(int x, int y)
	{
		if (_robot == null)
			return Color.BLACK.getRGB();
		return _robot.getPixelColor(x, y).getRGB();
	}
	
	/**
	 * Takes a screenshot of a screen region
	 * @param r The region to screenshot
	 * @return The screenshot
	 */
	public MultiResolutionImage multiResolutionScreenshot(Rectangle r)
	{
		if (_robot == null)
			return null;
		return _robot.createMultiResolutionScreenCapture(r);
	}
}
