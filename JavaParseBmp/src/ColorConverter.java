
import java.awt.Color;

public class ColorConverter {
	
	final static int BLACK =  0xff000000 ;
	final static int WHITE =  0xffffffff ;
	final static int RED =  0xffff0000 ;
	final static int GREEN =  0xff00ff00 ;
	final static int BLUE =  0xff0000ff ;
	
	
	static public int red (int c)
	{
		return (c>>16) & 0xff;
	}
	
	static public int green (int c)
	{
		return (c>>8) & 0xff;	
	}
	
	static public int blue (int c)
	{
		return c & 0xff;
	}
	
	static public void RGBtoHSV (int rgb, float[] arrHSV)
	{
		int r = (rgb>>16) & 0xff;
		int g = (rgb>>8) & 0xff;	
		int b = (rgb) & 0xff;	
		
		float vMax = 0;
		float vMin = 0;
		
		//float sat;
		
		if(r>g)
		{
			vMax = r;
			vMin = g;
		}
		else
		{
			vMax = g;
			vMin = r;
		}
		
		if(b>vMax)
			vMax = b;
		if(b<vMin)
			vMin = b;
		
		arrHSV[2] = vMax/255.f; // set value in HSV
		
		// Calculate the saturation component
		if(vMax==vMin)
			arrHSV[1] = 0;
		else 
		{
			arrHSV[1] = (vMax - vMin) / vMax;
		} 
		
		
		
		//compute hue
		if (arrHSV[1] == 0) 
			arrHSV[0] = 0;
		else
		{
			float fHue;
			float fDelta;
			fDelta = vMax - vMin;
			
			if(r == vMax)			
				fHue = (float)(g-b) / fDelta;			
			else if (g == vMax)			
				fHue = 2 + (float)(b-r) / fDelta;		
			else		
				fHue = 4 + (r-g) / fDelta;
			
			fHue *= 60.0;
			
			if (fHue < 0)
				fHue += 360;
			
			arrHSV[0] = fHue;
		}
		
	}

}
