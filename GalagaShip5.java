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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import hsa2.GraphicsConsole;

public class GalagaShip5 {

	public static void main(String[] args) {
		new GalagaShip5();
	}

	static final int WINW 		= 1024;
	//static final int WINH 		= 1024; TOO TALL FOR My SCREEN
	static final int WINH 		= 900;

	Random rand = new Random();

	GraphicsConsole gc = new GraphicsConsole (WINW,WINH, "Galaga Ship");
	static BufferedImage imgSprites=null;

	ArrayList<Alien2> aliens = new ArrayList<Alien2>();	
	ArrayList<Shot> shots = new ArrayList<Shot>();
	ArrayList<Bomb> bombs = new ArrayList<Bomb>();

	Rectangle ship = new Rectangle(WINW/2, WINH-80,30,30);

	int shipSpeed = 16;		
	boolean initShot = false;

	int iFireX=0;
	int iFireY=0;
	static boolean bFired=false;

	// Starfield Init
	int tk=0; // tick counter
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

	GalagaShip5() {
		setup();
		while(true) {
			calcGraphics();
			drawGraphics();
			gc.sleep(1);
		}
	}

	void setup() {
		gc.setBackgroundColor(Color.black);
		gc.clear();
		loadSpriteSheet();

		for(int j=1;j<9;j++) {
			Alien2 a =	new Alien2(j);
			aliens.add(a);	
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
		//TODO: fix error if file not found. The program does not print error message.
		try {
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("ARCADECLASSIC.TTF")));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Load Font
		Font font = new Font("ARCADECLASSIC", Font.BOLD, 40); 
		gc.setFont(font);


	}

	void loadSpriteSheet() {
		try { // must be wrapped in a 'try' statement
			imgSprites = ImageIO.read(getClass().getClassLoader().getResource("Sprites.png"));
		}catch(IOException ex) {
			System.out.println("Image cannot be loaded");
			System.exit(0);
		}
		if (imgSprites == null) {
			System.out.println("Image cannot be loaded");
			System.exit(0);
		}
	}

	void calcGraphics() {		

		moveShip();
		//moveShot();
		// Calc the aliens
		for (Alien2 a: aliens) {
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
			gc.clear();

			//	Draw the ship
			showSprite(0,7,0,0,ship.x,ship.y);

			// Draw all aliens
			for (Alien2 a: aliens) {
				//sTitle=sTitle+"iMode: "+a.iMode+" iAttack="+a.iAttack+", ";
				if(a.iMode==1 || a.iMode==3) { // if Arrival Mode OR Idle Mode
					showSprite(
							a.redAlien[a.iLine][1], // Sprite ID
							a.redAlien[a.iLine][2], // Rotation
							a.redAlien[a.iLine][3], // Flip X
							a.redAlien[a.iLine][4], // Flip Y
							a.x,
							a.y
							);
				}
				if(a.iMode==2) {
					gc.setColor(Color.YELLOW);
					gc.fillOval(a.x-(int)(0.5*a.iExplode*10),
							a.y-(int)(0.5*a.iExplode*10),
							a.iExplode*10, a.iExplode*10);
				}
				if(a.iMode==4) { // if Attack Mode	
					showSprite(
							a.redAlien[a.iLine][1], // Sprite ID (based on arrival data
							a.redAttack[a.iAttack][2], // Rotation
							a.redAttack[a.iAttack][3], // Flip X
							a.redAttack[a.iAttack][4], // Flip Y
							a.x, 
							a.y
							);
				}
				sTitle=sTitle+"iMode:"+a.iMode+", ";
			}
			for (Shot s: shots) {
				if(s.bFired) {
					//sTitle=sTitle+"s.x="+s.x+", s.y="+s.y;
					showSprite(	0,8,0,0,s.x, s.y);
				};
			}
			for (Bomb b: bombs) {
				if(b.bDropped) {
					//sTitle=sTitle+"s.x="+s.x+", s.y="+s.y;
					showSprite(	0,8,0,0,b.x, b.y);		
				}				
			}

		} //end of synchronized
		drawStars();
		showScore();

		if(bShipExplode) {
			if(iShipExplode<100) {
				iShipExplode+=2;
				gc.fillOval(ship.x-(int)(iShipExplode*10*0.5),ship.y-(int)(iShipExplode*10*0.5),iShipExplode*10,iShipExplode*10);
			}else {
				iShipExplode=0;
				bShipExplode=false;
			}
		}

		gc.setTitle(sTitle);
		gc.sleep(15);
	}

	public  void alienGo(Alien2 a) {
		if(a.iMode==0) { 						// Preparing for arrival
			a.iArrival--;
			if(a.iArrival==0) a.iMode=1; 	
		}

		if(a.iMode==1) { 						// Arrival
			a.iLine++;
			if (a.iLine>0&&a.iLine<79) {
				a.x=a.redAlien[a.iLine][5];
				a.y=a.redAlien[a.iLine][6];
				if(a.iLine==78) a.iMode=3;
				if(Math.random()>0.996) {
					Bomb bb = new Bomb(ship);
					bb.x=a.x;
					bb.y=a.y;
					bombs.add(bb);
				}
			}
		}

		if(a.iMode==2) { 						// Explode
			if(a.iExplode>0) {
				if(!(a.iExplode==0)) {
					gc.setColor(Color.YELLOW);
					gc.fillOval(a.x, a.y, a.iExplode*10, a.iExplode*10);
					//gc.sleep(20);
				}
				a.iExplode--;
			}else{
				a.iLine=1;
				a.iExplode=18;
				a.iMode=1;
			}
		}

		if(a.iMode==3) {						// Idle - Coinsider an attack
			if(Math.random()>0.996) a.iMode=4;
		}

		if(a.iMode==4) {						// Attack
			a.iAttack++;
			if (a.iAttack>0&&a.iAttack<115) { //-SYNC
				a.x=a.redAttack[a.iAttack][5]+a.redAlien[77][5]-165;  // offset atrack based on end of arrival data
				a.y=a.redAttack[a.iAttack][6]+a.redAlien[77][6]-170; // offset atrack based on end of arrival data

				if(a.iAttack==113) { //- SYNC
					a.iAttack=0;
					a.iLine=1;
					a.iMode=1;
				}
			}
			if(a.iAttack>20) {
				if(Math.random()>0.996) {
					Bomb bb = new Bomb(ship);
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
		// Check for collision
		for (Shot s: shots) {
			if(s.bFired) {			
				//System.out.println("Check for Y collision "+s.width+" close to "+a.width);
				//gc.setColor(Color.RED);
				//gc.fillOval(s.x, s.y, 40, 40);
				//gc.setColor(Color.GREEN);
				//gc.fillOval(a.x, a.y, 40, 40);
				//gc.sleep(10);
				//if(a.rAlien.intersects(s)) {
				if(Math.abs(a.x-s.x)<30 && Math.abs(a.y-s.y)<30) {
					//System.out.println("HIT!");
					//System.out.println("Alien "+a.x+", "+a.y+" width:"+a.width+" height:"+a.height);
					//System.out.println("Shot "+s.x+", "+s.y+" width:"+s.width+" height:"+s.height);
					//System.out.println(a.rAlien.intersects(s));
					//gc.sleep(3000);
					a.iMode=2; //Collision Check
					s.bFired=false;
					iScore=iScore+10;
					break;
				}
			}
		}

		// Check for bomb collision with ship
		for (Bomb b: bombs) {
			if(b.bDropped) {			
				if(Math.abs(b.x-ship.x)<30 && Math.abs(b.y-ship.y)<30) {
					b.bDropped=false;
					bShipExplode=true;
					break;
				}
			}
		}

	}

	void showSprite(int iRow,int iCol,int iFlipH, int iFlipV,int iX, int iY) {
		BufferedImage spriteImage = imgSprites.getSubimage(1+(iCol-1)*18,iRow*18+1,16,16);
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
		spriteBigImage = spriteImage.getScaledInstance(58, 58, spriteImage.TYPE_BYTE_INDEXED);
		gc.drawImage(spriteBigImage,iX,iY);
	}

	void fireShot() {
		// detect trigger
		if(!initShot&&!bShipExplode) {
			if(gc.isKeyDown('S') || gc.isKeyDown(' ')) {
				System.out.println("Shot fired");

				shots.add(new Shot(ship));
				initShot=true;
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
		if (tk%4==0) {
			iStarTimer++;
		}
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
	
	void showScore() {
		gc.setColor(Color.RED);
		gc.drawString("1UP", 20, 30);
		gc.setColor(Color.WHITE);
		String sScore="00000"+iScore;
		gc.drawString(sScore.substring(sScore.length()-5), 20, 60);
	}
}