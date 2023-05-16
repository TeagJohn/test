package com.dse.testdata.comparable.gmock;

public interface IGmockSpecialWords {
    String INCLUDE = "#include ";
    String PUBLIC = "public";
    String PRIVATE = "private";
    String CLASS = "class";
    String MOCK = "Mock";
    String MOCK_METHOD = "MOCK_METHOD";

    String NAME_CLASS = "/*NAMECLASS*/";
    String MOCK_METHODS = "/*MOCK_METHODS*/";
    String RETURN_TYPE = "ReturnType";
    String METHOD_NAME = "MethodName";
    String ARGS = "(Args)";
    String SPECS = ", (Specs)";
    String OVERRIDE = "override";
    String CONST = "const";

//    String MOCK_CLASS_TEMPLATE = "class Mock/*NAMECLASS*/ : public /*NAMECLASS*/ {\n"
//            + "public:\n"
//            + "/*MOCK_METHODS*/\n"
//            + "};\n\n";

    String PUBLIC_METHOD = "/*PUBLIC_METHODS*/";
    String PRIVATE_METHOD = "/*PRIVATE_METHODS*/";
    String PROTECTED_METHOD = "/*PROTECTED_METHODS*/";
    String MOCK_CLASS_TEMPLATE = "class Mock/*NAMECLASS*/{\n"
            + "private:\n"
            + "/*PRIVATE_METHODS*/"
            + "protected:\n"
            + "/*PROTECTED_METHODS*/"
            + "public:\n"
            + "/*PUBLIC_METHODS*/\n"
            + "};\n\n";

    String MOCK_METHOD_TEMPLATE ="MOCK_METHOD(ReturnType, MethodName, (Args), (Specs));\n";

    String EXPECT_CALL = "EXPECT_CALL";
    String MOCK_OBJECT = "mock_object";
    String METHOD = "method";
    String TIMES = "time_value";
    String WITH = "/*WITH*/";
    String WILLONCE = ".WillOnce(";
    String RETURN = "::testing::Return(";

    String EXPECT_CALL_TEMPLATE = "EXPECT_CALL(mock_object, method)/*WITH*/\n\t.Times(time_value)";

    String VerifyAndClearExpectations = "\n::testing::Mock::VerifyAndClearExpectations(mock_object);\n"
                                        + "::testing::Mock::VerifyAndClear(mock_object);\n"
                                        + "::testing::Mock::AllowLeak(mock_object);\n";

    String AKA_GMOCK = "AKA_GMOCK_";
    String AKA_INCLUDE_MOCK_FILE = "#ifndef AKA_INCLUDE_MOCK_FILE\n"
                                    + "#define AKA_INCLUDE_MOCK_FILE\n"
                                    + "/*INCLUDE_MOCK_FILE*/\n"
                                    + "#endif\n\n";
    String INCLUDE_MOCK_FILE = "/*INCLUDE_MOCK_FILE*/";
    String IFDEF_GMOCK = "#ifdef AKA_GMOCK_/*NAMECLASS*/\n"
                        + "#define /*NAMECLASS*/ Mock/*NAMECLASS*/\n"
                        + "#endif\n\n";
}
