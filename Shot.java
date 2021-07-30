package galaga;

import java.awt.Rectangle;

public class Shot extends Rectangle {
	
	boolean bFired=true;
	int shotSpeed = 22;
	
	Shot(Rectangle ship) {		
		x=ship.x;
		y=ship.y;
		height=30;
		width=30;
		bFired=true;

	}
	void moveShot() {		
		y -= shotSpeed;
		if(y<0) {
			bFired=false;
		}
	}
}
