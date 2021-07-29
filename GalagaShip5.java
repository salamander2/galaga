package galaga;


import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import hsa2.GraphicsConsole;

public class GalagaShip5 {

	public static void main(String[] args) {
		new GalagaShip5();
	}
	
	static final int WINW 		= 1024;
	static final int WINH 		= 1024;
	static final int SHOTSPEED	= 85;
	
	
	static GraphicsConsole gc = new GraphicsConsole (WINW,WINH, "Galaga Ship");
	static BufferedImage imgSprites=null;
	
	ArrayList<Alien2> aliens = new ArrayList<Alien2>();	
	ArrayList<Shot> shots = new ArrayList<Shot>();
	
	static Rectangle ship = new Rectangle(WINW/2, WINH-80,30,30);
	static Rectangle shot = new Rectangle(WINW/2, WINH-80,16,18);
	int shipSpeed = 16;
	int shotSpeed = 24;
	boolean initShot = false;
	
	int iFireX=0;
	int iFireY=0;
	static boolean bFired=false;
	
	
	GalagaShip5() {
		setup();
		while(true) {
			calcGraphics();
			drawGraphics();
			//gc.sleep(1000);
		}
	}
	
	public class Shot extends Rectangle {
		
		boolean bFired=true;
		
		Shot() {
			System.out.println("init shot "+ship.x+","+ship.y);
			x=ship.x;
			y=ship.y;
			height=30;
			width=30;
			bFired=true;
	
		}
		public void moveShot() {
			//System.out.println("move shot");
			x=x+1;
			y=y-shotSpeed;
			if(y<0) {
				bFired=false;
			}
		}
		public void drawShot() {
			//System.out.println("draw shot "+x+","+y);
			showSprite(	0,8,0,0,x, y);		
		}
	}
	
	void setup() {
		gc.setBackgroundColor(Color.black);
		gc.clear();
		loadSpriteSheet();
		
		for(int j=1;j<9;j++) {
			Alien2 a=	new Alien2(j);
			aliens.add(a);	
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
			for (Alien2 a: aliens) {
				alienGo(a);
			}
			fireShot();
	}
	
	void moveShip() {
		if (gc.isKeyDown('A')) {
			ship.x -=shipSpeed;
			if (ship.x < 0) ship.x = 0;
		}
		if (gc.isKeyDown('D')) {
			//System.out.println("Move Right!");
			ship.x +=shipSpeed;
			if (ship.x + ship.width > WINW-25) ship.x = WINW-ship.width-25; 
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
					s.drawShot();
				};
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
					break;
				}
			}
		}
	}
	
	static void showSprite(int iRow,int iCol,int iFlipH, int iFlipV,int iX, int iY) {
		BufferedImage spriteImage;
		spriteImage = imgSprites.getSubimage(1+(iCol-1)*18,iRow*18+1,16,16);
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
		if(!initShot) {
			if(gc.isKeyDown('S')) {
				System.out.println("Shot fired");
				Shot s = new Shot();
				shots.add(s);
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
}