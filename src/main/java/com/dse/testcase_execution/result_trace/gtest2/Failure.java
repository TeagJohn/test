package com.dse.testcase_execution.result_trace.gtest2;

import com.dse.testcase_execution.AbstractTestcaseExecution;
import com.dse.testcase_execution.result_trace.AbstractResultTrace;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testcase_manager.ITestCase;
import com.dse.testdata.comparable.gtest.*;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.google.gson.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Failure extends AbstractResultTrace {

    private String assertStm;

    private String message;

    public String getMessage() {
        return message;
    }

    /*
     * Example:
     *
     *       Expected: AKA_STUB_r
     *       Which is: 4            [i - 3] actual
     * To be equal to: r
     *       Which is: 3            [i - 1] expected
     * Aka function calls: 2        [i]     fcalls tag
     *
     */

    public String getActual() {
        String assertMethod = getAssertMethod();
        assertMethod = assertMethod.split(" " + SpecialCharacter.DELTA + " ")[0];
        if (IBinaryComparison.assertMethods.contains(assertMethod) || IFloatingPointComparison.assertMethods.contains(assertMethod)) {
            Pattern p;
            Matcher m;
            switch (assertMethod) {
                case IGTestAssertMethod.EXPECT_EQ:
                case IGTestAssertMethod.ASSERT_EQ:
                case IGTestAssertMethod.EXPECT_FLOAT_EQ:
                case IGTestAssertMethod.ASSERT_FLOAT_EQ:
                case IGTestAssertMethod.EXPECT_DOUBLE_EQ:
                case IGTestAssertMethod.ASSERT_DOUBLE_EQ:
                    p = Pattern.compile("Which is: ([a-zA-Z0-9\\'.+-]*)");
                    m = p.matcher(message);
                    if (m.find()) {
                        return m.group(1);
                    }
                    break;
                case IGTestAssertMethod.EXPECT_NEAR:
                case IGTestAssertMethod.ASSERT_NE:
                    p = Pattern.compile("evaluates to ([a-zA-Z0-9\\'.+-]*)");
                    m = p.matcher(message);
                    if (m.find()) {
                        return m.group(1);
                    }
                    break;
                default:
                    p = Pattern.compile("([a-zA-Z0-9\\'.+-]*) vs ([a-zA-Z0-9\\'.+-]*)");
                    m = p.matcher(message);
                    if (m.find()) {
                        return m.group(1);
                    }
                    break;
            }
        } else if (IBooleanConditions.assertMethods.contains(assertMethod)) {
            Pattern p = Pattern.compile("Actual: (true|false)");
            Matcher m = p.matcher(message);
            if (m.find()) {
                return m.group(1);
            }
        } else if (IGeneralizedAssertion.assertMethods.contains(assertMethod)) {
            Pattern p = Pattern.compile("Actual: ([a-zA-Z0-9\\'.+-]*)");
            Matcher m = p.matcher(message);
            if (m.find()) {
                return m.group(1);
            }
        } else if (IExceptionAssertions.assertMethods.contains(assertMethod)) {
            return "";
        }
        return PROBLEM_VALUE;
    }

    @Override
    public String getExpectedName() {
        String[] lines = getMessageLines(message);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(FCALL_TAG)) {
                return lines[i - 2].substring("To be equal to: ".length()).trim();
            }
        }
        return PROBLEM_VALUE;
    }

    private String getAssertMethod() {
        return assertStm.split("\\(")[0];
    }

    @Override
    public String getActualName() {
        String[] lines = getMessageLines(message);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(FCALL_TAG)) {
                return lines[i - 4].substring("Expected: ".length());
            }
        }
        return PROBLEM_VALUE;
    }

    public String getExpected() {
        String[] lines = getMessageLines(message);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(FCALL_TAG)) {
                return lines[i - 1].substring(OFFSET);
            }
        }

        return PROBLEM_VALUE;
    }

    protected static final int OFFSET = 10;

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("Failure: ");

        for (String line : getMessageLines(message)) {
            output.append(line).append(SpecialCharacter.LINE_BREAK);
        }

        return output.toString();
    }

    public String getAssertStm() {
        return assertStm;
    }

    public void setAssertStm(String assertStm) {
        this.assertStm = assertStm;
    }

    public static List<IResultTrace> load(ITestCase testCase) {
        List<IResultTrace> list = new ArrayList<>();

        String path = testCase.getExecutionResultTrace();

        if (!new File(path).exists())
            return null;

        String content = Utils.readFileContent(path);

        if (!content.trim().isEmpty()) {
            JsonArray jsonArray;
            try {
                jsonArray = JsonParser.parseString(content).getAsJsonArray();
            } catch (JsonSyntaxException e) {
                content = AbstractTestcaseExecution.refactorResultTrace(testCase);
                jsonArray = JsonParser.parseString(content).getAsJsonArray();
            }

            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Failure failure = new Failure();

                String assertStm = jsonObject.get("assertStm").getAsString();
                failure.setAssertStm(assertStm);

                String msg = jsonObject.get("message").getAsString();
                failure.setMessage(msg);


                if (!list.contains(failure))
                    list.add(failure);
            }
        }

        return list;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
