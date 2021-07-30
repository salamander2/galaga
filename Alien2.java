package galaga;


import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import hsa2.GraphicsConsole;

public class Alien2 extends Rectangle {
	
	static final int WINW 		= 768;
	static final int WINH 		= 1024;
	//int x,y;
	private int iSpr=0;
	private int iRot=0;
	private int iFH=0;
	private int iFV=0;
	int iPosX=0;
	int iPosY=0;		
	
	int redAlien[][]	=new int[80][7];
	int redAttack[][]	=new int[120][7];
	int iLine=0;

	int iExplode=18;
	int iAttack=0;
	int iArrival=0;
	
	Rectangle rAlien = new Rectangle(WINW/2, WINH-80,48,48);
	public int iMode=0;	
	
	Alien2(int i) {
		//System.out.println("Alien: "+i);
		loadMovement(i,1);
		loadMovement(i,2);
		// Set initial position of alien
		this.x=redAlien[1][5];
		this.y=redAlien[1][5];
		this.height=30;
		this.width=30;
		this.iLine=1;
		this.iArrival=i*6+10; // Staggered Arrival Times
	}
	
	void loadMovement(int i, int iMode) {
		// Define Enemy movement
		
		String sMode="";
		if(iMode==1) sMode="Enemy";
		if(iMode==2) sMode="Attack";
		
		String sFilename=sMode+i+".txt";
		//System.out.println("Filename is "+sFilename);
				
		File myObj = new File(sFilename);
		
		String p1="";
		String p2="";
		String p3="";
		String p4="";
		String p5="";
		String p6="";
		iLine=0;
		try {
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				
		        String data = myReader.nextLine();
		        //System.out.println("Data = ["+data+"]");
		        
		        p1=data.substring(0, data.indexOf(","));
		        data=data.substring(data.indexOf(",")+1,data.length());
		        p2=data.substring(0, data.indexOf(","));
		        data=data.substring(data.indexOf(",")+1,data.length());
		        p3=data.substring(0, data.indexOf(","));
		        data=data.substring(data.indexOf(",")+1,data.length());
		        p4=data.substring(0, data.indexOf(","));
		        data=data.substring(data.indexOf(",")+1,data.length());
		        p5=data.substring(0, data.indexOf(","));
		        data=data.substring(data.indexOf(",")+1,data.length());
		        p6=data;
	       
		       // System.out.print("                  Line:"+iLine+" "+p1+","+p2+","+p3+","+p4+","+p5+","+p6);
		        iLine++;
		        
		    	iSpr	= Integer.parseInt(p1);
				iRot	= Integer.parseInt(p2);
				iFH		= Integer.parseInt(p3);
				iFV		= Integer.parseInt(p4);
				iPosX	= Integer.parseInt(p5);
				iPosY	= Integer.parseInt(p6);
				if (iMode==1) {
					redAlien[iLine][1]=iSpr;
					redAlien[iLine][2]=iRot;
					redAlien[iLine][3]=iFH;
					redAlien[iLine][4]=iFV;
					redAlien[iLine][5]=iPosX;
					redAlien[iLine][6]=iPosY;
				};
				if (iMode==2) {
					redAttack[iLine][1]=iSpr;
					redAttack[iLine][2]=iRot;
					redAttack[iLine][3]=iFH;
					redAttack[iLine][4]=iFV;
					redAttack[iLine][5]=iPosX;
					redAttack[iLine][6]=iPosY;
				};
				
			}
			//System.out.println("");
		    myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
	}
}