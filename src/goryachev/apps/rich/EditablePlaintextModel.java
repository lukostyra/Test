/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package goryachev.apps.rich;

import java.util.ArrayList;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import goryachev.rich.StyledParagraph;
import goryachev.rich.StyledTextModel;
import goryachev.rich.TextCell;
import goryachev.rich.TextPos;

public class EditablePlaintextModel extends StyledTextModel {
    private final ArrayList<String> paragraphs = new ArrayList();

    public EditablePlaintextModel() {
        paragraphs.add("");
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public int getParagraphCount() {
        return paragraphs.size();
    }

    @Override
    public String getPlainText(int index) {
        return paragraphs.get(index);
    }

    @Override
    public StyledParagraph getParagraph(int index) {
        return new StyledParagraph() {
            @Override
            public String getPlainText() {
                return EditablePlaintextModel.this.getPlainText(index);
            }
            
            @Override
            public int getIndex() {
                return index;
            }
            
            @Override
            public TextCell createTextCell() {
                String text = getPlainText();
                TextFlow f = new TextFlow(new Text(text));
                return new TextCell(index, f);
            }
        };
    }

    @Override
    public void replace(TextPos start, TextPos end, String text) {
        System.err.println("replace start=" + start + " end=" + end + " text=[" + text + "]"); // FIX

        // update paragraphs
        // update markers
        // fire event
        int len = text.length();

        removeRegion(start, end);

        int ix = start.lineIndex();
        int cix = start.getInsertionIndex();
        String s = paragraphs.get(ix);

        // TODO insert new line, needs a different code path

        String s2 = insertText(s, cix, text);
        paragraphs.set(ix, s2);

        fireChangeEvent(start, end, len, 0, 0);
    }
    
    @Override
    protected void insertLineBreak(TextPos pos) {
        System.err.println("insertLineBreak pos=" + pos); // FIX
        // TODO clamp position here? or presume all is ok?
        int ix = pos.lineIndex();
        if(ix >= getParagraphCount()) {
            paragraphs.add("");
        } else {
            int cix = pos.getInsertionIndex();
            String s = paragraphs.get(ix);
            if(cix >= s.length()) {
                paragraphs.add(ix + 1, "");
            } else {
                paragraphs.set(ix, s.substring(0, cix));
                paragraphs.add(ix + 1, s.substring(cix));
            }
        }
        fireChangeEvent(pos, pos, 0, 1, 0);
    }

    private static String insertText(String text, int index, String toInsert) {
        // TODO handle null text!
        if (index >= text.length()) {
            return text + toInsert;
        } else {
            return text.substring(0, index) + toInsert + text.substring(index);
        }
    }

    private void removeRegion(TextPos start, TextPos end) {
        String s2;
        int ix = start.lineIndex();
        String text = paragraphs.get(ix);

        if (ix == end.lineIndex()) {
            // TODO handle null text!
            int len = text.length();
            if (end.charIndex() >= len) {
                s2 = text.substring(0, start.charIndex());
            } else {
                s2 = text.substring(0, start.charIndex()) + text.substring(end.charIndex());
            }
            paragraphs.set(ix, s2);
        } else {
            // TODO check for document end here
            s2 = text.substring(0, start.charIndex());
            paragraphs.set(ix, s2);

            int ct = end.lineIndex() - ix - 1;
            ix++;
            for (int i = 0; i < ct; i++) {
                paragraphs.remove(ix);
            }
            ix++;
            text = paragraphs.get(ix);
            s2 = text.substring(end.charIndex());
            paragraphs.set(ix, s2);
        }
    }
}
