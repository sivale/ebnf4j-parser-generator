package com.sverko.ebnf;

import com.sverko.ebnf.SvgPrinter.SvgNode.ParseNodeType;
import com.sverko.ebnf.tools.UnicodeString;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SvgPrinter {

  Set<SvgNode> svgNodeList = new HashSet<>();
  Map<ParseNode, SvgNode> sisterNodes = new HashMap<>();
  StringBuilder sb = new StringBuilder();
  int cnt = 0, id = 1000;
  int offsetFromTop = 100, offsetFromLeft = 100;

  public SvgPrinter(ParseNode rootNode) {
    SvgNode rootSvgNode = new SvgNode(0, 0);
    rootSvgNode.setNodeType(getSvgNodeType(rootNode));
    rootSvgNode.textInside = rootNode.name;
    rootSvgNode.id = id;

    sb.append(
        "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
            "<svg xmlns:svg='http://www.w3.org/2000/svg' \n" +
            "xmlns='http://www.w3.org/2000/svg' >"
    );

    makeSvgNodesTree(rootNode, rootSvgNode);
    connectSvgNodes(rootNode, rootSvgNode);
    positionSvgNodeTree(rootSvgNode);
    printSvgNodes(rootSvgNode);
    printSvgPaths(rootSvgNode);
    sb.append("</svg>\n");
  }

  public void makeSvgNodesTree(ParseNode parseNode, SvgNode parentNode) {

    sisterNodes.put(parseNode, parentNode);
    SvgNode childNode;

    if (parseNode.hasDownNode()) {
      if (!parseNode.isAncestor(parseNode.downNode)) {//prevents infinite loops
        childNode = new SvgNode();
        childNode.id = ++id;
        childNode.setNodeType(getSvgNodeType(parseNode.getDownNode()));
        childNode.textInside = getTextInside(parseNode.getDownNode());
        if(getSvgNodeType(parseNode.downNode) == ParseNodeType.TERMINAL_NODE){
          childNode.textBelow = ((TerminalNode) parseNode.getDownNode()).getTag();
        }
        makeSvgNodesTree(parseNode.getDownNode(), childNode);
      }
    }

    if (parseNode.hasRightNode()) {
      if (parseNode.rightNode.parent.equals(
          parseNode)) {  // not needed in current implementation but this might change in future
        childNode = new SvgNode(parentNode.getCol() + 1, parentNode.getRow());
        childNode.id = ++id;
        childNode.setNodeType(getSvgNodeType(parseNode.getRightNode()));
        childNode.textInside = getTextInside(parseNode.getRightNode());
        makeSvgNodesTree(parseNode.getRightNode(), childNode);
      }
    }
  }

// we need to divide creation of nodes and connecting them because
// it may happen that a node points to another node which is not
// created yet and which shall not be created by the node that
// points to it as it is not it's parent

  public void connectSvgNodes(ParseNode parseNode, SvgNode parentNode) {
    SvgNode childNode;

    if (parseNode.hasDownNode()) {
      childNode = sisterNodes.get(parseNode.downNode);
      parentNode.downNode = childNode;
      if (!parseNode.isAncestor(parseNode.downNode) && !parentNode.downNode.hasParent()) {
        parentNode.downNode = childNode;
        childNode.parent = parentNode;
        childNode.col = parentNode.col;
        childNode.row = parentNode.row + 1;
        connectSvgNodes(parseNode.downNode, childNode);
      }
    }
    if (parseNode.hasRightNode()) {
      childNode = sisterNodes.get(parseNode.rightNode);
      parentNode.rightNode = childNode;
      if (parseNode.rightNode.parent.equals(parseNode)) {
        childNode.parent = parentNode;
        childNode.col = parentNode.col + 1;
        childNode.row = parentNode.row;
        connectSvgNodes(parseNode.rightNode, childNode);
      }
    }

  }

  private String getTextInside(ParseNode parseNode) {

    if (parseNode instanceof AntiNode) {
      return "AN";
    } else if (parseNode instanceof OrNode) {
      return "OR";
    } else if (parseNode instanceof LoopNode) {
      if (((LoopNode) parseNode).max < Integer.MAX_VALUE) {
        return "LN:" + ((LoopNode) parseNode).max;
      } else {
        return "LN";
      }
    } else if (parseNode instanceof TerminalNode) {
      return "TN";
    } else if (parseNode instanceof PositionNode) {
      return "PN";
    } else if (parseNode instanceof NonTerminalNode) {
      return (parseNode.name);
    } else {
      return "UNKNOWN";
    }
  }

  public boolean positionSvgNodeTree(SvgNode parentNode) {
    SvgNode childNode, branchNode = null;
    svgNodeList.add(parentNode);
    boolean moved = false;
    // n.b. there might be downNodes which are liked by some node but who do not have a parent (i.e. first NTN-Node)
    // more formally put: parentNode.downNode.parent != parentNode
    if (parentNode.downNode != null && parentNode.downNode.hasParent() && parentNode.downNode.parent.equals(parentNode)) {
      childNode = parentNode.downNode;
      for (SvgNode n : svgNodeList) {
        if (childNode.col == n.col && childNode.row == n.row) {
          branchNode = realignBranch(childNode, n);
          moved = true;
          break;
        }
      }
      if (moved) {
        positionSvgNodeTree(branchNode);
        return true;
      } else {
        if (positionSvgNodeTree(childNode)) {
          return true;
        }
      }
    }
    if (parentNode.rightNode != null) {
      childNode = parentNode.rightNode;
      for (SvgNode n : svgNodeList) {
        if (childNode.col == n.col && childNode.row == n.row) {
          branchNode = realignBranch(childNode, n);
          moved = true;
          break;
        }
      }
      if (moved) {
        positionSvgNodeTree(branchNode);
        return true;
      } else {
        if (positionSvgNodeTree(childNode));
      }
    }
    return moved;
  }

  private SvgNode realignBranch(SvgNode n1, SvgNode n2) {
    SvgNode branch;
    if (n2.parent != null) {
      while (n1.parent.col == n2.parent.col) {
        n1 = n1.parent;
        n2 = n2.parent;
      }
      branch = (n1.parent.col > n2.parent.col) ? n1.parent : n2.parent;
    } else {
      branch = (n1.parent);
    }

    while (branch.parent.col == branch.col) {
      branch = branch.parent;
    }
    svgNodeList.add(new SvgNode(branch.col, branch.row));
    moveBranchColumnRight(branch);
    return branch;
  }

  private void moveBranchColumnRight(SvgNode n) {
    svgNodeList.remove(n);
    n.col += 1;
    if (n.downNode != null && n.downNode.parent.equals(n)) {
      moveBranchColumnRight(n.downNode);
    }
    if (n.rightNode != null) {
      moveBranchColumnRight(n.rightNode);
    }
  }


  public void printSvgNode(SvgNode node) {

    int pushLeft = 19, pushDown = 18;
    if (cnt++ < 5000) {
      switch (node.getNodeType()) {
        case POSITION_NODE:
        {
          sb.append("<rect width='50' height='50'" +
              " x='" + (node.col * 100 + offsetFromLeft) +
              "' y='" + (node.row * 100 + offsetFromTop) +
              "' style='fill:#ffffff;stroke:#000000;stroke-width:1' />\n");
          sb.append("<text x='" + ((node.col * 100) + offsetFromLeft + pushLeft) + "' y='" + (
              (node.row * 100) + 10 + offsetFromTop + pushDown) +
              "' font-family='Arial' font-size='10'>" +
              node.textInside +
              "</text>\n");
        }
        break;
        case ANTI_NODE: {
          sb.append("<rect width='50' height='50'" +
              " x='" + (node.col * 100 + offsetFromLeft) +
              "' y='" + (node.row * 100 + offsetFromTop) +
              "' style='fill:none;stroke:#000000;stroke-width:1' />\n");
          sb.append("<text x='" + ((node.col * 100) + offsetFromLeft + pushLeft - 2) + "' y='" + (
              (node.row * 100) + 10 + offsetFromTop + pushDown) +
              "' font-family='Arial' font-size='14' fill='none'  stroke='#000000' stroke-width='0.1px'>"
              +
              node.textInside +
              "</text>\n");
        }
        break;
        case LOOP_NODE: {
          sb.append("<circle cx='" + (node.col * 100 + 25 + offsetFromLeft) +
              "' cy='" + (node.row * 100 + 25 + offsetFromTop) +
              "' r='25' style='fill:none;stroke:#000000;stroke-width:1' />\n");
          sb.append("<text x='" + ((node.col * 100) + offsetFromLeft + pushLeft) + "' y='" + (
              (node.row * 100) + 10 + offsetFromTop + pushDown) +
              "' font-family='Arial' font-size='10'>" +
              node.textInside +
              "</text>\n");
        }
        break;
        case OR_NODE: {
          sb.append(
              "<polygon points='" + (node.col * 100 + offsetFromLeft) + "," + (node.row * 100 + 25
                  + offsetFromTop) +
                  " " + (node.col * 100 + 25 + offsetFromLeft) + "," + (node.row * 100
                  + offsetFromTop) +
                  " " + (node.col * 100 + 50 + offsetFromLeft) + "," + (node.row * 100 + 25
                  + offsetFromTop) +
                  " " + (node.col * 100 + 25 + offsetFromLeft) + "," + (node.row * 100 + 50
                  + offsetFromTop) +
                  "' style='fill:none;stroke:#000000;stroke-width:1'/>\n"
          );
          sb.append("<text x='" + ((node.col * 100) + offsetFromLeft + pushLeft) + "' y='" + (
              (node.row * 100) + 10 + offsetFromTop + pushDown) +
              "' font-family='Arial' font-size='10'>" +
              node.textInside +
              "</text>\n");
        }
        break;
        case TERMINAL_NODE: {
          // draw octagon
          double centerX = node.col * 100 + offsetFromLeft + 25;
          double centerY = node.row * 100 + offsetFromTop + 25;

          sb.append("<polygon points='"
              + (node.col * 100 + offsetFromLeft) + "," + (node.row * 100 + 16.67 + offsetFromTop) +
              " " + (node.col * 100 + 16.67 + offsetFromLeft) + "," + (node.row * 100
              + offsetFromTop) +
              " " + (node.col * 100 + 33.34 + offsetFromLeft) + "," + (node.row * 100
              + offsetFromTop) +
              " " + (node.col * 100 + 50 + offsetFromLeft) + "," + (node.row * 100 + 16.67
              + offsetFromTop) +
              " " + (node.col * 100 + 50 + offsetFromLeft) + "," + (node.row * 100 + 33.34
              + offsetFromTop) +
              " " + (node.col * 100 + 33.34 + offsetFromLeft) + "," + (node.row * 100 + 50
              + offsetFromTop) +
              " " + (node.col * 100 + 16.67 + offsetFromLeft) + "," + (node.row * 100 + 50
              + offsetFromTop) +
              " " + (node.col * 100 + offsetFromLeft) + "," + (node.row * 100 + 33.34
              + offsetFromTop) +
              " " + (node.col * 100 + offsetFromLeft) + "," + (node.row * 100 + 16.67
              + offsetFromTop) +
              "' style='fill:none;stroke:#000000;stroke-width:1'/>\n"
          );
          sb.append("<text x='" + ((node.col * 100) + offsetFromLeft + pushLeft) + "' y='" + (
              (node.row * 100) + 10 + offsetFromTop + pushDown) +
              "' font-family='Arial' font-size='10'>" +
              node.textInside +
              "</text>\n");

          sb.append("<text x='" + centerX + "' y='" + (centerY + 35) +
              "' font-family='Arial' font-size='10' text-anchor='middle'>" +
              node.textBelow +
              "</text>\n");
        } break;
        case NON_TERMINAL_NODE: {
          sb.append("<rect width='70' height='50'" +
              " x='" + (node.col * 100 - 10 + offsetFromLeft) +
              "' y='" + (node.row * 100 + offsetFromTop) +
              "' style='fill:none;stroke:#000000;stroke-width:1' />\n");
          String[] lines = node.textInside.split("_");
          double longestLine = 0;
          for (String line : lines) {
            double lineWidth = getStringWidth(line);
            if (longestLine < lineWidth) {
              longestLine = lineWidth;
            }
          }

          double scaleFactorCanditate = getScaleFactor(longestLine);
          double scaleFactor = scaleFactorCanditate < 1? scaleFactorCanditate : 1;
          double newLineOffset = 14*scaleFactor;
          double baseLineHight = 8.748;
          double vPadding = (50 - (lines.length * baseLineHight * scaleFactor + (lines.length-1) * newLineOffset)) / 2;
          double lineOffset = 0.0;
          lineOffset += vPadding+baseLineHight;
          for (String line : lines) {
            double textWidth = getStringWidth(line)*scaleFactor;
            double marginLeft;
            if (textWidth <= 60) {
              marginLeft = (70 - textWidth) / 2;
            } else {
              marginLeft = 5;
            }
            sb.append(
                "<g>\n"+
                    "<text "
                        + "x='"+(((node.col * 100) -10) + offsetFromLeft + marginLeft)+"' "
                        + "y='"+((node.row * 100) + offsetFromTop + lineOffset)
                        +"' font-family='sans-serif' font-size='" + getFontSize(longestLine)+"px'>"
            );
            sb.append(line);
            sb.append("</text>\n</g>\n");
            lineOffset += newLineOffset;
          }
        }
        break;
      }
    }
  }


  public void printSvgNodes(SvgNode parentNode) {
    printSvgNode(parentNode);
    if (parentNode.downNode != null && parentNode.downNode.hasParent() && parentNode.downNode.parent.equals(parentNode)) {
      printSvgNodes(parentNode.downNode);
    }
    if (parentNode.rightNode != null) {
      printSvgNodes(parentNode.rightNode);
    }
  }

  public void printParseTreeToFile(String path) throws IOException {
    OutputStream fis = new FileOutputStream(path);
    Writer out = new BufferedWriter(new OutputStreamWriter(fis));
    out.write(sb.toString());
    out.close();
  }

  public SvgNode.ParseNodeType getSvgNodeType(ParseNode parseNode) {

    if (parseNode instanceof AntiNode) {
      return SvgNode.ParseNodeType.ANTI_NODE;
    } else if (parseNode instanceof PositionNode) {
      return SvgNode.ParseNodeType.POSITION_NODE;
    } else if (parseNode instanceof OrNode) {
      return SvgNode.ParseNodeType.OR_NODE;
    } else if (parseNode instanceof LoopNode) {
      return SvgNode.ParseNodeType.LOOP_NODE;
    } else if (parseNode instanceof TerminalNode) {
      return SvgNode.ParseNodeType.TERMINAL_NODE;

    } else if (parseNode instanceof NonTerminalNode) {
      return SvgNode.ParseNodeType.NON_TERMINAL_NODE;

    } else {
      return SvgNode.ParseNodeType.UNKNOWN;
    }
  }

  public void printSvgPaths(SvgNode parentNode) {
    printSvgPath(parentNode);
    if (parentNode.downNode != null && parentNode.downNode.hasParent() && parentNode.downNode.parent.equals(parentNode)) {
      printSvgPaths(parentNode.downNode);
    }
    if (parentNode.rightNode != null) {
      printSvgPaths(parentNode.rightNode);
    }
  }

  public void printSvgPath(SvgNode node) {
    int lane = 0;

    if (node.downNode != null) {

      if (node.col == node.downNode.col) {
        if (node.row < node.downNode.row) {
          //downNode is directly below node
          sb.append(
              "<path d= 'M " + ((node.col * 100) + offsetFromLeft + 25) + " " + ((node.row * 100)
                  + offsetFromTop + 50) +
                  " l " + 0 + " " + ((node.downNode.row - node.row) * 50)
                  + "' fill='none' stroke='black' stroke-width='1' /> \n");
        }


      }
      if (node.col > node.downNode.col) {
        //downNode is to the left of the node
        if (node.row < node.downNode.row) {
          //downNode is below the node
          lane = Math.min(getLane(node), 10);
          sb.append("<path d= 'M " + ((node.col * 100) + offsetFromLeft + 25 + (lane * 2)) + " " + (
              (node.row * 100) + offsetFromTop + 50) +
              " l 0 " + (25 + (lane * 2)) + " l " + ((node.downNode.col - node.col) * 100)
              + " 0 l 0 " + ((node.downNode.row - node.row - 1) * 100 + 25 - (lane * 2)) +
              "' fill='none' stroke='black' stroke-width='1' /> \n");
        } else {
          //downNode is above the node
          lane = Math.min(getLane(node), 10);
          sb.append("<path d= 'M " + ((node.col * 100) + offsetFromLeft + 25 + (lane * 2)) + " " + (
              (node.row * 100) + offsetFromTop + 50) +
              " l 0 " + (25 + (lane * 2)) +
              " l " + ((node.downNode.col - node.col) * 100 + 50 - lane * 4) + " 0 " +
              " l 0 " + (-25 + (lane * 2) + ((node.downNode.row - node.row) * 100 - 50) - (25
              + lane * 2)) +
              " l " + (-40 + lane * 4) + " 0" +
              " l 0 " + (25 - lane * 2) +
              "' fill='none' stroke='black' stroke-width='1' /> \n"
          );
        }
      }

      if (node.col < node.downNode.col) {
        //down node is to the right
        if (node.row < node.downNode.row) {
          //downNode is below the node
          lane = Math.min(getLane(node), 20);
          sb.append("<path d= 'M " + ((node.col * 100) + offsetFromLeft + 25 - (lane * 2)) + " " + (
              (node.row * 100) + offsetFromTop + 50) +
              " l 0 " + (25 - (lane * 2)) + " l " + (node.downNode.col - node.col) * 100 + " 0 l 0 "
              + (25 + (lane * 2)) + "' fill='none' stroke='black' stroke-width='1' /> \n");

        } else {
          //downNode is above or equal to the node
          sb.append("<path d= 'M " + ((node.col * 100) + offsetFromLeft + 25 + (lane * 2)) + " " + (
              (node.row * 100) + offsetFromTop + 50) +
              " l 0 " + (25 + (lane * 2)) +
              " l " + ((node.downNode.col - node.col) * 100 + 50 - lane * 4) + " 0 " +
              " l 0 " + (-25 + (lane * 2) + ((node.downNode.row - node.row) * 100 - 50) - (25
              + lane * 2)) +
              " l " + (-40 + lane * 4) + " 0" +
              " l 0 " + (25 - lane * 2) +
              "' fill='none' stroke='black' stroke-width='1' /> \n"
          );
        }
      }
    }

    if (node.rightNode != null) {
      sb.append("<path d= 'M " + ((node.col * 100) + offsetFromLeft + 50) + " " + ((node.row * 100)
          + offsetFromTop + 25) +
          "l " + (((node.rightNode.col - node.col - 1) * 100) + 50)
          + " 0' fill='none' stroke='black' stroke-width='1' /> \n");
    }
  }

  public int getLane(SvgNode node) {
    int pos = 0;
    if (node.col >= node.downNode.col) {
      //downNode to the left or directly below
      for (SvgNode n : svgNodeList) {
        if (node.downNode.equals(n.downNode)) {
          if (node.col > n.col) {
            pos++;
          }
        }
      }
    } else {
      for (SvgNode n : svgNodeList) {
        if (node.downNode.equals(n.downNode)) {
          if (node.col < n.col) {
            pos++;
          }
        }
      }
    }

    return pos;
  }

  public static class SvgNode {

    int col, row, id;
    String textInside;
    String textBelow;

    public SvgNode(int col, int row) {
      this.col = col;
      this.row = row;
    }

    public SvgNode() {
      this(-5, -5);   //to see misplaced nodes right away
    }

    protected int getCol() {
      return col;
    }
    protected void setCol(int col) {
      this.col = col;
    }
    protected int getRow() {
      return row;
    }
    protected void setRow(int row) {
      this.row = row;
    }
    protected SvgNode getParent() {
      return parent;
    }
    protected void setParent(SvgNode parent) {
      this.parent = parent;
    }
    protected SvgNode getRightNode() {
      return rightNode;
    }
    protected void setRightNode(SvgNode rightNode) {
      this.rightNode = rightNode;
    }
    protected SvgNode getDownNode() {
      return downNode;
    }
    protected void setDownNode(SvgNode downNode) {
      this.downNode = downNode;
    }

    protected boolean hasParent() { return (this.parent != null); }
    protected ParseNodeType getNodeType() {
      return nodeType;
    }
    protected void setNodeType(ParseNodeType nodeType) {
      this.nodeType = nodeType;
    }

    SvgNode parent, rightNode, downNode;
    enum ParseNodeType {POSITION_NODE, OR_NODE, LOOP_NODE, TERMINAL_NODE, NON_TERMINAL_NODE, ANTI_NODE, SUB_NODE, UNKNOWN}
    ParseNodeType nodeType;
  }

  public double getStringWidth(String s) {
    UnicodeString us = new UnicodeString(s);
    double stringWidth = 0;
    for (int i = 0; i < us.length(); i++) {
      stringWidth += getGlyphWidth(us.getStringAt(i));
    }
    return stringWidth;
  }

  public double getFontSize(double width){
    if(width >= 60) {
      return 12 * (60 / width);
    }
    return 12;
  }

  public double getScaleFactor(double width){
    return 60 / width;
  }

  public double getGlyphWidth(String s) {
    switch (s) {
      case "A":
        return 8.45;
      case "B":
        return 7.89;
      case "C":
        return 8.17;
      case "D":
        return 8.92;
      case "E":
        return 7.27;
      case "F":
        return 6.60;
      case "G":
        return 9.03;
      case "H":
        return 8.63;
      case "I":
        return 3.15;
      case "J":
        return 3.45;
      case "K":
        return 7.72;
      case "L":
        return 6.47;
      case "M":
        return 9.97;
      case "N":
        return 8.58;
      case "O":
        return 9.22;
      case "P":
        return 6.98;
      case "Q":
        return 9.22;
      case "R":
        return 8.09;
      case "S":
        return 7.37;
      case "T":
        return 7.17;
      case "U":
        return 8.44;
      case "V":
        return 8.17;
      case "W":
        return 11.73;
      case "X":
        return 8.10;
      case "Y":
        return 7.34;
      case "Z":
        return 8.05;
      case "_":
        return 9.39;
      case " ":
        return 4.0;
      default:
        return 8.0;
    }
  }


}
