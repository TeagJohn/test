package auto_testcase_generation.cfg;

import auto_testcase_generation.cfg.object.*;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.FunctionNode;
import com.dse.regression.cia.WaveCIA;
import com.dse.testcase_manager.AbstractTestCase;
import com.dse.util.CFGUtils;
import com.dse.util.PathUtils;
import com.dse.util.Utils;
import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

public class CFGExporter {
    private final static AkaLogger logger = AkaLogger.get(CFGExporter.class);

    public List<CfgNode> visitedNodes = new ArrayList<>();
    public Stack<CfgNode> stackScopeNodes = new Stack<>();
    public Stack<CfgNode> runningScopeStach = new Stack<>();
    FunctionNode functionNode;

    public void exportCFG(FunctionNode functionNode) {
        this.functionNode = functionNode;
        ICFG cfg = null;
        try {
            cfg = CFGUtils.createCFG(functionNode, Environment.getInstance().getTypeofCoverage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cfg == null) return;

        visitedNodes.clear();

        if (functionNode.getAbsolutePath().contains("method1")) {
            try {
                ICFG cfg1 = cfg;
                String pathCFG2 = "/home/teag-john/Documents/regression_paper/test/demo/aka-working-space/test45/cfg/lib/lib1.cpp/Struct1_method1_int_int_cfg.json";
                CFG cfg2 = new CFGImporter().importCFGByFile(pathCFG2);
                WaveCIA.getWaveCIA().compareCFG((CFG) cfg1, cfg2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(CFG.class, new JsonSerializer<CFG>() {
                    @Override
                    public JsonElement serialize(CFG cfg, Type type, JsonSerializationContext jsonSerializationContext) {
                        JsonObject json = new JsonObject();
                        json.addProperty("functionNode", functionNode.getAbsolutePath());


                        JsonArray jsonStatements = new JsonArray();
                        for (ICfgNode node : cfg.getAllNodes()) {
                            JsonElement element = exportToJsonElement((CfgNode) node);
                            if (!element.isJsonNull()) jsonStatements.add(element);
                        }
                        json.add("statements", jsonStatements);
                        return json;
                    }
                }).setPrettyPrinting().create();

        String json = gson.toJson(cfg);

        System.out.println("PATH CFG FUNCTION NODE of " + functionNode.getAbsolutePath() + " is that " + CFGUtils.createCFGFunctionPath(functionNode));
        Utils.writeContentToFile(json, CFGUtils.createCFGFunctionPath(functionNode));
    }



    public JsonElement exportToJsonElement(CfgNode cfgNode) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .registerTypeAdapter(CfgNode.class, new CFGNodeTreeSerializer(cfgNode, this))
                .setPrettyPrinting().create();
        return gson.toJsonTree(cfgNode, CfgNode.class);
    }

    static class CFGNodeTreeSerializer implements JsonSerializer<CfgNode> {

        private final CfgNode cfgNode;
        public CFGExporter exporter;

        public CFGNodeTreeSerializer(CfgNode cfgNode, CFGExporter exporter) {
            this.cfgNode = cfgNode;
            this.exporter = exporter;
        }

        @Override
        public JsonElement serialize(CfgNode cfgNode, Type type, JsonSerializationContext jsonSerializationContext) {
            if (exporter.visitedNodes.contains(cfgNode)) return null;
            exporter.visitedNodes.add(cfgNode);
//            if (!(cfgNode instanceof NormalCfgNode)) return jsonSerializationContext.serialize(cfgNode.getTrueNode(), CfgNode.class);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", cfgNode.getClass().getName());
            jsonObject.addProperty("id", cfgNode.getId());
            jsonObject.addProperty("content", cfgNode.getContent());
            if (cfgNode instanceof NormalCfgNode) {
                jsonObject.addProperty("startOffset", ((NormalCfgNode) cfgNode).getAst().getFileLocation().getNodeOffset());
                jsonObject.addProperty("lineOffset", ((NormalCfgNode) cfgNode).getAst().getFileLocation().getStartingLineNumber());
            }
            if (cfgNode.getTrueNode() != null) jsonObject.addProperty("true", cfgNode.getTrueNode().getId());
            if (cfgNode.getFalseNode() != null) jsonObject.addProperty("false", cfgNode.getFalseNode().getId());

//            if (cfgNode instanceof ConditionCfgNode) {
//                JsonArray childrenTrue = new JsonArray();
//                JsonArray childrenFalse = new JsonArray();
//                // true branch
//                CfgNode trueNode = (CfgNode) cfgNode.getTrueNode();
//                if (trueNode instanceof ScopeCfgNode && trueNode.getContent().equals("{")) {
//                    exporter.stackScopeNodes.push(trueNode);
//                    CfgNode child = (CfgNode) trueNode.getTrueNode();
//                    while (!(child instanceof ScopeCfgNode && child.getContent().equals("}"))) {
//                        //do something here ...
//                        JsonElement element = jsonSerializationContext.serialize(child, CfgNode.class);
//                        if (!element.isJsonNull()) childrenTrue.add(element);
//                        child = (CfgNode) child.getTrueNode();
//                        if (child instanceof ScopeCfgNode && child.getContent().equals("}")) exporter.stackScopeNodes.pop();
//                    }
//                }
//                else {
//                    JsonElement element = jsonSerializationContext.serialize(trueNode, CfgNode.class);
//                    if (!element.isJsonNull()) childrenTrue.add(element);
//                }
//
//                // false branch
//                CfgNode falseNode = (CfgNode) cfgNode.getFalseNode();
//                if (falseNode instanceof ScopeCfgNode && falseNode.getContent().equals("{")) {
//                    exporter.stackScopeNodes.push(falseNode);
//                    CfgNode child = (CfgNode) falseNode.getFalseNode();
//                    while (!(child instanceof ScopeCfgNode && child.getContent().equals("}"))) {
//                        //do something here ...
//                        JsonElement element = jsonSerializationContext.serialize(child, CfgNode.class);
//                        if (!element.isJsonNull()) childrenFalse.add(element);
//                        child = (CfgNode) child.getFalseNode();
//                        if (child instanceof ScopeCfgNode && child.getContent().equals("}")) exporter.stackScopeNodes.pop();
//                    }
//                }
//                else {
//                    JsonElement element = jsonSerializationContext.serialize(falseNode, CfgNode.class);
//                    if (!element.isJsonNull()) childrenFalse.add(element);
//                }
//                jsonObject.add("true", childrenTrue);
//                jsonObject.add("false", childrenFalse);
//            }

            return jsonObject;
        }
    }

    public static void main(String[] args) {
        System.out.println(new CFGExporter().getClass().getSimpleName());
    }
}
