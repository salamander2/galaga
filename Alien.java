package galaga;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Alien extends Rectangle {
	
	static final int WINW 		= 768;
	static final int WINH 		= 1024;
	//int x,y;
//	private int iSpr=0;
//	private int iRot=0;
//	private int iFH=0;
//	private int iFV=0;
	//TODO check iPosX vs rectangle.x
//	int iPosX=0;
//	int iPosY=0;		
	
	//Arrays must start at zero. 
	int redAlien[][]	=new int[80][6];
	int redAttack[][]	=new int[120][6];
	int iLine=0;

	int iExplode=18;
	int iAttack=0;
	int iArrival=0;
	
	Rectangle rAlien = new Rectangle(WINW/2, WINH-80,48,48);
	int iMode=0;	
	
	Alien(int i) {
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
	
	private void loadMovement(int i, int iMode) {
		// Define Enemy movement
		
		String sMode="";
		if(iMode==1) sMode="Enemy";
		if(iMode==2) sMode="Attack";
		
		String sFilename=sMode+i+".txt";
		//System.out.println("Filename is "+sFilename);
				
		File myObj = new File(sFilename);
		
		//NOTE: iLine actually starts loading data into array line 1 and not 0.
		iLine=0;
		try {
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {				
		        String data = myReader.nextLine().trim();
		       
		        //check for comments
		        if (data.charAt(0) == '"') continue;
		        //Sample data: 4,7,0,0,168,168    
		       
		        iLine++;
		        String[] tokens = data.split(",");
		        for (int j = 0; j < tokens.length; j++) {
		        	if (iMode==1) {
		        		redAlien[iLine][j] = Integer.parseInt(tokens[j]);
		        	}
		        	if (iMode==2) {
		        		redAttack[iLine][j] = Integer.parseInt(tokens[j]);
		        	}
				}       
				
			}
			//System.out.println("");
		    myReader.close();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			System.exit(0);
		}
		
	}
}