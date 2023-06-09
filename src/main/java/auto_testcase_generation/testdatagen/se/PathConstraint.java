package auto_testcase_generation.testdatagen.se;

import auto_testcase_generation.cfg.object.ICfgNode;

/**
 * Represent a path constraint
 * 
 * @author ducanhnguyen
 *
 */
public class PathConstraint {
	protected String constraint;
	protected ICfgNode cfgNode;
	protected int type;

	public PathConstraint(String constraint, ICfgNode cfgNode, int type) {
		this.constraint = constraint;
		this.cfgNode = cfgNode;
		this.type = type;
	}

	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	public ICfgNode getCfgNode() {
		return cfgNode;
	}

	public void setCfgNode(ICfgNode cfgNode) {
		this.cfgNode = cfgNode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new PathConstraint(constraint, cfgNode, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PathConstraint) {
			return ((PathConstraint) obj).getConstraint().equals(getConstraint());
		} else
			return false;
	}

	@Override
	public String toString() {
		if (cfgNode != null)
			return constraint + "\t\t(original = \"" + cfgNode.getContent() + "\")";
		else
			return constraint;
//		if (cfgNode != null)
//			return constraint + "\t\t(" + (type == CREATE_FROM_DECISION ? "decision - " : "additional")
//					+ cfgNode.getContent() + ")";
//		else
//			return constraint + "\t\t(" + (type == CREATE_FROM_DECISION ? "decision" : "additional") + ")";
	}

	public static final int UNSPECIFIED = -1;

	public static final int CREATE_FROM_DECISION = 0;

	// Represent additional constraints, e.g., constraints for positive indexes
	public static final int ADDITIONAL_TYPE = 1;
}
