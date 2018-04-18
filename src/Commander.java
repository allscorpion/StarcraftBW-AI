import helpers.BaseManager;
import helpers.BuildingsManager;
import helpers.ConstructionManager;
import helpers.MiningHelper;
import helpers.ResourcesManager;
import helpers.UnitsManager;
import helpers.WorkersManager;

public class Commander {
	public static void Init() {
		BaseManager.Init();
		BuildingsManager.Init();
		ConstructionManager.Init();
		MiningHelper.Init();
		ResourcesManager.Init();
		UnitsManager.Init();
		WorkersManager.Init();
	}
	
}
