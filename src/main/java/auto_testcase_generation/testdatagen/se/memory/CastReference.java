package auto_testcase_generation.testdatagen.se.memory;

public class CastReference extends Reference {

    private ISymbolicVariable ref;


    public CastReference(LogicBlock block) {
        super(block);
    }

    public CastReference(LogicBlock block, String startIndex) {
        super(block, startIndex);
    }

    public CastReference(ISymbolicVariable ref, LogicBlock block, String startIndex) {
        super(block, startIndex);
        this.ref = ref;
    }

    public ISymbolicVariable getRef() {
        return ref;
    }

    public void setRef(ISymbolicVariable ref) {
        this.ref = ref;
    }
}
