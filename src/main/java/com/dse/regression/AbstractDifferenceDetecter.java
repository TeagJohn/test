package com.dse.regression;

import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroSearchListNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.parser.VectorCastProjectLoader;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.Node;
import com.dse.parser.object.ProjectNode;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractDifferenceDetecter {
    final static AkaLogger logger = AkaLogger.get(AbstractDifferenceDetecter.class);

    protected Map<INode, Date> modifiedSourcecodeFiles = new HashMap<>();

    // nodes are deleted in the old tree
    protected List<INode> deletedNodes = new ArrayList<>();
    protected List<String> deletedPaths = new ArrayList<>();

    // nodes are modified in the old tree
    protected List<INode> modifiedNodes = new ArrayList<>();
//    protected List<String> modifiedPaths = new ArrayList<>();

    // nodes are added in the new tree
    protected List<INode> addedNodes = new ArrayList<>();
//    protected List<String> addedPaths = new ArrayList<>();

    protected String elementFolderOfOldVersion;

    public void detectChanges(INode physicalTreeRoot, String elementFolderOfOldVersion) {
        // get changed source code file by analyzing physical tree
        List<INode> modifiedSrcNodes = getModifiedSourcecodeFiles(physicalTreeRoot);
//        // get added node
//        List<INode> addedNodeList = getAddedSourcecodeFiles(physicalTreeRoot);

        for (INode modifiedSrcNode : modifiedSrcNodes) {
            diff(modifiedSrcNode, elementFolderOfOldVersion);
        }
    }

    public abstract void diff(INode modifiedSrcNode, String elementFolderOfOldVersion);

    public List<INode> getModifiedSourcecodeFiles(INode physicalTreeRoot) {
        // get all source codes file available in physical tree file
        List<INode> nodes = Search.searchNodes(physicalTreeRoot, new SourcecodeFileNodeCondition());

        // find changed file in these files
        List<INode> changedNode = new ArrayList<>();
        for (INode node : nodes)
            if (node instanceof ISourcecodeFileNode) {
                ISourcecodeFileNode castedNode = (ISourcecodeFileNode) node;
                Date oldLastModifiedDate = castedNode.getLastModifiedDate();
                Date newModifiedDate = getDate(new File(castedNode.getAbsolutePath()));
                if (oldLastModifiedDate != null && !oldLastModifiedDate.equals(newModifiedDate)) {
                    changedNode.add(node);
                    logger.debug("File " + castedNode.getAbsolutePath() + " is changed since " + newModifiedDate.toString());

                    modifiedSourcecodeFiles.put(node, newModifiedDate);

                    castedNode.setLastModifiedDate(newModifiedDate);
                }
                else if (oldLastModifiedDate == null && newModifiedDate != null) { // add new file -> have no old modified date, have only new modified date
                    changedNode.add(node);
                    logger.debug("File " + castedNode.getAbsolutePath() + " is added since " + newModifiedDate.toString());
                    modifiedSourcecodeFiles.put(node, newModifiedDate);
                    addedNodes.add(node);
                    castedNode.setLastModifiedDate(newModifiedDate);
                }
            }

        return changedNode;
    }

    public List<INode> getAddedSourcecodeFiles(INode physicalTreeRoot) {
        // added node return
        List<INode> addedNodes = new ArrayList<>();
        // This project node has been loaded by physical tree, which had been saved in the previous time.
        INode oldProjectNode = physicalTreeRoot;
        INode newProjectNode = null;
        String path = "";
        try {
            if (physicalTreeRoot instanceof ProjectNode) path = physicalTreeRoot.getAbsolutePath();
        }
        catch (NullPointerException nullPointerException) {
            nullPointerException.printStackTrace();
            logger.error("Can not find the project's absolute path!");
        }
        if (!path.isEmpty()) {
            VectorCastProjectLoader loader = new VectorCastProjectLoader();
            loader.setSourcecodeList(Arrays.asList(new File(path)));
            newProjectNode = loader.constructPhysicalTree();
        }
        if (newProjectNode == null) return null;
        List<Node> childrenOld = oldProjectNode.getChildren();
        for (Node childNew : newProjectNode.getChildren()) {
            boolean isIncluded = false;
            for (Node childOld : childrenOld) {
                if (childOld.getAbsolutePath().equals(childNew.getAbsolutePath())) {
                    isIncluded = true;
                }
            }
            if (!isIncluded) addedNodes.add(childNew);
        }
        return addedNodes;
    }

    public Date getDate(File f) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date newModifiedDate = null;
        try {
            newModifiedDate = sdf.parse(sdf.format(f.lastModified()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newModifiedDate;
    }



    public Map<INode, Date> getModifiedSourcecodeFiles() {
        return modifiedSourcecodeFiles;
    }

    public void setModifiedSourcecodeFiles(Map<INode, Date> modifiedSourcecodeFiles) {
        this.modifiedSourcecodeFiles = modifiedSourcecodeFiles;
    }

    public List<INode> getDeletedNodes() {
        return deletedNodes;
    }

    public void setDeletedNodes(List<INode> deletedNodes) {
        this.deletedNodes = deletedNodes;
    }

    public List<INode> getModifiedNodes() {
        return modifiedNodes;
    }

    public void setModifiedNodes(List<INode> modifiedNodes) {
        this.modifiedNodes = modifiedNodes;
    }

    public List<INode> getAddedNodes() {
        return addedNodes;
    }

    public void setAddedNodes(List<INode> addedNodes) {
        this.addedNodes = addedNodes;
    }

    public List<String> getDeletedPaths() {
        return deletedPaths;
    }

    public void setDeletedPaths(List<String> deletedPaths) {
        this.deletedPaths = deletedPaths;
    }

    public String getElementFolderOfOldVersion() {
        return elementFolderOfOldVersion;
    }

    public void setElementFolderOfOldVersion(String elementFolderOfOldVersion) {
        this.elementFolderOfOldVersion = elementFolderOfOldVersion;
    }

    //    public List<String> getModifiedPaths() {
//        return modifiedPaths;
//    }
//
//    public void setModifiedPaths(List<String> modifiedPaths) {
//        this.modifiedPaths = modifiedPaths;
//    }
//
//    public List<String> getAddedPaths() {
//        return addedPaths;
//    }
//
//    public void setAddedPaths(List<String> addedPaths) {
//        this.addedPaths = addedPaths;
//    }
}
