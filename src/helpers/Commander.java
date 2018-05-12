package helpers;
import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import enums.PlayStyles;

public class Commander {
	public static void Init() {
		CameraHelper.init();
		BuildingsManager.Init();
		BaseManager.Init();
		ConstructionManager.Init();
		MiningHelper.Init();
		ResourcesManager.Init();
		UnitsManager.Init();
		WorkersManager.Init();
		EnemyManager.Init();
		ScoutsManager.Init();
		currentPlayStyle = PlayStyles.Military;
		allMyUnits = new ArrayList<Unit>();
	}
	public static List<Unit> allMyUnits;
	
	public static PlayStyles currentPlayStyle;
}
