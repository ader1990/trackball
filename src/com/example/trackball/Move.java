package com.example.trackball;

import java.security.InvalidAlgorithmParameterException;

public abstract class Move {
	private int maxValue = 255;
	private int value = 0;

	public Move(int value) throws InvalidAlgorithmParameterException {
		if (checkValue(value))
			this.value = Math.abs(value);
		else
			throw new InvalidAlgorithmParameterException();
	}

	private boolean checkValue(Object value2) {
		// TODO Auto-generated method stub
		return maxValue >= ((Integer) value2);
	}

	public Move(float value) {
		this.value = (int) value;
	}

	public int getValue() {
		return this.value;
	}
	public String toString() {
		return getName()+" "+getValue();
	}

	public abstract String getName();
}
