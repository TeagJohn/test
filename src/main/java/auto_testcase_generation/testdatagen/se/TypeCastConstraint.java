package auto_testcase_generation.testdatagen.se;

import auto_testcase_generation.cfg.object.ICfgNode;

public class TypeCastConstraint extends PathConstraint {

    public TypeCastConstraint(String constraint, ICfgNode cfgNode, int type) {
        super(constraint, cfgNode, type);
    }
}
