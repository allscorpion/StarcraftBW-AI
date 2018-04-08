package models;

import bwapi.Unit;

public class Worker {
	public Worker(Unit u) {
		unit = u;
	}
	public Unit unit;
	public Unit miningFrom;
	public boolean isBuilding;
	public boolean isScout;
}
