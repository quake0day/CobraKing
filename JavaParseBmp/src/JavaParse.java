
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;


public class JavaParse 
{
	
	public static void main(String[] args) 
	{

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
		
		Process process = new Process();
		process.initProcess();
		
		
		//process.processImage(177);	
		process.readBmps();
		
		Toolkit.getDefaultToolkit().beep();  
			
	}
	
	 private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("HelloWorldSwing");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 
	        //Add the ubiquitous "Hello World" label.
	        JLabel label = new JLabel("Hello World");
	        frame.getContentPane().add(label);
	 
	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	    }

}
