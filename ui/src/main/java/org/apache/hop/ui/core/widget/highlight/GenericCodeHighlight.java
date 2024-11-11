/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hop.ui.core.widget.highlight;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.hop.ui.core.gui.GuiResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

public class GenericCodeHighlight implements LineStyleListener {

  CodeScanner scanner;
  int[] tokenColors;
  Color[] colors;
  Vector<int[]> blockComments = new Vector<>();

  public static final int EOF = -1;
  public static final int EOL = 10;

  public static final int WORD = 0;
  public static final int WHITE = 1;
  public static final int KEY = 2;
  public static final int COMMENT = 3; // single line comment: //
  public static final int STRING = 5;
  public static final int OTHER = 6;
  public static final int NUMBER = 7;
  public static final int FUNCTIONS = 8;

  public static final int MAXIMUM_TOKEN = 9;

  public GenericCodeHighlight(ScriptEngine engine) {
    initializeColors();
    scanner = new CodeScanner(engine.getKeywords(), engine.getBuiltInFunctions());
    scanner.setKeywords(engine.getKeywords());
    scanner.setFunctions(engine.getBuiltInFunctions());
  }

  Color getColor(int type) {
    if (type < 0 || type >= tokenColors.length) {
      return null;
    }
    return colors[tokenColors[type]];
  }

  boolean inBlockComment(int start, int end) {
    for (int i = 0; i < blockComments.size(); i++) {
      int[] offsets = blockComments.elementAt(i);
      // start of comment in the line
      if ((offsets[0] >= start) && (offsets[0] <= end)) {
        return true;
      }
      // end of comment in the line
      if ((offsets[1] >= start) && (offsets[1] <= end)) {
        return true;
      }
      if ((offsets[0] <= start) && (offsets[1] >= end)) {
        return true;
      }
    }
    return false;
  }

  void initializeColors() {
    final GuiResource guiResource = GuiResource.getInstance();
    colors =
        new Color[] {
          guiResource.getColorBlack(),
          guiResource.getColorRed(),
          guiResource.getColorDarkGreen(), // DEEM-MOD
          guiResource.getColorBlue(),
          guiResource.getColorOrange()
        };
    tokenColors = new int[MAXIMUM_TOKEN];
    tokenColors[WORD] = 0;
    tokenColors[WHITE] = 0;
    tokenColors[KEY] = 3;
    tokenColors[COMMENT] = 1;
    tokenColors[STRING] = 2;
    tokenColors[OTHER] = 0;
    tokenColors[NUMBER] = 0;
    tokenColors[FUNCTIONS] = 4;
  }

  void disposeColors() {
    for (int i = 0; i < colors.length; i++) {
      colors[i].dispose();
    }
  }

  /**
   * Event.detail line start offset (input) Event.text line text (input) LineStyleEvent.styles
   * Enumeration of StyleRanges, need to be in order. (output) LineStyleEvent.background line
   * background color (output)
   */
  public void lineGetStyle(LineStyleEvent event) {
    Vector<StyleRange> styles = new Vector<>();
    int token;
    StyleRange lastStyle;

    if (inBlockComment(event.lineOffset, event.lineOffset + event.lineText.length())) {
      styles.addElement(
          new StyleRange(event.lineOffset, event.lineText.length() + 4, colors[2], null));
      event.styles = new StyleRange[styles.size()];
      styles.copyInto(event.styles);
      return;
    }
    scanner.setRange(event.lineText);
    String xs = ((StyledText) event.widget).getText();
    if (xs != null) {
      parseBlockComments(xs);
    }
    token = scanner.nextToken();
    while (token != EOF) {
      if (token != OTHER) {
        if ((token == WHITE) && (!styles.isEmpty())) {
          int start = scanner.getStartOffset() + event.lineOffset;
          lastStyle = styles.lastElement();
          if (lastStyle.fontStyle != SWT.NORMAL) {
            if (lastStyle.start + lastStyle.length == start) {
              // have the white space take on the style before it to minimize font style
              // changes
              lastStyle.length += scanner.getLength();
            }
          }
        } else {
          Color color = getColor(token);
          if (color != colors[0]) { // hardcoded default foreground color, black
            StyleRange style =
                new StyleRange(
                    scanner.getStartOffset() + event.lineOffset, scanner.getLength(), color, null);
            if (token == KEY) {
              style.fontStyle = SWT.BOLD;
            }
            if (styles.isEmpty()) {
              styles.addElement(style);
            } else {
              lastStyle = styles.lastElement();
              if (lastStyle.similarTo(style)
                  && (lastStyle.start + lastStyle.length == style.start)) {
                lastStyle.length += style.length;
              } else {
                styles.addElement(style);
              }
            }
          }
        }
      }
      token = scanner.nextToken();
    }
    event.styles = new StyleRange[styles.size()];
    styles.copyInto(event.styles);
  }

  public void parseBlockComments(String text) {
    blockComments = new Vector<>();
    StringReader buffer = new StringReader(text);
    int ch;
    boolean blkComment = false;
    int cnt = 0;
    int[] offsets = new int[2];
    boolean done = false;

    try {
      while (!done) {
        switch (ch = buffer.read()) {
          case -1:
            {
              if (blkComment) {
                offsets[1] = cnt;
                blockComments.addElement(offsets);
              }
              done = true;
              break;
            }
          case '/':
            {
              ch = buffer.read();
              if ((ch == '*') && (!blkComment)) {
                offsets = new int[2];
                offsets[0] = cnt;
                blkComment = true;
                cnt++;
              } else {
                cnt++;
              }
              cnt++;
              break;
            }
          case '*':
            {
              if (blkComment) {
                ch = buffer.read();
                cnt++;
                if (ch == '/') {
                  blkComment = false;
                  offsets[1] = cnt;
                  blockComments.addElement(offsets);
                }
              }
              cnt++;
              break;
            }
          default:
            {
              cnt++;
              break;
            }
        }
      }
    } catch (IOException e) {
      // ignore errors
    }
  }

  /** A simple fuzzy scanner for Java */
  public class CodeScanner {

    protected Map<String, Integer> fgKeys = null;
    protected Map<?, ?> fgFunctions = null;
    protected Map<String, Integer> kfKeys = null;
    protected Map<?, ?> kfFunctions = null;
    protected StringBuilder fBuffer = new StringBuilder();
    protected String fDoc;
    protected int fPos;
    protected int fEnd;
    protected int fStartToken;
    protected boolean fEofSeen = false;

    private List<String> kfKeywords = new ArrayList<>();

    private List<String> fgKeywords = new ArrayList<>();

    public CodeScanner(List<String> keywords, List<String> functions) {
      this.setKeywords(keywords);
      this.setFunctions(functions);
      initialize();
      initializeETLFunctions();
    }

    /** Returns the ending location of the current token in the document. */
    public final int getLength() {
      return fPos - fStartToken;
    }

    /** Initialize the lookup table. */
    void initialize() {
      fgKeys = new Hashtable<>();
      Integer k = Integer.valueOf(KEY);
      for (int i = 0; i < fgKeywords.size(); i++) {
        fgKeys.put(fgKeywords.get(i), k);
      }
    }

    public void setKeywords(List<String> kfKeywords) {
      this.kfKeywords = kfKeywords;
    }

    public void setFunctions(List<String> functions) {
      this.fgKeywords = functions;
    }

    void initializeETLFunctions() {
      kfKeys = new Hashtable<>();
      Integer k = Integer.valueOf(FUNCTIONS);
      for (int i = 0; i < kfKeywords.size(); i++) {
        kfKeys.put(kfKeywords.get(i), k);
      }
    }

    /** Returns the starting location of the current token in the document. */
    public final int getStartOffset() {
      return fStartToken;
    }

    /** Returns the next lexical token in the document. */
    public int nextToken() {
      int c;
      fStartToken = fPos;
      while (true) {
        switch (c = read()) {
          case EOF:
            return EOF;
          case '/': // comment
            c = read();
            if (c == '/') {
              while (true) {
                c = read();
                if ((c == EOF) || (c == EOL)) {
                  unread(c);
                  return COMMENT;
                }
              }
            } else {
              unread(c);
            }
            return OTHER;
          case '\'': // char const
            for (; ; ) {
              c = read();
              switch (c) {
                case '\'':
                  return STRING;
                case EOF:
                  unread(c);
                  return STRING;
                case '\\':
                  c = read();
                  break;
                default:
                  break;
              }
            }

          case '"': // string
            for (; ; ) {
              c = read();
              switch (c) {
                case '"':
                  return STRING;
                case EOF:
                  unread(c);
                  return STRING;
                case '\\':
                  c = read();
                  break;
                default:
                  break;
              }
            }

          case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9':
            do {
              c = read();
            } while (Character.isDigit((char) c));
            unread(c);
            return NUMBER;
          default:
            if (Character.isWhitespace((char) c)) {
              do {
                c = read();
              } while (Character.isWhitespace((char) c));
              unread(c);
              return WHITE;
            }
            if (Character.isJavaIdentifierStart((char) c)) {
              fBuffer.setLength(0);
              do {
                fBuffer.append((char) c);
                c = read();
              } while (Character.isJavaIdentifierPart((char) c));
              unread(c);
              Integer i = fgKeys.get(fBuffer.toString());
              if (i != null) {
                return i.intValue();
              }
              i = kfKeys.get(fBuffer.toString());
              if (i != null) {
                return i.intValue();
              }
              return WORD;
            }
            return OTHER;
        }
      }
    }

    /** Returns next character. */
    protected int read() {
      if (fPos <= fEnd) {
        return fDoc.charAt(fPos++);
      }
      return EOF;
    }

    public void setRange(String text) {
      fDoc = text;
      fPos = 0;
      fEnd = fDoc.length() - 1;
    }

    protected void unread(int c) {
      if (c != EOF) {
        fPos--;
      }
    }
  }
}
