package models;

import java.util.List;

import bwapi.Unit;

public class CommandCenter {
	public CommandCenter(Unit u) {
		unit = u;
	}
	public Unit unit;
	public boolean hasGasStructure;
	public Unit gasStructure;
	public boolean allowOversaturation;
	public boolean isMineralFieldStartedToDeplete;
}
