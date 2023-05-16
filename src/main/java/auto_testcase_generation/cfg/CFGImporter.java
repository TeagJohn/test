package auto_testcase_generation.cfg;

import auto_testcase_generation.cfg.object.*;
import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.util.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.util.Pair;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CFGImporter {
    private final static AkaLogger logger = AkaLogger.get(CFGImporter.class);
    private CFG cfg;
    private List<ICfgNode> statement = new ArrayList<>();
    private Map<Integer, CfgNode> mapStatementByID = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        try {
            new CFGImporter().importCFGByFile(
                    "/home/teag-john/Documents/regression_paper/test/demo/aka-working-space/test43/cfg/lib/lib1.cpp/Struct1_method1_int_int_cfg.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        String className = "auto_testcase_generation.cfg.object.BeginFlagCfgNode";
//        Class<?> clazz = Class.forName(className);
//        Constructor<?> ctor = clazz.getConstructor(String.class);
//        Object object = ctor.newInstance(new Object[] {});
//        if (object instanceof BeginFlagCfgNode) System.out.println("true");
    }

    public CFG importCFGByFile(String pathFileOfCFG) throws IOException {
        String jsonContent = Utils.readFileContent(pathFileOfCFG);
        JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
        if (jsonObject == null || jsonObject.isJsonNull()) {
            logger.debug("Failed to import CFG path: " + pathFileOfCFG);
            throw new IOException("Import CFG failed.");
        }
        return parseJsonToCFG(jsonObject);
    }

    public CFG parseJsonToCFG(JsonObject jsonObject) {
        cfg = new CFG(statement);
//        FunctionNode functionNode = (FunctionNode) Search.searchNodes(
//                Environment.getInstance().getProjectNode(), new FunctionNodeCondition(), functionName);
        JsonArray stms = jsonObject.get("statements").getAsJsonArray();
        if (stms == null) return cfg;

        String functionName = jsonObject.get("functionNode").getAsString();
        try {
            logger.debug("CFG function found: " + functionName);
            cfg.setFunctionNode((IFunctionNode) UIController.searchFunctionNodeByPath(functionName));
        } catch (FunctionNodeNotFoundException e) {
            logger.debug("CFG function not found: " + functionName);
            e.printStackTrace();
        }

        Map<Integer, Pair<Integer, Integer>> mapIndex = new HashMap<>();
        for (int i = 0; i < stms.size(); i++) {
            JsonObject stm = stms.get(i).getAsJsonObject();
            String type = stm.get("type").getAsString();
            int id = stm.get("id").getAsInt();
            String content = stm.get("content").getAsString();
            int trueBranchID = (stm.keySet().contains("true")) ? stm.get("true").getAsInt() : -1;
            int falseBranchID = (stm.keySet().contains("false")) ? stm.get("false").getAsInt() : -1;

            Object object = null;
            try {
//                logger.debug("id=" + id + " content=" + content);
                object = Class.forName(type).getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (object != null) {
                ((ICfgNode) object).setId(id);
                ((ICfgNode) object).setContent(content);
                if (object instanceof NormalCfgNode) {
                    int startOffset = stm.get("startOffset").getAsInt();
                    int lineOffset = stm.get("lineOffset").getAsInt();
                    ((NormalCfgNode) object).setStartOffset(startOffset);
                    ((NormalCfgNode) object).setLineOffset(lineOffset);
                }
                statement.add((ICfgNode) object);
                mapStatementByID.put(id, (CfgNode) object);
            }
            mapIndex.put(id, new Pair<>(trueBranchID, falseBranchID));
        }

        for (int id : mapIndex.keySet()) {
            int trueID = mapIndex.get(id).getKey();
            int falseID = mapIndex.get(id).getValue();
            CfgNode parentNode = mapStatementByID.get(id);
            if (trueID >= 0) {
                CfgNode trueNode = mapStatementByID.get(trueID);
                parentNode.setTrue(trueNode);
                trueNode.setParent(parentNode);
            }
            if (falseID >= 0) {
                CfgNode falseNode = mapStatementByID.get(falseID);
                parentNode.setFalse(falseNode);
                falseNode.setParent(parentNode);
            }
        }

        return cfg;
    }

    public CfgNode createCfgNode(String className, int id, String content) {
        int lastDot = className.lastIndexOf(".");
        className = className.substring(lastDot);
        switch (className) {
            case "BeginFlagCfgNode": {
                new SimpleCfgNode();
            }
        }
        return null;
    }
}
