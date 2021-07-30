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

public class Galaga5 {

	public static void main(String[] args) {
		new Galaga5();
	}

	static final int WINW 		= 1024;
	//static final int WINH 		= 1024; TOO TALL FOR My SCREEN
	static final int WINH 		= 900;

	Random rand = new Random();

	GraphicsConsole gc = new GraphicsConsole (WINW,WINH, "Galaga Ship");
	static BufferedImage imgSprites=null;

	ArrayList<Alien> aliens = new ArrayList<Alien>();	
	ArrayList<Shot> shots = new ArrayList<Shot>();
	ArrayList<Bomb> bombs = new ArrayList<Bomb>();

	Rectangle ship = new Rectangle(WINW/2, WINH-80,30,30);

	int shipSpeed = 16;		
	boolean initShot = false;

	static boolean bFired=false;

	// Starfield Init
	static final int NUMSTARS 	= 80;
	static int 		 BGSPEED 	= 5;
	ArrayList<Point> stars = new ArrayList<Point>();
	ArrayList<Color> starColors = new ArrayList<Color>();

	//Scoring and Other Text Display in TTF font 
	int iScore=0;
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

	// Ship Explode
	boolean bShipExplode=false;
	int iShipExplode=0;

	Galaga5() {
		setup();
		while(true) {
			calcGraphics();
			drawGraphics();
			//Debug
			gc.setTitle("Shots:"+shots.size() + "    Bombs:"+ bombs.size());
			gc.sleep(15);
		}
	}

	void setup() {
		gc.setBackgroundColor(Color.black);
		gc.clear();
		loadSpriteSheet();

		//The numbers are used to load specific flight patterns
		for(int j=1;j<9;j++) {
			aliens.add(new Alien(j));	
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
			imgSprites = ImageIO.read(getClass().getClassLoader().getResource("sprites.png"));
		}catch(IOException | IllegalArgumentException ex) {
			System.out.println("Image cannot be loaded");
			System.exit(0);
		}		
	}

	void calcGraphics() {	
		moveShip();
		fireAndMoveShot();

		// Calc the aliens
		for (Alien a: aliens) {
			alienGo(a);
		}
		moveAndExplodeBombs();
		moveStars();
	}

	void moveShip() {
		if(bShipExplode) return;

		if (gc.isKeyDown('A')) {
			ship.x -= shipSpeed;
			if (ship.x < 0) ship.x = 0;
		}
		if (gc.isKeyDown('D')) {
			ship.x +=shipSpeed;
			if (ship.x + ship.width > WINW-25) ship.x = WINW-ship.width-25; 
		}
	}

	void drawGraphics() {

		synchronized(gc) {
			gc.clear();

			//	Draw the ship
			showSprite2(0,7,0,0,ship.x,ship.y);

			// Draw all aliens
			for (Alien a: aliens) {
				//sTitle=sTitle+"iMode: "+a.iMode+" iAttack="+a.iAttack+", ";
				if(a.iMode==Alien.MODE_ARRIVE || a.iMode==Alien.MODE_IDLE) { 
					showSprite2(
							a.redAlien[a.iLine][0], // Sprite ID
							a.redAlien[a.iLine][1], // Rotation
							a.redAlien[a.iLine][2], // Flip X
							a.redAlien[a.iLine][3], // Flip Y
							a.x,
							a.y
							);
				}
				if(a.iMode==Alien.MODE_EXPLODE) {
					gc.setColor(Color.YELLOW);
					gc.fillOval(a.x-(int)(0.5*a.iExplode*10), a.y-(int)(0.5*a.iExplode*10), a.iExplode*10, a.iExplode*10);
				}
				if(a.iMode==Alien.MODE_ATTACK) { 	
					showSprite2(
							a.redAlien[a.iLine][0], // Sprite ID (based on arrival data)
							a.redAttack[a.iAttack][1], // Rotation
							a.redAttack[a.iAttack][2], // Flip X
							a.redAttack[a.iAttack][3], // Flip Y
							a.x, 
							a.y
							);
				}
				//sTitle=sTitle+"iMode:"+a.iMode+", ";
			}
			for (Shot s: shots) {
				if(s.bFired) {
					showSprite2(0,8,0,0,s.x, s.y);
				};
			}
			for (Bomb b: bombs) {
				if(b.bDropped) {
					showSprite2(0,8,0,0,b.x, b.y);		
				}				
			}		
			drawStars();
			showScore();
		} //end of synchronized

		if(bShipExplode) {
			if(iShipExplode<100) {
				iShipExplode+=2;
				gc.fillOval(ship.x-(int)(iShipExplode*10*0.5),ship.y-(int)(iShipExplode*10*0.5),iShipExplode*10,iShipExplode*10);
			}else {
				iShipExplode=0;
				bShipExplode=false;
			}
		}
	}

	public  void alienGo(Alien a) {
		if(a.iMode==Alien.MODE_PREP) {	// Preparing for arrival
			a.iArrival--;
			if(a.iArrival==0) a.iMode=Alien.MODE_ARRIVE; 	
		}

		if(a.iMode==Alien.MODE_ARRIVE) {	// Arrival
			a.iLine++;
			if (a.iLine>0 && a.iLine<79) {  //why these numbers?
				a.x=a.redAlien[a.iLine][4];
				a.y=a.redAlien[a.iLine][5];
				if(a.iLine==78) a.iMode=Alien.MODE_IDLE;
				if(Math.random()>0.996) {					
					bombs.add(new Bomb(ship, a));
				}
			}
		}

		if(a.iMode==Alien.MODE_EXPLODE) {
			if(a.iExplode>0) {
				a.iExplode--;
			}else{
				a.iLine=1;
				a.iExplode=18;
				a.iMode=Alien.MODE_ARRIVE;
			}
		}

		if(a.iMode==Alien.MODE_IDLE) {	// Idle - Coinsider an attack
			if(Math.random()>0.996) a.iMode=Alien.MODE_ATTACK;
		}

		if(a.iMode==Alien.MODE_ATTACK) {
			a.iAttack++;
			if (a.iAttack>0&&a.iAttack<115) { //-SYNC
				a.x=a.redAttack[a.iAttack][4]+a.redAlien[77][4]-165;  // offset atrack based on end of arrival data
				a.y=a.redAttack[a.iAttack][5]+a.redAlien[77][5]-170; // offset atrack based on end of arrival data

				if(a.iAttack==113) { //- SYNC
					a.iAttack=0;
					a.iLine=1;
					a.iMode=Alien.MODE_ARRIVE;
				}
			}
			if(a.iAttack>20) {
				if(Math.random()>0.996) {
					bombs.add(new Bomb(ship, a));
				}
			}

		}


		// Check for collision
		for (Shot s: shots) {
			if(s.bFired) {			
				if(a.intersects(s)) {
					a.iMode=Alien.MODE_EXPLODE;
					s.bFired=false;
					iScore=iScore+10;
					break;
				}
			}
		}



	}

	void moveAndExplodeBombs() {

		for (Bomb b: bombs) {
			if(b.bDropped) {
				b.moveBomb();

				if (b.intersects(ship)) {
					//TODO: does this remove the bomb or do they just continue accumulating
					b.bDropped=false;
					bShipExplode=true;
					break;
				}
			}
		}
	}

	//Replaced by showSprite2()
	void showSprite(int iRow,int iCol,int iFlipH, int iFlipV,int iX, int iY) {
		BufferedImage spriteImage = imgSprites.getSubimage(1+(iCol-1)*18,iRow*18+1,16,16);

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

	/* Draw and flip sprites using DrawImage.
	 * 
	 * Spritesheet: 
	 * Images for ship and aliens are 16x16 with a 2 pixel border between then (how stupid!) and a 1 pixel offset on top and left
	 * Colums 1-7 are for various orientations. Columns 7,8 are for animating the sprite
	 * The first two rows are the ship, after that come various aliens etc.  The ship is not animated.
	 * Destination images are 58x58.
	 * Other images like the explosions are bigger than 16x16 so this method will not work for them.
	 * 
	 * drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, observer)
	  	img the specified image to be drawn. This method does nothing if img is null.
		dx1 the x coordinate of the first corner of the destination rectangle.
		dy1 the y coordinate of the first corner of the destination rectangle.
		dx2 the x coordinate of the second corner of the destination rectangle.
		dy2 the y coordinate of the second corner of the destination rectangle.
		sx1 the x coordinate of the first corner of the source rectangle.
		sy1 the y coordinate of the first corner of the source rectangle.
		sx2 the x coordinate of the second corner of the source rectangle.
		sy2 the y coordinate of the second corner of the source rectangle.
		observer object. It is STRONGLY recommended to set this to NULL 
	 */
	void showSprite2(int iRow,int iCol,int iFlipH, int iFlipV,int iX, int iY) {		
		//if no flipping
		if (iFlipV + iFlipH == 0) {
			gc.drawImage(imgSprites, iX,iY,iX+58,iY+58, 1+(iCol-1)*18,1+iRow*18,1+(iCol-1)*18+16,1+iRow*18+16, null);
		}
		if(iFlipH==1) {
			gc.drawImage(imgSprites, iX+58,iY,iX,iY+58, 1+(iCol-1)*18,1+iRow*18,1+(iCol-1)*18+16,1+iRow*18+16, null);
		} 
		if (iFlipV==1) {
			gc.drawImage(imgSprites, iX,iY+58,iX+58,iY, 1+(iCol-1)*18,1+iRow*18,1+(iCol-1)*18+16,1+iRow*18+16, null);			
		}
	}

	void fireAndMoveShot() {
		// detect trigger
		if(!initShot&&!bShipExplode) {
			if(gc.isKeyDown('S') || gc.isKeyDown(' ')) {
				shots.add(new Shot(ship));
				initShot=true;
			}
		}else {
			if(gc.getKeyCode()==0) initShot=false;
		}
		for (Shot s: shots) {
			if(s.bFired) s.moveShot();			
		}
	}

	void drawStars(){
		for (int i = 0; i < stars.size(); i++) {  
			gc.setColor(starColors.get(i));
			Point p = stars.get(i);
			gc.fillRect(p.x, p.y, 4,8);
		}
	}


	void moveStars() {
		for (Point p : stars) {
			p.y += BGSPEED; 

			//so it will work moving up or down
			if (BGSPEED > 0) {
				if (p.y > WINH) {
					p.x = rand.nextInt(WINW);
					p.y = -10; //  HEY - This is one set of repeating stars!
				}	
			} else {
				if (p.y < 0) {
					p.x = rand.nextInt(WINW);
					p.y = WINH + 10; //  HEY - This is one set of repeating stars!
				}
			}
		}
	}

	void showScore() {
		gc.setColor(Color.RED);
		gc.drawString("1UP", 20, 30);
		gc.setColor(Color.WHITE);
		String sScore=String.format("%05d",iScore);
		gc.drawString(sScore, 20, 60);
	}
}