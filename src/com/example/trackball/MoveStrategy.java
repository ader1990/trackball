package com.example.trackball;

import java.util.ArrayList;

public class MoveStrategy {
	private float centerX;
	private float centerY;
	private int steps = 51;
	private float maxMotorSpeed=255;

	public MoveStrategy(float centerX, float centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
	}

	public ArrayList<Move> getMoves(float positionX, float positionY) {
		
		ArrayList<Move> moves = new ArrayList<Move>();
		float positionXCentered = positionX - centerX;
		float positionYCentered = positionY - centerY;
		float speedX=(int)((positionXCentered*maxMotorSpeed)/(centerX));
		int speedGearX=(int)(speedX/steps);
		float speedY=(int)((positionYCentered*maxMotorSpeed)/(centerY));
		int speedGearY=(int)((speedY/steps));
		Move move;
		
		if (speedGearY > 0) {
			move = new MoveBack(speedGearY);
			moves.add(move);
		}else {
			move = new MoveFront(-speedGearY);
			moves.add(move);
		}
		if (speedGearX < 0) {
			move = new MoveLeft(-speedGearX);
			moves.add(move);
		}
		else {
			move = new MoveRight(speedGearX);
			moves.add(move);
		}
		return moves;
	}

}
