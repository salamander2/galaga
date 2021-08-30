package galaga;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import hsa2.GraphicsConsole;

class Alien2 extends Rectangle {
	
	static final boolean bAliensV2	= true; // 
	
	static final int WINW 		= 768;
	static final int WINH 		= 1024;
	//int x,y;
	private int iSpr=0;
	private int iRot=0;
	private int iFH=0;
	private int iFV=0;
	int iPosX=0;
	int iPosY=0;		
	
	int redAlien[][]	=new int[180][7];
	int redAttack[][]	=new int[120][7];
	int iLine=0;

	int iExplode=125;
	int iAttack=0;
	int iArrival=0;	
	
	Rectangle rAlien = new Rectangle(WINW/2, WINH-80,48,48);
	public int iMode=0;	
	
	int iOffsetX=-10;
	int iOffsetY=-10;
	
	Alien2(int i,int iAlienX,int iAlienY) {
		//System.out.println("Alien: "+i);
		loadMovement(i,1,iAlienY,iAlienX);
		loadMovement(i,2,iAlienY,iAlienX);
		// Set initial position of alien
		this.x=redAlien[1][5];
		this.y=redAlien[1][5];
		this.height=40;
		this.width=42;
		this.iLine=1;
		this.iArrival=Math.abs(i)*26+10; // Staggered Arrival Times
		//this.iMode=1;  // all aliens arrive at the same time
	}
	

	void loadMovement(int i, int iMode,int iAlienCol,int iAlienRow) {
		// Define Enemy movement
		// Reading from a text file: https://www.w3schools.com/java/java_files_read.asp
		
		String sMode="";
		if(iMode==1) sMode="Enemy";
		if(iMode==2) sMode="Attack";
		
		String sFilename=null;
		
		
		if(!bAliensV2) { // old aliens
			sFilename=sMode+i+".txt";
		}else { // new aliens
			if (sMode=="Enemy")sMode="Arrival";
			//sFilename="AlienPaths/"+sMode+i+"-5.txt";
			sFilename="AlienPaths/"+sMode+iAlienRow+"-"+iAlienCol+".txt";
		}
		
		System.out.println("Filename is "+sFilename);
		
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
		       
		        if(!bAliensV2) { // old aliens
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
		        }else { // new aliens
		        	p1=data;
		        	data = myReader.nextLine();
		        	p2=data;
		        	data = myReader.nextLine();
		        	p3=data;
		        	data = myReader.nextLine();
		        	p4=data;
		        	data = myReader.nextLine();
		        	p5=data;
		        	data = myReader.nextLine();
		        	p6=data;
		        }
		       // System.out.print("                  Line:"+iLine+" "+p1+","+p2+","+p3+","+p4+","+p5+","+p6);
		        iLine++;
		        
		    	iSpr	= Integer.parseInt(p1);
				iRot	= Integer.parseInt(p2);
				iFH		= Integer.parseInt(p3);
				iFH = (iFH+1)%2; //invert
				iFV		= Integer.parseInt(p4);
				iFV = (iFV+1)%2; //invert
				iPosX	= Integer.parseInt(p5);
				iPosY	= Integer.parseInt(p6);
				if (iMode==1) {
					if(iLine==1) {
						redAlien[iLine][1]=iSpr;
					}else {
						redAlien[iLine][1]=redAlien[1][1];
					}
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
		        
		    // Flip the order of motion for arrivals
		    if (iMode==1) {
		    	Collections.reverse(Arrays.asList(redAlien)); // reverse thdata since this is an arrival
		    	
		    	// PROBLEM - all the data is now at the end of the array
		    	// SOLUTION - find out where the data starts...
		    	int q;
		    	for(q=0;q<180;q++) {
		    		//System.out.println("----------------------------");
		    		//System.out.println("iX = "+redAlien[q][5]+" iY = "+redAlien[q][6]);
		    		if(redAlien[q][5]!=0) {
		    			break;
		    		}
		    	}
		    	System.out.println("Shift amount required is: "+q);
		    	// ... and move it to the start of the array
		    	int n=0;
		    	for(int r=q;r<180;r++) {
		    		n++;
		    		redAlien[n][1]=redAlien[r][1];redAlien[r][1]=0;
		    		redAlien[n][2]=redAlien[r][2];redAlien[r][2]=0;
		    		redAlien[n][3]=redAlien[r][3];redAlien[r][3]=0;
		    		redAlien[n][4]=redAlien[r][4];redAlien[r][4]=0;
		    		redAlien[n][5]=redAlien[r][5];redAlien[r][5]=0;
		    		redAlien[n][6]=redAlien[r][6];redAlien[r][6]=0;
		    	}
		    	// Let's see if the array looks better...
		    	for(q=1;q<10;q++) {
		    		//System.out.println("____________________________");
		    		//System.out.println("iX = "+redAlien[q][5]+" iY = "+redAlien[q][6]);
		    	}
		    	
		    }
		    
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}