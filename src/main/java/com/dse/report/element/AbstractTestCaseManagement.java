package com.dse.report.element;

import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractTestCaseManagement extends Section {

    private static final AkaLogger logger = AkaLogger.get(AbstractTestCaseManagement.class);

    public AbstractTestCaseManagement() {
        super("tcs-manage");
    }

    protected abstract void generate();

    protected Table.Cell<Text> generateStatusCell(AssertionResult result) {
        String statusCol = SpecialCharacter.EMPTY;

        String bgColor;
        if (result == null || result.getTotal() == 0)
            bgColor = COLOR.LIGHT;
        else {
            statusCol = String.format("PASS %d/%d", result.getPass(), result.getTotal());

            if (result.isAllPass())
                bgColor = COLOR.GREEN;
            else if (result.getPass() == 0)
                bgColor = COLOR.RED;
            else
                bgColor = COLOR.YELLOW;
        }

        return new Table.Cell<>(statusCol, bgColor);
    }
}
