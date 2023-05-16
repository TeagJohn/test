package com.dse.testcase_execution.testdriver;

import com.dse.environment.Environment;
import com.dse.testcase_execution.DriverConstant;
import com.dse.testcase_manager.TestCase;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;

/**
 * Old name: TestdriverGenerationforCpp
 * <p>
 * Generate test driver for function put in an .cpp file in executing test data entering by users
 * <p>
 * comparing EO and RO
 *
 * @author ducanhnguyen
 */
public class GTestDriverGenerationForCpp extends AssertableTestDriverGeneration {

    @Override
    public String getTestDriverTemplate() {
        return Utils.readResourceContent(CPP_GTEST_DRIVER_PATH);
    }

    protected String wrapScriptInTryCatch(String script) {
        if (script.endsWith("}")) {
            script = script.substring(0, script.length() - 1);
        }
        return String.format(
                "try %s catch (std::exception& error) {\n\t\t" +
                        DriverConstant.MARK + "(\"Phat hien loi runtime\");\n" +
                        "\t}\n", script.replaceFirst("^\n*", "").replaceAll("\n", "\n\t\t")+"\n\t}");
    }

    @Override
    protected String generateTestScript(TestCase testCase) throws Exception {
        if (Environment.getInstance().getCompiler().isUseGTest()) {
            String body = generateBodyScript(testCase);

            return String.format(
                    "TEST(%s, %s) {\n" +
                            "\t%s" +
                            "};\n\n",
                    "AkaTest", testCase.getName().replaceAll("[^\\w]", SpecialCharacter.UNDERSCORE), body);

        } else {
            return super.generateTestScript(testCase);
        }

//        return String.format("void " + AKA_TEST_PREFIX + "%s(void) {\n%s\n}\n", testCaseName, body);
    }
}
