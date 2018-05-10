package models;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

public class Scout {
	public Scout(Unit u) {
		unit = u;
	}
	public Unit unit;
	public Position currentTarget;
}
