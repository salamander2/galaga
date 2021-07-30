package galaga;

import java.awt.Rectangle;

public class Bomb extends Rectangle {
	
	boolean bDropped=true;
	int bombSpeed = 4;
	
	Bomb(Rectangle ship, Alien alien) {
		x=alien.x;
		y=alien.y;
		height=30;
		width=30;
		bDropped=true;
	}
	
	void moveBomb() {
		//System.out.println("move shot");
		//x=x+1;
		y += bombSpeed;
		if(y>Galaga5.WINH) {
			bDropped=false;
		}
	}	
}
