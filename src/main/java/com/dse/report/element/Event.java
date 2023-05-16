package com.dse.report.element;

import com.dse.environment.Environment;
import com.dse.report.converter.*;
import com.dse.report.converter.gtest.GTestInitialAssertionConverter;
import com.dse.report.converter.gtest.GTestLastAssertionConverter;
import com.dse.report.converter.gtest.GTestMiddleAssertionConverter;
import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testdata.object.SubprogramNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Event implements IElement {

    private final List<IElement> elements = new ArrayList<>();

    private final SubprogramNode subprogram;
    private final List<IResultTrace> failures;

    private final int index;
    private final Position pos;

    private int iterator = 1;

    /**
     * Result PASS/ALL
     */
    private AssertionResult results = new AssertionResult();

    private Map<SubprogramNode, Integer> numberOfCalls;

    public Event(SubprogramNode subprogram, List<IResultTrace> failures, int index, Position pos) {
        this.subprogram = subprogram;
        this.failures = failures;
        this.index = index;
        this.pos = pos;
    }

    public Event(SubprogramNode subprogram, List<IResultTrace> failures, int index, Position pos, int iterator) {
        this.subprogram = subprogram;
        this.failures = failures;
        this.index = index;
        this.pos = pos;
        this.iterator = iterator;
    }

    public void setNumberOfCalls(Map<SubprogramNode, Integer> numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    public void generate() {
        generateEventHeader(elements, index);

        Converter assertConverter;

        if (Environment.getInstance().getCompiler().isUseGTest()) {
            if (pos == Position.FIRST) {
                assertConverter = new GTestInitialAssertionConverter(failures, index);
            } else if (pos == Position.LAST) {
                assertConverter = new GTestLastAssertionConverter(failures, index, numberOfCalls);
            } else {
                assertConverter = new GTestMiddleAssertionConverter(failures, index, iterator);
            }
        } else {
            if (pos == Position.FIRST)
                assertConverter = new InitialAssertionConverter(failures, index);
            else if (pos == Position.LAST)
                assertConverter = new LastAssertionConverter(failures, index, numberOfCalls);
            else
                assertConverter = new MiddleAssertionConverter(failures, index, iterator);

        }


        Table assertTable = assertConverter.execute(subprogram);

        results = assertConverter.getResults();
        elements.add(assertTable);

        if (failures != null) {
            generateUserCodeAssert();
        }
    }

    public static void generateEventHeader(List<IElement> body, int index) {
        Section.Line title = new Section.CenteredLine(new Text("Event " + index, TEXT_STYLE.BOLD), COLOR.MEDIUM);
        body.add(title);
    }

    public AssertionResult getResults() {
        return results;
    }

    private void generateUserCodeAssert() {
        final String fcallTag = "Aka function calls: " + index;

        List<IResultTrace> userCodeFailures = failures.stream()
                .filter(f -> f.getUserCode() != null && f.getMessage().contains(fcallTag))
                .collect(Collectors.toList());

        if (!userCodeFailures.isEmpty()) {
            elements.add(new Section.CenteredLine("User Code Assertion", COLOR.MEDIUM));
            UserCodeAssertion assertion = new UserCodeAssertion(userCodeFailures);
            elements.add(assertion);

//            results[0] += assertion.getResults()[0];
//            results[1] += assertion.getResults()[1];
            results.append(assertion.getResults());
        }
    }

    public List<IElement> getElements() {
        return elements;
    }

    @Override
    public String toHtml() {
        StringBuilder html = new StringBuilder();

        for (IElement element : elements) {
            html.append(element.toHtml());
        }

        return html.toString();
    }

    public enum Position {
        FIRST,
        LAST,
        UNKNOWN,
        MIDDLE
    }
}
