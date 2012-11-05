package com.example.myfirstscanner;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import com.google.zxing.client.android.encode.*;
import com.example.myfirstscanner.R.id;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

public class EncoderActivity extends Activity {
	public String string;
	public ByteMatrix b;
	public String data;
	static int height=100;
	static int width=100;
	public QRCodeEncoder encoder;
	private static final int WHITE = 0xFFFFFFFF;
	  private static final int BLACK = 0xFF000000;
	 public MultiFormatWriter qw;
     public BitMatrix result = null;
     public  ImageView imageView;
     public String s[];
     public Bitmap[] bm;
     public int frame;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	qw = new MultiFormatWriter();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoder);
        Log.i("123","good1");

        Intent intent=getIntent();
        s = new String[10];
        //s[0]="12345678";
         s[0]="Money causes teenagers to feel stress. It makes them feel bad about themselves and envy other people. My friend, for instance, lives with her family and has to share a room with her sister, who is very cute and intelligent. This girl wishes she could have her own room and have a lot of stuff, but she can’t have these things because her family doesn’t have much money. Her family’s income is pretty low because her father is old and doesn’t go to work. Her sister is the only one who works. Because her family can’t buy her the things she wants, she feels a lot of stress and gets angry sometimes. Once, she wanted a beautiful dress to wear to a sweetheart dance. She asked her sister for some money to buy the dress. She was disappointed because her sister didn’t have money to give her. She sat in silence for a little while and then started yelling out loud. She said her friends got anything they wanted but she didn’t. Then she felt sorry for herself and asked why she was born into a poor family. Not having money has caused this girl to think negatively about herself and her family. It has caused a lot of stress in her life.";
        
        s[1]="Note how the first sentence, My hometown, Wheaton, is famous for several amazing geographical features,is the most general statement. This sentence is different from the two sentences that follow it, since the second and third sentences mention specific details about the town's geography, and are not general statements.";
        
        s[2]="Newspapers in India are classified into two categories according to the amount and completeness of information in them. Newspapers in the first category have more information and truth. Those in the second category do not have much information and sometimes they hide the truth. Newspapers in the first category have news collected from different parts of the country and also from different countries. They also have a lot of sports and business news and classified ads. The information they give is clear and complete and it is supported by showing pictures. The best know example of this category is the Indian Express. Important news goes on the first page with big headlines, photographs from different angles, and complete information. For example, in 1989-90, the Indian prime minister, Rajive Ghandi, was killed by a terrorist using a bomb. This newspaper investigated the situation and gave information that helped the CBI to get more support. They also showed diagrams of the area where the prime minister was killed and the positions of the bodies after the attack. This helped the reader understand what happened. Unlike newspaper in the first category, newspapers in the second category do not give as much information. They do not have international news, sports, or business news and they do not have classified ads. Also, the news they give is not complete. For example, the newspaper Hindi gave news on the death of the prime minister, but the news was not complete. The newspaper didn’t investigate the terrorist group or try to find out why this happened. Also, it did not show any pictures from the attack or give any news the next day. It just gave the news when it happened, but it didn’t follow up. Therefore, newspapers in the first group are more popular than those in the second group.";
        
        s[3]="Most students like the freedom they have in college. Usually college students live on their own, in the dormitory or in an apartment. This means they are free to come and go as they like. Their parents can’t tell them when to get up, when to go to school, and when to come home. It also means that they are free to wear what they want. There are no parents to comment about their hair styles or their dirty jeans. Finally, they are free to listen to their favorite music without interference from parents.";
        s[4]=" California is the most wonderful place to visit because of its variety of weather and its beautiful nature. (subject development) Visitors to California can find any weather they like. They can find cool temperatures in the summer; also they can find warm weather in the winter. They can find places that are difficult for humans to live in the summer because they are so hot. Or they can find places closed in the winter because of the snow. On the other hand, visitors can find the nature they like. They can find high mountains and low valleys. Visitors can find a huge forest, a dead desert, and a beautiful coast.(summary sentence) So California is the most wonderful place to visit because of its weather and nature.";
        s[5]="The first thing we did as soon as we came to the U.S.A. about two years ago was to search for an apartment in order not to live with one of our relatives. After looking for one month to find a suitable apartment, I finally found the apartment where we have been living. It includes a living room three bedrooms, and a kitchen. Probably the living room is my favorite room of all because we often gather together there after we come home from work or school. It is a comfortable room for our family. Entering the living room from the front door, we can we a new piano in the corner, with a vase of colorful flowers on it. In the opposite corner stands a Sony television, which I bought for my children to watch cartoons and for us to see films and get the daily news. Besides, there is a sofa next to the piano, a loveseat beside the TV, and also a low table between them. This is a comfortable place to sit while we watch TV or talk. On one of the light blue walls is a tranquil picture of the sea. The floor is covered with a dark red carpet, which my children like to play on. They also like to sit on it when they watch TV. The large window is shaded by a light colored curtain, giving the room a soft, bright feeling. A ceiling fan with small lights is hanging from the ceiling, whenever the fan and lights are on, we can see dangling images, which are reflected from the furniture in the room. Generally, our living room is a place where we receive our guest, gather together to discuss any topic and enjoy our leisure time.";
        s[6]="Three important Swiss customs for tourists to know deal with religion, greeting, and punctuality. (subject development) The Swiss people are very religious, and Sunday is their holy day. On Sunday, people rarely work in the garden, in the house, or even on the car. Foreign tourists should know that the most drugstores, supermarkets, and banks are closed on Sunday. The Swiss are also a formal people. For example, they seldom call acquaintances by their first names; the German “Herr” and French “Monsieur” are much more frequently used in Switzerland than the English “Mister” is used in the United States. A tourist should therefore say either “Herr” or “Monsieur” when greeting an acquaintance, and only use the person’s first name if he is a close friend. In addition, Switzerland is the land of watches and exactness. It is important to be on time to parties, business, meetings, and churches because Swiss hosts, factory bosses, and ministers all love punctuality. It is especially important for tourists to be on time for trains: Swiss train conductors never wait for late arrivers. (summary sentence) In summary, Swiss customs are very easy to follow and very important to remember!";
        s[7]="The battles of Marathon and Tours are examples of how war has often determined the development of Western civilization. (subject development) The basis of Western civilization was probably decided at the Battle of Marathon about 2,500 years ago. In this battle, a small number of Greek soldiers led by a famous Greek general defeated 100,000 invading Persians under the Persian king. Because the Greeks won, Greek ideas about many subjects matured and became the foundation of Western society. Whereas Marathon laid the basis of Western civilization, its structure remained the same as a result of the Battle of Tours in A.D. 732. Before this battle, Muslim armies had taken control of a large number of countries, but they were stopped by a group of soldiers led by Charles Martel in France. If the Muslims had won at Tours, Islam might have become the major religion of Western society.";
        s[8]="The battles of Marathon and Tours are examples of how war has often determined the development of Western civilization. (subject development) The basis of Western civilization was probably decided in Greece at the Battle of Marathon in 490 B.C. In this battle, 10,000 Greek soldiers led by Miltiades defeated 100,000 invading Persians under Darius I. Because the Greeks won, Greek ideas about philosophy, science, literature, and politics (such as democracy) matured and became the foundation of Western society. Whereas Marathon laid the basis of Western civilization, its structure remained the same as a result of the Battle of Tours in A.D. 732. Before this battle, Muslim armies had taken control of countries from India to the Atlantic Ocean, but they were stopped by a European army under Charles Martel at this battle in southwest France. If the Muslims had won at Tours, Islam might have become the major religion of Western society.";
        s[9]="A topic sentence usually comes at the beginning of a paragraph; that is, it is usually the first sentence in a formal academic paragraph.  (Sometimes this is not true, but as you practice writing with this online lesson site, please keep to this rule unless you are instructed otherwise.)  Not only is a topic sentence the first sentence of a paragraph, but, more importantly, it is the most general sentence in a paragraph.  What does most general mean?  It means that there are not many details in the sentence, but that the sentence introduces an overall idea that you want to discuss later in the paragraph.";
        frame=intent.getIntExtra("LONG_STRING",30);
        Log.i("123","string is "+string);
        //string="As allies of the British, the Iroquois were resettled in Canada after the war. In the treaty settlement, the British ceded most Indian lands to the new United States. Because New York made treaty with the Iroquois without getting Congressional approval, some of the land purchases are the subject of modern-day claims by the individual tribes";
        
        Log.i("123","string is "+string);
       imageView= (ImageView) this.findViewById(id.imageView1);
       Log.i("123","good2");
       bm=new Bitmap[10];
       int i=0;
       for(i=0;i<10;i++){
    	   s[i]=s[i].substring(0,100);
    	   bm[i]=encode(s[i]);
       }
      
       boolean a=imageView.post(new Runnable() {  
    	   
    	   int j = 0;      
    	   @Override   
    	   public void run() {
    		  if(j<=999999)
    	      imageView.setImageBitmap(bm[j%10]);
    	      if(j++ <= 999999){
    	         imageView.postDelayed(this, 1000/frame);
    	      }
    	   }
    	});
      
       
       
      /*  try {
			b=string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        try {
			data=new String(b,"ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
       // QRCode q = null;
       // QRCodeWriter qw=new QRCodeWriter();
      
     /*  try {
         try {
			result = qw.encode(string, BarcodeFormat.QR_CODE, 300, 300);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       } catch (IllegalArgumentException iae) {
         // Unsupported format
         
       }
       int width = result.getWidth();
       int height = result.getHeight();
       int[] pixels = new int[width * height];
       for (int y = 0; y < height; y++) {
         int offset = y * width;
         for (int x = 0; x < width; x++) {
           pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
         }
       }

       Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
       bitmap.setPixels(pixels, 0, width, 0, 0, width, height);*/
       /*
        try {
			bitMatrix = qw.encode(string, BarcodeFormat.QR_CODE, 300, 300);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        File file = new File("qr_png.png");
        try {
            MatrixToImageWriter.writeToFile(bitMatrix, "PNG", file);
            System.out.println("printing to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        Bitmap bm=BitmapFactory.decodeFile("qr_png.png");*/
        //BufferedImage bi=MatrixToImageWriter.toBufferedImage(bitMatrix);
      //  Bitmap bm=BitmapFactory.decodeResource( , 1);

      /*  try {
			q=Encoder.encode(string, ErrorCorrectionLevel.L);
		} catch (WriterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
        
      /*   b=q.getMatrix();
         int width = b.getWidth(); 
         int height = b.getHeight(); 

         byte[][] array = b.getArray();

         //create buffered image to draw to
         Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
         Bitmap bm = Bitmap.createBitmap(height, width, conf);

         //iterate through the matrix and draw the pixels to the image
         for (int y = 0; y < height; y++) { 
          for (int x = 0; x < width; x++) { 
           int grayValue = array[y][x] & 0xff; 
           bm.setPixel(x, y, 0x000000);
          }
         }
         //BufferedImage bi=MatrixToImageWriter.toBufferedImage(b);
       
       // QRCodeWriter writer=new QRCodeWriter();
        
        //try {
         //matrix = writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE,height ,width );
        //}
        //catch (com.google.zxing.WriterException e) {
         //exit the method
         //return;
      
       //create buffered image to draw to
       //BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

       //iterate through the matrix and draw the pixels to the image
      /* for (int y = 0; y < height; y++) { 
        for (int x = 0; x < width; x++) { 
         int grayValue = array[y][x] & 0xff; 
         image.setRGB(x, y, (grayValue == 0 ? 0 : 0xFFFFFF));
        }
       }
*/
         //TextView tv=(TextView) this.findViewById(id.textView1);
         //tv.setText(bm.getPixel(1, 1));
         //setContentView(tv);
         
      
      // imageView.setImageBitmap(bitmap);
        
        
       // BufferedImage b=MatrixToImageWriter.toBufferedImage(matrix);
        //ImageView iv=(ImageView) this.findViewById(id.imageView);
    //    try {
		//	ImageIO.write(b, "png", new File("123"));
		//} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
       
      
        	//iv.setImageBitmap(bm);
		}

    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_encoder, menu);
        return true;
    }
    public Bitmap encode(String s){
    	 Log.i("123","good3");
         BitMatrix result=null;
         Log.i("123","good4");
         try {
           try {
  			result = qw.encode(s, BarcodeFormat.QR_CODE, 300, 300);
  		} catch (WriterException e) {
  			// TODO Auto-generated catch block
  			//e.printStackTrace();
  		}
         } catch (IllegalArgumentException iae) {
           // Unsupported format
           
         }
         Log.i("123","good5");
         int width = result.getWidth();
         int height = result.getHeight();
         int[] pixels = new int[width * height];
         for (int y = 0; y < height; y++) {
           int offset = y * width;
           for (int x = 0; x < width; x++) {
             pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
           }
         }
         
         Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
         bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
         Log.i("123","should change image");
         return bitmap;
        // ImageView newiv=new ImageView(this);
         //newiv.setImageBitmap(bitmap);
         //this.setContentView(newiv);
    }
    
}
