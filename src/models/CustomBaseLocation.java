package models;

import java.util.List;

import bwapi.Unit;
import bwta.BaseLocation;

public class CustomBaseLocation {
	public CustomBaseLocation(BaseLocation bl) {
		baseLocation = bl;
	}
	public BaseLocation baseLocation;
	public CommandCenter commandCenter;
}
