package auto_testcase_generation.cfg.object;

/**
 * Represent flag statement, e.g., "{", "}"
 *
 * @author ducvu
 */
public abstract class FlagCfgNode extends CfgNode {
	public FlagCfgNode() {
	}

	@Override
	public boolean isNormalNode() {
		return false;
	}

	@Override
	public boolean shouldDisplayInCFG() {
		return true;
	}

	@Override
	public boolean shouldInBlock() {
		return false;
	}
}
