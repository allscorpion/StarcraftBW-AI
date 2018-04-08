package models;

import bwapi.Unit;

public class Mineral {
	public Mineral(Unit m) {
		node = m;
	}
	public Unit node;
	public int amountOfWorkersAssignedToMineral;
}
