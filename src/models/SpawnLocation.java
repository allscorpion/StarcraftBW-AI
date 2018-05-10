package models;

import bwta.BaseLocation;

public class SpawnLocation {
	public SpawnLocation(BaseLocation bl) {
		init(bl, false);
	}
	public SpawnLocation(BaseLocation bl, boolean _isMySpawn) {
		init(bl, _isMySpawn);
	}
	public void init(BaseLocation bl, boolean _isMySpawn) {
		baseLocation = bl;
		isMySpawn = _isMySpawn;
	}
	public BaseLocation baseLocation;
	public boolean isMySpawn;
	public boolean isEnemySpawn;
}
