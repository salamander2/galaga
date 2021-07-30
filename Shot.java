package galaga;

import java.awt.Rectangle;

public class Shot extends Rectangle {
	
	boolean bFired=true;
	int shotSpeed = 24;
	
	Shot(Rectangle ship) {
		//System.out.println("init shot "+ship.x+","+ship.y);
		x=ship.x;
		y=ship.y;
		height=30;
		width=30;
		bFired=true;

	}
	void moveShot() {
		//System.out.println("move shot");
		x=x+1;
		y=y-shotSpeed;
		if(y<0) {
			bFired=false;
		}
	}
//	public void drawShot() {
//		//System.out.println("draw shot "+x+","+y);
//		showSprite(	0,8,0,0,x, y);		
//	}
}
