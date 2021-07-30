package galaga;

import java.awt.Rectangle;

public class Bomb extends Rectangle {
	
	boolean bDropped=true;
	int bombSpeed = 1;
	
	Bomb(Rectangle ship) {
		//System.out.println("init shot "+ship.x+","+ship.y);
		x=ship.x;
		y=0;
		height=30;
		width=30;
		bDropped=true;

	}
	
	void moveBomb() {
		//System.out.println("move shot");
		//x=x+1;
		y=y+bombSpeed;
		if(y>GalagaShip5.WINH) {
			bDropped=false;
		}
	}
	
}
