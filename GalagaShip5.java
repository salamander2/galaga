package galaga;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import hsa2.GraphicsConsole;

public class GalagaShip5 {

	public static void main(String[] args)  throws Exception  {
		new GalagaShip5();
	}
	
	// User setup (you will probably want to turn off sound 
	// if you are working on this for extended periods of time.
	// (No offense to the Galaga sound designer)
	static final boolean bSound	= true;
	
	// bAliensV2 - Select your alien swarm 
	// FALSE - Test of concept aliens, limited to 8, no rotation
	// TRUE - pre-programmed aliens arrive in 5 waves of 8 with rotat
	static final boolean bAliensV2	= true; // 
	
	
	static final int WINW 		= 768;
	static final int WINH 		= 1024;
	
	Random rand = new Random();
	
	GraphicsConsole gc = new GraphicsConsole (WINW,WINH, "Galaga Ship");
	BufferedImage imgSprites=null;
	BufferedImage imgLoaded=null;
	
	ArrayList<Alien2> aliens = new ArrayList<Alien2>();	
	ArrayList<Shot> shots = new ArrayList<Shot>();
	ArrayList<Bomb> bombs = new ArrayList<Bomb>();
	
	
	ArrayList<Point> alienHomes = new ArrayList<Point>(); 	//Version 2 of the aliens
	int[][] aType = new int[11][6]; 						// means 0-10, 0-6
	String[] sHiScores = new String[1000]; 						// means 0-10, 0-6
	// Choose a number of aliens from 1 to 8
	int iMaxAliens=8;
	// Choose a number of aliens from 1 to 8
	boolean bDoArrivals=true;
	boolean bDoAttacks=false;
	// Turn on TRACE
	boolean bTrace=false;
	boolean bGameOver=true;
	boolean bStartOver=false;
	static Rectangle ship = new Rectangle(WINW/2, WINH-100,54,60);
	static Rectangle shot = new Rectangle(WINW/2, WINH-80,16,18);
	int tk=0; //ticks
	int alienSpeed = 4; //(lower means faster)
	int shipSpeed = 16;
	int shotSpeed = 24;
	int bombSpeed = 1;
	boolean initShot = false;
	int iShipsLeft=3;
	int iFireX=0;
	int iFireY=0;
	static boolean bFired=false;
	int iActiveShots=0;
	int iMaxShots=2;
	int iCredits=0;
	int iArrivingAliens=8;
	
	// Starfield Init
	static final int NUMSTARS 	= 80;
	static int 		 BGSPEED 	= 5;
	ArrayList<Point> stars = new ArrayList<Point>();
	ArrayList<Color> starColors = new ArrayList<Color>();
	int iStarTimer=0;
	
	//Scoring and Other Text Display in TTF font 
	int iScore=0;
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	
	// Ship Explode
	boolean bShipExplode=false;
	int iShipExplode=0;
	
	// Sounds
	File soundAttack 	= new File("Galaga_Attack.wav");
	File soundFire 		= new File("Galaga_Fire.wav");
	File soundKill 		= new File("Galaga_Kill_Enemy.wav");
	File soundCoin 		= new File("Galaga_Coin.wav");
	File soundExplode 	= new File("explosion.wav");
	File soundGameStart	= new File("StartGame.wav");
	File soundGameTheme = new File("startTheme.wav");
	Clip clip 			= null;
	
	
	GalagaShip5() {	
		setup();
		while(true) {
			bStartOver=false;
			ship.x=WINW/2;
			splashScreen();
			// TODO:reset all alien positions
			iShipsLeft=3;
			iArrivingAliens=8;
			iScore=0;
			int i=0;
			
			for (Alien2 a: aliens) {
				i++;
				a.iArrival=Math.abs(i)*26+10; 
				a.iMode=0;
				a.iLine=0;
				a.iExplode=0;
			}
			
			shots.clear();
			bombs.clear();
			
			while(true) {
				calcGraphics();
				drawGraphics();
				if(bStartOver && (iArrivingAliens==0))break;  
			}
		}
	}
	
	public class Shot extends Rectangle {
		
		boolean bFired=true;
		int iOffset=-20;
		Shot() {
			play(soundFire);
			if(bTrace)System.out.println("init shot "+ship.x+","+ship.y);
			x=ship.x+20;
			y=ship.y;
			// Use width and height of the actual image!
			height=35;
			width=8;
			bFired=true;
	
		}
		public void moveShot() {
			//System.out.println("move shot");
			x=x+1;
			y=y-shotSpeed;
			if(y<0) {
				bFired=false;
				iActiveShots--;
			}
		}
		public void drawShot() {
			//System.out.println("draw shot "+x+","+y);
			showSprite(	0,8,0,0,x+iOffset, y);		
		}
	}
	
	public class Bomb extends Rectangle {
		
		boolean bDropped=true;
		int iOffsetX=-20;
		int iOffsetY=-10;
		
		Bomb() {
			//System.out.println("init shot "+ship.x+","+ship.y);
			x=ship.x;
			y=WINH;
			height=32;
			width=8;
			bDropped=true;
	
		}
		public void moveBomb() {
			//System.out.println("move shot");
			//x=x+1;
			y=y+bombSpeed;
			if(y>WINH) {
				bDropped=false;
				y=0;
			}
		}
		public void drawBomb() {
			//System.out.println("draw shot "+x+","+y);
			showSprite(	0,8,0,0,x+iOffsetX, y+iOffsetY);
			//gc.drawRect(x, y, width, height);
		}
	}
	
	
	void setup() {
		//getSQL();
		loadingPatterns();
		gc.setBackgroundColor(Color.black);
		gc.clear();
		loadSpriteSheet();
		
		initAlienHomes(); // part of Aliens V2
		try {
			loadHighScores();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!bAliensV2) {
			for(int j=1;j<=iMaxAliens;j++) { // Let's just load one
				Alien2 a=	new Alien2(j,0,0);
				aliens.add(a);	
			}
		}else {	
			int j=0;
			for(Point ah: alienHomes) {
				j++;
				if(j<9) {
					Alien2 a=	new Alien2(-1*j,ah.x,ah.y);
					aliens.add(a);
					if(bTrace)System.out.println("ADDED ALIEN "+j);
					//sFilename=sMode+ah.x+"-"+ah.y+".txt";
				}
			}		
		}
		
		
		//create and store a random starfield
		int x,y;
		for (int i = 0; i < NUMSTARS; i++) {
			x = rand.nextInt(WINW);
			y = rand.nextInt(WINH);
			stars.add(new Point(x,y));
			starColors.add(new Color(rand.nextInt(100)+100, rand.nextInt(100)+100, rand.nextInt(100)+100, rand.nextInt(100)+100)); 
		}
		
		// Setup Font
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("ARCADECLASSIC.ttf"))); //file goes in root of project
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Load Font
		Font font = new Font("ARCADECLASSIC", Font.BOLD, 40); // Requirement -7-
		gc.setFont(font);
	}
	
	void loadingPatterns() {
		// Simulate loading pattern of 80s hardware
		scrambleImage("Garbage.png",2000);
		scrambleImage("GarbageV2.jpg",2000);
		play(soundExplode);
		loadImage("Grid.jpg");
		gc.drawImage(imgLoaded,0,0);
		gc.sleep(2500);	
	}
	
	void splashScreen() {
		while(gc.getKeyCode()!=49 || iCredits==0) { // Press 5 to insert a coin
			synchronized(gc) {
				gc.clear();
				moveStars();
				drawStars();
				showBottomRow();
				showHighScore();
				if(iCredits>0) {
					showPushStart();
				}else {
					tk++;
					
					if(tk>50&&tk<400) {
						gc.setColor(Color.CYAN);
						gc.drawString(" GALAGA ", 320, 160);
					}
					if(tk>100&&tk<400) {
						gc.setColor(Color.CYAN);
						gc.drawLine(280, 230, 315, 230);
						gc.drawLine(480, 230, 515, 230);
						gc.drawString("SCORE", 340, 240);
					}
					if(tk>150&&tk<400) {
						gc.setColor(Color.CYAN);
						gc.drawString("  50     100", 370, 350);
						showSprite(5, 7, 0, 0, 270, 310);
					}
					if(tk>200&&tk<400) {
						gc.setColor(Color.CYAN);
						gc.drawString("100     200", 360, 410);
						showSprite(4, 7, 0, 0, 270, 370);
					}
					
					if(tk>450&&tk<650) {
						gc.setColor(Color.YELLOW);
						gc.drawString("PRESS  5  TO  INSERT  COIN", 070, 500);	
					}
					if(tk>500&&tk<650) {
						gc.setColor(Color.YELLOW);
						gc.drawString("PRESS  1  FOR  ONE  PLAYER  GAME", 070, 560);	
					}
					
					
					if(tk>700&&tk<1200) {
						showHighScores();
					}
					
					if(tk>1200)tk=0;
				}
				scanLines();
			}
			if(gc.getKeyCode()==53) {
				iCredits++;
				play(soundGameStart);
				while(gc.getKeyCode()!=0) {
					// wait for key to be let go
				}
			}
			gc.sleep(15);
		}
		bGameOver=false;
		iCredits--;
		play(soundGameTheme);
		for(int j=0;j<250;j++) {
			synchronized(gc) {
				gc.clear();
				moveStars();
				drawStars();
				showBottomRow();
				showHighScore();
				gc.setColor(Color.CYAN);
				gc.drawString("READY!", 290, 500);
				scanLines();
			}
			gc.sleep(15);
		}
	}
	
	
	void loadSpriteSheet() {
		try { // must be wrapped in a 'try' statement
			imgSprites = ImageIO.read(getClass().getClassLoader().getResource("Sprites.png")); // .jpg must be in 'bin' folder
		}catch(IOException ex) {
			System.out.println("Image cannot be loaded");
		}
	}
	
	void calcGraphics() {		
	
			moveShip();
			//moveShot();
			// Calc the aliens
			int iAlienCounter=0;
			for (Alien2 a: aliens) {
				iAlienCounter++;
				//System.out.println("Alien ID:"+iAlienCounter);
				alienGo(a);
			}
			fireShot();
			moveStars();
	}
	
	void moveShip() {
		if(!bShipExplode) {
			if (gc.isKeyDown('A')) {
				ship.x -=shipSpeed;
				if (ship.x < 0) ship.x = 0;
			}
			if (gc.isKeyDown('D')) {
			//	System.out.println("Move Right!");
				ship.x +=shipSpeed;
				if (ship.x + ship.width > WINW-25) ship.x = WINW-ship.width-25; 
			}
		}
	}
	
	void drawGraphics() {
		String sTitle="";
		synchronized(gc) {
			tk++;
			gc.clear();

			gc.setTitle("iArrivingAliens = "+iArrivingAliens);
			
			//	Draw ship
			if(!bShipExplode  && !bGameOver) {
				showSprite(0,7,0,0,ship.x,ship.y);
				//gc.drawRect(ship.x, ship.y, ship.width, ship.height);
			}
			
			// Draw all aliens
			int iAlienCounter=0;
			for (Alien2 a: aliens) {
				iAlienCounter++;
				if(bTrace)System.out.println("a.iMode="+a.iMode);
				//sTitle=sTitle+"iMode: "+a.iMode+" iAttack="+a.iAttack+", ";
				if(a.iMode==1 || a.iMode==3) { // if Arrival Mode OR Idle Mode
					//System.out.println("Draw alien "+iAlienCounter+"...frame:"+a.iLine);
				    
					showSprite(
						a.redAlien[a.iLine][1], // Sprite ID
						a.redAlien[a.iLine][2], // Rotation
						a.redAlien[a.iLine][3], // Flip X
						a.redAlien[a.iLine][4], // Flip Y
						a.x, //+a.iOffsetX,
						a.y //+a.iOffsetY
				   );
				}
				if(a.iMode==2) { 				// Alien is exploding!
					if(false) { // I really like the look of the yellow oval but...
						gc.setColor(Color.YELLOW);
						gc.fillOval(a.x-(int)(0.5*a.iExplode*10),
									a.y-(int)(0.5*a.iExplode*10),
									a.iExplode*10, a.iExplode*10);
					} else { // this is more faithful to 1981
						if(a.iExplode<125) {		
							if(a.iExplode>100) {
								showSprite(0,25,0,0,a.x-28,a.y-31);
							} else {
								if(a.iExplode>75) {
									showSprite(0,23,0,0,a.x-28,a.y-31);
								} else {
									if(a.iExplode>50) {
										showSprite(0,21,0,0,a.x-28,a.y-31);
									} else { 
										if(a.iExplode>25) {
											showSprite(0,19,0,0,a.x-28,a.y-31);
										} else {
											showSprite(0,17,0,0,a.x-28,a.y-31);
										}
									}
								}
							}
							a.iExplode--;
							a.iExplode--;
							a.iExplode--;
							a.iExplode--;
						}	
					}
				}
				if(a.iMode==4 && bDoAttacks) { // if Attack Mode and attacks are turned on	
					 showSprite(
						a.redAlien[a.iLine][1], // Sprite ID (based on arrival data
						a.redAttack[a.iAttack][2], // Rotation
						a.redAttack[a.iAttack][3], // Flip X
						a.redAttack[a.iAttack][4], // Flip Y
						a.x, 
						a.y
					);
				}
				//sTitle=sTitle+"iMode:"+a.iMode+", ";
			}
			for (Shot s: shots) {
				if(s.bFired) {
					//sTitle=sTitle+"s.x="+s.x+", s.y="+s.y;
					s.drawShot();
				};
			}
			for (Bomb b: bombs) {
				if(b.bDropped) {
					//sTitle=sTitle+"s.x="+s.x+", s.y="+s.y;
					b.drawBomb();
				};
			}
			explodeShip();
			drawStars();
			showScore();
			showBottomRow();
			scanLines();
			
			
		} // end synchronized
		

		//gc.setTitle(sTitle);
		gc.sleep(15);
	}
	
	void scanLines() {
		gc.setColor(Color.BLACK);
		for(int z=0;z<WINH;z+=3) {
			gc.drawLine(0,z,WINW,z);
		}
	}
	
	
	public void alienGo(Alien2 a) {
		if(a.iMode==0) { 						// Preparing for arrival
			if(tk%alienSpeed==0)a.iArrival--;
			if(bTrace)System.out.println("iArrival="+a.iArrival);
			if(a.iArrival==0) a.iMode=1; 	
		}
		
		if(a.iMode==1 && bDoArrivals) { 		// if Arrival and arrivals allowed
			if(tk%alienSpeed==0) {
				a.iLine++;      // control the speed!
			}
			if (a.iLine>0&&a.iLine<180) {	// Used to be 79
				// Best line ever added to smooth the animation!!!
				if(a.redAlien[a.iLine+1][5]>0) {
					a.x=(int) Math.round(a.redAlien[a.iLine][5]+(tk%alienSpeed+1.0)/alienSpeed*(a.redAlien[a.iLine+1][5]-a.redAlien[a.iLine][5]));
					a.y=(int) Math.round(a.redAlien[a.iLine][6]+(tk%alienSpeed+1.0)/alienSpeed*(a.redAlien[a.iLine+1][6]-a.redAlien[a.iLine][6]));
				}else {
					a.x=a.redAlien[a.iLine][5];
					a.y=a.redAlien[a.iLine][6];
				}
				
				if(a.x==0) {
					if(bTrace)System.out.println("DONE ARRIVING!!!!");
					a.iMode=3;  // Used to be 78
					iArrivingAliens--;
					a.iLine=a.iLine-1;  // Go back to the last valid value
					if(bTrace)System.out.println("The last valid frame is:"+a.iLine);
					if(bTrace)System.out.println("The value of x:"+a.redAlien[a.iLine][5]);
					if(bTrace)System.out.println("The value of y:"+a.redAlien[a.iLine][6]);
					// Restore the valid values for the last frame
					a.x=a.redAlien[a.iLine][5];
					a.y=a.redAlien[a.iLine][6];
					// Set the rotation to ZERO
					a.redAlien[a.iLine][2]=7;
					a.redAlien[a.iLine][4]=0;
				}

				
				if(Math.random()>0.996) {
					Bomb bb = new Bomb();
					bb.x=a.x;
					bb.y=a.y;
					bombs.add(bb);
				}
			}
		}
		
		if(a.iMode==2) { 						// Explode
			if(a.iExplode<1) {
				a.iLine=1;
				a.x=0;
				a.y=-100;
				a.iExplode=125;
				a.iMode=1;
				
			}
		}
		
		if(a.iMode==3) {						// Idle - Consider an attack
			if(bTrace)System.out.println("IDLE...");
			// flap wings
			if((tk/50)%2==0) { // Flap wings when idle
				a.redAlien[a.iLine][2]=7;
			}else {
				a.redAlien[a.iLine][2]=8;
			}
			
			if(!bShipExplode && bDoAttacks) {
				if(Math.random()>0.986) {
					a.iMode=4;
					play(soundAttack);
				}
			}
		}
					
		if(a.iMode==4 && bDoAttacks) {						// Attack
			a.iAttack++;
			if (a.iAttack>0 && a.iAttack<180) { //-SYNC (used to be 115)
		//		a.x=a.redAttack[a.iAttack][5]+a.redAlien[77][5]-165;  // offset atrack based on end of arrival data
		//		a.y=a.redAttack[a.iAttack][6]+a.redAlien[77][6]-170; // offset atrack based on end of arrival data
		//	
		//		if(a.iAttack==113) { //- SYNC
		//		//if(a.x==0) {
				a.iAttack=0;
				a.iLine=1;
				a.iMode=1;
		//		}
			}
			if(a.iAttack>20) { // only consider an attack after the 1st 20 steps of animation
				if(Math.random()>0.991) {
					Bomb bb = new Bomb();
					bb.x=a.x;
					bb.y=a.y;
					bombs.add(bb);
				}
			}
			
		}
		for (Bomb b: bombs) {
			if(b.bDropped) {
				b.moveBomb();
				// System.out.println(s.y);
			};
		}
		// Check for collision (shots and bombs)
		for (Shot s: shots) {
			if(s.bFired) {			
				//gc.drawRect(s.x, s.y, s.width, s.height);
				//gc.drawRect(a.x, a.y, a.width, a.height);
				if (a.intersects(s) && a.iMode!=2 && a.iMode!=0) { // not already exploding
					if(a.iMode==1) iScore=iScore+100; // Arriving - 100
					if(a.iMode==3) {
						iScore=iScore+50; // Idle - 50
						iArrivingAliens++;
					}
					a.iMode=2; //Collision Check
					a.iExplode=124; // Allows alien to explode
					s.bFired=false;
					iActiveShots--;
					//alienSpeed--; peculiar results!
					play(soundKill);
					break;
				 }
			}
		}
		for (Bomb b: bombs) {
			if(b.bDropped) {			
				if (b.intersects(ship)) {
					//gc.sleep(1000);
					b.bDropped=false;
					bShipExplode=true;
					iShipsLeft--;
					play(soundExplode);
					gc.setColor(Color.RED);
					gc.fillOval(b.x+b.iOffsetX, b.y+b.iOffsetY, 40, 40);
				 }
			}
		}
		
	}
	
	void showSprite(int iRow,int iCol,int iFlipH, int iFlipV,int iX, int iY) {
		BufferedImage spriteImage;
		boolean bShrink=false;
		if(iRow==99) {
			iRow=0;
			bShrink=true;
		}
		if(bTrace)System.out.println("iX="+iX+" iY="+iY+" iCol="+iCol+" iRow="+iRow);
		
		iX=Math.abs(iX);
		iY=Math.abs(iY);
		if(iCol==0) iCol=1;
		//if(iRow==0)iRow=1;
		
		
		if(iCol<9) {
			spriteImage = imgSprites.getSubimage(1+(iCol-1)*18,iRow*18+1,16,16);
		} else {
			if(iCol<17) {
				spriteImage = imgSprites.getSubimage(1+(iCol-1)*18-(iCol-9),iRow*18+1,32,32);
			} else {
				spriteImage = imgSprites.getSubimage(1+(iCol-1)*18-(iCol-17),iRow*18+1,32,32);
			}
		}
		// http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/Fliptheimagehorizontally.htm
		if(iFlipH==1) {
			  AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			  tx.translate(-spriteImage.getWidth(null), 0);
			  AffineTransformOp op = new AffineTransformOp(	tx,
					  										AffineTransformOp.TYPE_NEAREST_NEIGHBOR
					  				 );
			  spriteImage = op.filter(spriteImage, null);
		}
		if(iFlipV==1) {
			  AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			  tx.translate(0,-spriteImage.getWidth(null));
			  AffineTransformOp op = new AffineTransformOp(	tx,
					  										AffineTransformOp.TYPE_NEAREST_NEIGHBOR
					  				 );
			  spriteImage = op.filter(spriteImage, null);
		}
		Image spriteBigImage = null;
		if(!bShrink) {
			if(iCol<9) {
				spriteBigImage = spriteImage.getScaledInstance(58, 58, spriteImage.TYPE_BYTE_INDEXED);
			} else {
				spriteBigImage = spriteImage.getScaledInstance(116, 116, spriteImage.TYPE_BYTE_INDEXED);
			}
		} else {
			spriteBigImage = spriteImage.getScaledInstance(40, 40, spriteImage.TYPE_BYTE_INDEXED);
		}
		gc.drawImage(spriteBigImage,iX,iY);
	}
	
	void fireShot() {
		// detect trigger
		if(!initShot&&!bShipExplode) {
			if(gc.isKeyDown('S')) {
				if(bTrace)System.out.println("Shot fired");
				if(iActiveShots<iMaxShots) {
					Shot s = new Shot();
					shots.add(s);
					iActiveShots++;
					initShot=true;
				}
			}
		}else {
			if(gc.getKeyCode()==0) initShot=false;
		}
		for (Shot s: shots) {
			if(s.bFired) {
				s.moveShot();
				// System.out.println(s.y);
			};
		}
	}
	
	
	void drawStars(){
		//Draw stars
		iStarTimer++;
		if(iStarTimer<25) {
			for (int i = 0; i < 20; i++) {   //one way of looping through an arraylist
				gc.setColor(starColors.get(i));
				Point p = stars.get(i);
				gc.fillRect(p.x, p.y, 4,8);
			}
		}else {
			if(iStarTimer<50) {
				for (int i = 11; i < 40; i++) {   //one way of looping through an arraylist
					gc.setColor(starColors.get(i));
					Point p = stars.get(i);
					gc.fillRect(p.x, p.y, 4,8);
				}
			}else {
				if(iStarTimer<75) {
					for (int i = 21; i < 60; i++) {   //one way of looping through an arraylist
						gc.setColor(starColors.get(i));
						Point p = stars.get(i);
						gc.fillRect(p.x, p.y, 4,8);
					}
				}else {
					for (int i = 31; i < 80; i++) {   //one way of looping through an arraylist
						gc.setColor(starColors.get(i));
						Point p = stars.get(i);
						gc.fillRect(p.x, p.y, 4,8);
					}
					if(iStarTimer==100) iStarTimer=0;
				}
			}
		}
	}
	
	void moveStars() {
		for (Point p : stars) {                        //use a for-each loop to loop through arraylist
			p.y -= BGSPEED; //change star position
			//if (p.y < 0) p.y = WINH + 10; //  HEY - This is one set of repeating stars!
			if (p.y < 0) {
				p.x = rand.nextInt(WINW);
				p.y = WINH + 10; //  HEY - This is one set of repeating stars!
			}	
		}
	}
	
	void showHighScore() {
		gc.setColor(Color.RED);
		gc.drawString("1UP                              HIGH SCORE", 20, 30);
		gc.setColor(Color.WHITE);
		gc.drawString("                                              20000", 20, 60);
	}
	
	void showScore() {
		showHighScore();
		String sScore="00000"+iScore;
		gc.drawString(sScore.substring(sScore.length()-5), 20, 60);
	}
	
	void showBottomRow() {
		gc.setColor(Color.BLACK);
		gc.fillRect(0, WINH-25, WINW, 25);
		if(bGameOver) {
			gc.setColor(Color.WHITE);
			gc.drawString("CREDITS  "+iCredits, 0, WINH);
		}else { // show all the ships that are left
			for(int i=0; i<iShipsLeft; i++) {
				showSprite(99,7,0,0,20+i*42,WINH-40);
			}
		}
	}

	void showPushStart() {
		gc.setColor(Color.CYAN);
		gc.drawString("PUSH   START   BUTTON", 220, 400);
		gc.setColor(Color.YELLOW);
		gc.drawString("1ST   BONUS   FOR   20000   PTS", 160, 470);
		gc.drawString("2ND   BONUS   FOR   70000   PTS", 160, 540);
		gc.drawString("AND   FOR   EVERY   70000   PTS", 160, 610);
	
		gc.setColor(Color.WHITE);
		gc.drawString("1981   NAMCO   LTD", 250, 700);
		showSprite(0,7,0,0,80,420);
		showSprite(0,7,0,0,80,495);
		showSprite(0,7,0,0,80,570);
		
		
	}
	
	
	void explodeShip() {
		if(bShipExplode) {
			if(iShipExplode<100) {
				iShipExplode+=2;
				//gc.fillOval(ship.x-(int)(iShipExplode*10*0.5),ship.y-(int)(iShipExplode*10*0.5),iShipExplode*10,iShipExplode*10);
				if(iShipExplode<25) {
					showSprite(0,9,0,0,ship.x-28,ship.y-31);
				} else {
					if(iShipExplode<50) {
						showSprite(0,11,0,0,ship.x-28,ship.y-31);
					} else {
						if(iShipExplode<75) {
							showSprite(0,13,0,0,ship.x-28,ship.y-31);
						} else {
							showSprite(0,15,0,0,ship.x-28,ship.y-31);
						}
					}
				}
			}else {
				//bShipExplode=false;
				if(iShipsLeft<1)bGameOver=true;
				iShipExplode++;
				if(iShipExplode<250) {
					if(!bGameOver) {
						gc.drawString("GET READY!", 300, 700);
					}else {
						gc.drawString("GAME OVER", 300, 700);
					}
				}else {
					bShipExplode=false;
					iShipExplode=0;
					if(bGameOver)bStartOver=true;
				}
				
			}
		}
	}
	
	public void play(File soundFile) {
		  try  {
		    clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
		    clip.addLineListener(new LineListener() {
		      @Override
		      public void update(LineEvent event) {
		        if (event.getType() == LineEvent.Type.STOP)
		          clip.close();
		      }
		    });
		    //File soundFile = new File( "DotsSound.wav" ); // Must be in root of PROJECT folder (Ex Demo HSA2)
		    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( soundFile );
		    clip = AudioSystem.getClip();
		    clip.open(AudioSystem.getAudioInputStream(soundFile));
		    //System.out.print("Loading Clip...");
		    gc.sleep(1); //time to load the sound
		    if(bSound) {
		    	clip.start();
		    };
		  }
		  catch (Exception exc) {
			System.out.println("Error");
		    exc.printStackTrace(System.out);
		  }
	}
	
	void initAlienHomes() {
		// Armada one
		alienHomes.add(new Point(5,2)); aType[5][2]=4;
		alienHomes.add(new Point(6,2)); aType[6][2]=4;
		alienHomes.add(new Point(5,3)); aType[5][3]=4;
		alienHomes.add(new Point(6,3)); aType[6][3]=4;
		alienHomes.add(new Point(5,4));	aType[5][4]=5;
		alienHomes.add(new Point(6,4));	aType[6][4]=5;
		alienHomes.add(new Point(5,5));	aType[5][5]=5;
		alienHomes.add(new Point(6,5));	aType[6][5]=5;
		
		// Armada two
		alienHomes.add(new Point(4,1)); aType[4][1]=2;
		alienHomes.add(new Point(5,1)); aType[5][1]=2;
		alienHomes.add(new Point(6,1)); aType[6][1]=2;
		alienHomes.add(new Point(7,1)); aType[7][1]=2;
		alienHomes.add(new Point(4,2)); aType[4][2]=4;
		alienHomes.add(new Point(4,3)); aType[4][3]=4;
		alienHomes.add(new Point(7,2)); aType[7][2]=4;
		alienHomes.add(new Point(7,3)); aType[7][3]=4;
		
		// Armada three
		alienHomes.add(new Point(2,2)); aType[2][2]=4;
		alienHomes.add(new Point(3,2)); aType[3][2]=4;
		alienHomes.add(new Point(2,3)); aType[2][3]=4;
		alienHomes.add(new Point(3,3)); aType[3][3]=4;
		alienHomes.add(new Point(8,2)); aType[8][2]=4;
		alienHomes.add(new Point(9,2)); aType[9][2]=4;
		alienHomes.add(new Point(8,3)); aType[8][3]=4;
		alienHomes.add(new Point(9,3)); aType[9][3]=4;
	
		
		// Armada four
		alienHomes.add(new Point(3,4));aType[3][4]=5;
		alienHomes.add(new Point(4,4));aType[4][4]=5;
		alienHomes.add(new Point(3,5));aType[3][5]=5;
		alienHomes.add(new Point(4,5));aType[4][5]=5;
		alienHomes.add(new Point(7,4));aType[7][4]=5;
		alienHomes.add(new Point(8,4));aType[8][4]=5;
		alienHomes.add(new Point(7,5));aType[7][5]=5;
		alienHomes.add(new Point(8,5));aType[8][5]=5;
	
		// Armada five
		alienHomes.add(new Point(1,4));aType[1][4]=5;
		alienHomes.add(new Point(2,4));aType[2][4]=5;
		alienHomes.add(new Point(1,5));aType[1][5]=5;
		alienHomes.add(new Point(2,5));aType[2][5]=5;
		alienHomes.add(new Point(9,4));aType[9][4]=5;
		alienHomes.add(new Point(10,4));aType[10][4]=5;
		alienHomes.add(new Point(9,5));aType[9][5]=5;
		alienHomes.add(new Point(10,5));aType[10][5]=5;
	}
	
	void loadHighScores() throws IOException {
		int j=0;
		URL url=null;
		try {
			url = new URL("http://javabeehome.com/HiScores.txt");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		BufferedReader read = new BufferedReader(
		new InputStreamReader(url.openStream()));
		
		String i;
        while ((i = read.readLine()) != null) {
        	j++;
        	sHiScores[j]=i;
        }
        read.close();
		
		
	}
	
	
	void showHighScores() {
		
		String[] arrPrefix= {"", "1ST", "2nd", "3rd", "4th", "5th"};
		
		gc.setColor(Color.BLUE);
		gc.drawString("THE   GALACTIC   HEROES", 140, 210);
		gc.setColor(Color.RED);
		gc.drawString("BEST 5", 290, 420);
		gc.drawLine(230,406,270,406);
		gc.drawLine(430,406,470,406);
		gc.setColor(Color.CYAN);
		gc.drawString("SCORE             NAME", 270, 520);
		
		
		for(int q=1;q<6;q++) {
			gc.drawString(arrPrefix[q]+"          "+sHiScores[q].replace(",", "            "), 130, 520+q*50);
		}
	}
	
	
	void loadImage(String imageFile){
		try { // must be wrapped in a 'try' statement
			imgLoaded = ImageIO.read(getClass().getClassLoader().getResource(imageFile)); // .jpg must be in 'bin' folder
		}catch(IOException ex) {
			System.out.println("Image cannot be loaded");
		}
	
	}
	void scrambleImage(String imageFile,int iMSecs){
		try { // must be wrapped in a 'try' statement
			imgLoaded = ImageIO.read(getClass().getClassLoader().getResource(imageFile)); // .jpg must be in 'bin' folder
		}catch(IOException ex) {
			System.out.println("Image cannot be loaded");
		}
		for(int j=0;j<(iMSecs/30);j++) {
			BufferedImage imgSpriteImage;
			
			int iC,iR=0;
			for(int k=0;k<500;k++) {
				synchronized(gc) {			
					//System.out.println(k);
					iC=(int) Math.round(Math.random()*14.0);
					iR=(int) Math.round(Math.random()*19.0);
					imgSpriteImage = imgLoaded.getSubimage(1+(iC)*50,iR*50+1,90,90);
					iC=(int) Math.round(Math.random()*14.0);
					iR=(int) Math.round(Math.random()*19.0);
					gc.drawImage(imgSpriteImage,1+(iC)*50,iR*50+1);
				}
			}
			gc.sleep(15);
		}
		gc.sleep(500);
	}
	
	
	void getSQL() {
	  Statement stmt = null;
	  ResultSet rs = null;
	  try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            
            Connection conn = null;
            
            
            conn = DriverManager.getConnection("jdbc:mysql://javabeehome/hnac7307_MyScores?" +
                                               "user=hnac7307_JavaB&password=itisaperiodofcivilwar");
            System.out.println("hi");
            System.out.println("*** "+conn.getCatalog());
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT Initials FROM hnac7307_MyScores");

            //System.out.println(rs.Initials);            
            rs.close();
    
       } catch (Exception ex) {
           // handle the error
    	   System.out.println("SQLException: " + ex.getMessage()); 
    	   System.out.println("SQLException: " +ex.toString()); 
    	      
       
       }	
	  
		
	}
}