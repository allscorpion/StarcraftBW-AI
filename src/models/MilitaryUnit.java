package models;

import bwapi.Position;
import bwapi.Unit;
import helpers.StarCraftInstance;

public class MilitaryUnit {
	public MilitaryUnit (Unit u) {
		unit = u;
	}
	public void AttackUnit(Unit u) {
		unit.attack(u);
		LastOrderFrame = StarCraftInstance.game.getFrameCount();
	}
	public void AttackPosition(Position p) {
		unit.attack(p);
		LastOrderFrame = StarCraftInstance.game.getFrameCount();
	}
	public Unit unit;
	public int LastOrderFrame;
}
