package structures;

import bwapi.UnitType;
import interfaces.IStructure;

public class CacheStructure {
	public CacheStructure(UnitType bt, IStructure sc) {
		buildingType = bt;
		structureClass = sc;
	}
	public UnitType buildingType;
	public IStructure structureClass;
}
