/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.imports.kettle;

import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.util.StringUtil;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.xml.XmlHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Map;

public class KettleConverter {

  public static final String XML_TAG_KETTLE_TRANSFORMATION_STEPS = "transformation-steps"; // NEXUS_MOD
  public static final String XML_TAG_KETTLE_JOB_ENTRIES = "job-jobentries"; // NEXUS_MOD

  private enum EntryType {
    TRANS, JOB, START, DUMMY, OTHER
  }

  private String defaultWorkflowRunConfiguration = "";
  private String defaultPipelineRunConfiguration = "";

  public KettleConverter() {
    super();
  }

  public static Node checkForKettleTransformationSteps(Document doc, Node node) {
    if (node == null || !node.hasChildNodes()) { // NEXUS-MOD KETTLE CONVERT
      node = XmlHandler.getSubNode(doc, KettleConverter.XML_TAG_KETTLE_TRANSFORMATION_STEPS);
      if (node != null && node.hasChildNodes()) {
        KettleConverter converter = new KettleConverter();
        converter.processNode(doc, node, null);
      }
    }
    return node;
  }

  public static Node checkForKettleJobEntries(Document doc, Node node) {
    if (node == null || !node.hasChildNodes()) { // NEXUS-MOD KETTLE CONVERT
      node = XmlHandler.getSubNode(doc, KettleConverter.XML_TAG_KETTLE_JOB_ENTRIES);
      if (node != null && node.hasChildNodes()) {
        KettleConverter converter = new KettleConverter();
        converter.processNode(doc, node, null);
      }
    }
    return node;
  }

  private void renameNode(Document doc, Element element, String newElementName) {
    doc.renameNode(element, null, newElementName);
  }

  public void processNode(Document doc, Node node, EntryType entryType) {
    Node nodeToProcess = node;
    NodeList nodeList = nodeToProcess.getChildNodes();

    // do a first pass to remove repository definitions
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node repositoryNode = nodeList.item(i);
      if (repositoryNode.getNodeType() == Node.ELEMENT_NODE) {
        if (KettleConst.repositoryTypes.contains(repositoryNode.getTextContent())) {

          for (int j = 0; j < node.getChildNodes().getLength(); j++) {
            Node childNode = node.getChildNodes().item(j);
            if (childNode.getNodeName().equals("jobname") || childNode.getNodeName().equals("transname")) {
              if (!StringUtil.isEmpty(childNode.getTextContent())) {
                nodeToProcess = processRepositoryNode(node);
              }
            }
          }
          nodeList = nodeToProcess.getChildNodes();
        }
      }
    }

    for (int i = 0; i < nodeList.getLength(); i++) {
      Node currentNode = nodeList.item(i);
      if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
        // Identify if an entry is of type START or DUMMY type because they must be managed properly
        if (currentNode.getNodeName().equals("entry")) {
          entryType = EntryType.OTHER;
          Node entryTypeNode = null;
          boolean isEntryTypeSpecial = false;
          NodeList currentNodeChildNodes = currentNode.getChildNodes();
          for (int i1 = 0; i1 < currentNodeChildNodes.getLength(); i1++) {
            Node childNode = currentNodeChildNodes.item(i1);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
              if (childNode.getNodeName().equals("type") && childNode.getChildNodes().item(0).getNodeValue().equals("SPECIAL")) {
                isEntryTypeSpecial = true;
                entryTypeNode = childNode;
              } else if (childNode.getNodeName().equals("type") && childNode.getChildNodes().item(0).getNodeValue().equals("TRANS")) {
                entryType = EntryType.TRANS;
              } else if (childNode.getNodeName().equals("type") && childNode.getChildNodes().item(0).getNodeValue().equals("JOB")) {
                entryType = EntryType.JOB;
              } else if (isEntryTypeSpecial && childNode.getNodeName().equals("start") && childNode.getChildNodes().item(0).getNodeValue().equals("Y")) {
                entryType = EntryType.START;
              } else if (isEntryTypeSpecial && childNode.getNodeName().equals("dummy") && childNode.getChildNodes().item(0).getNodeValue().equals("Y")) {
                entryType = EntryType.DUMMY;
                // Immediately change entry type to DUMMY to not bother about it later on
                entryTypeNode.getFirstChild().setTextContent("DUMMY");
              }
            }
          }
        }

        // remove superfluous elements
        if (entryType == EntryType.OTHER) {
          if (KettleConst.kettleElementsToRemove.containsKey(currentNode.getNodeName())) {
            if (!StringUtils.isEmpty(KettleConst.kettleElementsToRemove.get(currentNode.getNodeName()))) {
              // see if we have multiple parent nodes to check for:
              if (KettleConst.kettleElementsToRemove.get(currentNode.getNodeName()).contains(",")) {
                Node parentNode = currentNode.getParentNode();
                String[] parentNodeNames = KettleConst.kettleElementsToRemove.get(currentNode.getNodeName()).split(",");
                for (String parentNodeName : parentNodeNames) {
                  if (parentNode.getNodeName().equals(parentNodeName)) {
                    parentNode.removeChild(currentNode);
                  }
                }
              } else {
                if (currentNode.getParentNode().getNodeName().equals(KettleConst.kettleElementsToRemove.get(currentNode.getNodeName()))) {
                  currentNode.getParentNode().removeChild(currentNode);
                }
              }
            } else {
              currentNode.getParentNode().removeChild(currentNode);
            }
          }
        } else if (entryType == EntryType.START) {
          if (KettleConst.kettleStartEntryElementsToRemove.containsKey(currentNode.getNodeName())) {
            currentNode.getParentNode().removeChild(currentNode);
          }
        } else if (entryType == EntryType.DUMMY) {
          if (KettleConst.kettleDummyEntryElementsToRemove.containsKey(currentNode.getNodeName())) {
            currentNode.getParentNode().removeChild(currentNode);
          }
        }

        if (entryType == EntryType.JOB || entryType == EntryType.TRANS) {
          if (currentNode.getNodeName().equals("run_configuration") && Utils.isEmpty(currentNode.getNodeValue())) {
            if (entryType == EntryType.JOB)
              currentNode.setNodeValue(defaultWorkflowRunConfiguration);
            else if (entryType == EntryType.TRANS)
              currentNode.setNodeValue(defaultPipelineRunConfiguration);
          }
        }

        // rename Kettle elements to Hop elements
        if (KettleConst.kettleElementReplacements.containsKey(currentNode.getNodeName())) {
          renameNode(doc, (Element) currentNode, KettleConst.kettleElementReplacements.get(currentNode.getNodeName()));
        }

        // replace element contents with Hop equivalent
        if (KettleConst.kettleReplaceContent.containsKey(currentNode.getTextContent())) {
          currentNode.setTextContent(KettleConst.kettleReplaceContent.get(currentNode.getTextContent()));
        }

        processNode(doc, currentNode, entryType);
      }

      // partial node content replacement
      if (currentNode.getNodeType() == Node.TEXT_NODE && !StringUtils.isEmpty(currentNode.getTextContent())) {
        for (Map.Entry<String, String> entry : KettleConst.kettleReplaceInContent.entrySet()) {
          if (currentNode.getTextContent().contains(entry.getKey())) {
            currentNode.setTextContent(currentNode.getTextContent().replace(entry.getKey(), entry.getValue()));
          }
        }
      }
    }
  }

  private Node processRepositoryNode(Node repositoryNode) {

    String filename = "";
    String directory = "${PROJECT_HOME}";
    String type = "";
    Node filenameNode = null;

    for (int i = 0; i < repositoryNode.getChildNodes().getLength(); i++) {
      Node childNode = repositoryNode.getChildNodes().item(i);
      if (childNode.getNodeName().equals("directory")) {
        // if (childNode.getTextContent().startsWith(System.getProperty("file.separator"))) {
        if (childNode.getTextContent().startsWith("\\") || childNode.getTextContent().startsWith("/")) { // Nexus NEXUS-MOD
          directory += childNode.getTextContent();
        } else {
          directory += System.getProperty("file.separator") + childNode.getTextContent();
        }
        repositoryNode.removeChild(childNode);
      }
      if (childNode.getNodeName().equals("type")) {
        if (KettleConst.jobTypes.contains(childNode.getTextContent())) {
          type = ".hwf";
        }
        if (KettleConst.transTypes.contains(childNode.getTextContent())) {
          type = ".hpl";
        }
      }
      if (childNode.getNodeName().equals("filename")) {
        filename = childNode.getTextContent().replaceAll(".ktr", "").replaceAll(".kjb", "");
        childNode.setTextContent(filename + type);
        filenameNode = childNode;
      }

      // hard coded local run configuration for now
      if (childNode.getNodeName().equals("run_configuration")) {
        childNode.setTextContent("local");
      }
      if (childNode.getNodeName().equals("jobname") || childNode.getNodeName().equals("transname")) {
        filename = childNode.getTextContent();
        repositoryNode.removeChild(childNode);
      }
    }

    filenameNode.setTextContent(directory + "/" + filename + type);

    return repositoryNode;
  }

}
