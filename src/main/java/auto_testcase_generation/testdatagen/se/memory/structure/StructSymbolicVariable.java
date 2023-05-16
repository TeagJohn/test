package auto_testcase_generation.testdatagen.se.memory.structure;

import auto_testcase_generation.testdatagen.se.memory.ISymbolicVariable;
import auto_testcase_generation.testdatagen.se.memory.PhysicalCell;
import auto_testcase_generation.testdatagen.se.memory.basic.BasicSymbolicVariable;

import java.util.ArrayList;
import java.util.List;

public class StructSymbolicVariable extends SimpleStructureSymbolicVariable {

	public StructSymbolicVariable(String name, String type, int scopeLevel) {
		super(name, type, scopeLevel);
	}

	@Override
	public List<PhysicalCell> getAllPhysicalCells() {
		// TODO: Get all physical cells of this symbolic variable
		ArrayList<PhysicalCell> physicalCells = new ArrayList<>();
		for (ISymbolicVariable attribute : attributes) {
			physicalCells.addAll(getPhysicalCellsOfAttribute(attribute));
		}

		return physicalCells;
	}

	private List<PhysicalCell> getPhysicalCellsOfAttribute(ISymbolicVariable attribute) {
		ArrayList<PhysicalCell> physicalCells = new ArrayList<>();
		if (attribute instanceof SimpleStructureSymbolicVariable) {
			physicalCells.addAll(((SimpleStructureSymbolicVariable) attribute).getAllPhysicalCells());
		} else if (attribute instanceof BasicSymbolicVariable) {
			physicalCells.add(((BasicSymbolicVariable) attribute).getCell());
		} else {
			physicalCells.addAll(attribute.getAllPhysicalCells());
		}

		return physicalCells;
	}
}
