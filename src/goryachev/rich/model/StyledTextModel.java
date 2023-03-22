/*
 * Copyright (c) 2022, 2023, Oracle and/or its affiliates. All rights reserved.
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
// This code borrows heavily from the following project, with permission from the author:
// https://github.com/andy-goryachev/FxEditor
package goryachev.rich.model;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.scene.input.DataFormat;
import goryachev.rich.Marker;
import goryachev.rich.RichTextArea;
import goryachev.rich.TextPos;
import goryachev.rich.impl.Markers;
import goryachev.rich.util.Util;

/**
 * Base class for a styled text model for use with {@link RichTextArea}.
 * The text is considered to be a collection of paragraphs, represented by {@link StyledParagraph} class.
 * 
 * TODO printing
 */
public abstract class StyledTextModel {
    public interface ChangeListener {
        /**
         * Indicates a change in the model text.
         * The listeners are updated *after* the corresponding changes have been made to the model.
         * 
         * @param start start of the affected text block
         * @param end end of the affected text block
         * @param charsAddedTop number of characters inserted on the same line as start
         * @param linesAdded the number of paragraphs inserted between start and end
         * @param charsAddedBottom number of characters inserted on the same line as end
         */
        public void eventTextUpdated(TextPos start, TextPos end, int charsAddedTop, int linesAdded, int charsAddedBottom);
        
        /**
         * Indicates a change in styles only, with no change in the model text.
         * @param start start of the affected text block
         * @param end end of the affected text block
         */
        public void eventStyleUpdated(TextPos start, TextPos end);
    }
    
    /**
     * Indicates whether the model is editable.
     */
    public abstract boolean isEditable();

    /**
     * Returns the number of paragraphs in the model.
     */
    public abstract int getParagraphCount();

    /**
     * Returns the specified paragraph.  The caller should never attempt to ask for a paragraph outside of the
     * valid range.
     *
     * @param index paragraph index in the range (0...{@link getParagraphCount()})
     */
    public abstract StyledParagraph getParagraph(int index);
    
    /**
     * This method gets called only by an editable model.
     * start is guaranteed to precede end.
     * 
     * @param start
     * @param end
     */
    protected abstract void removeRegion(TextPos start, TextPos end);

    /**
     * This method is called to insert a single text segment at the given position.
     * @return the character count of the inserted text
     */
    protected abstract int insertTextSegment(int index, int offset, StyledSegment text);

    /** inserts a line break */
    protected abstract void insertLineBreak(int index, int offset);
    
    /** inserts a paragraph node */
    protected abstract void insertParagraph(int index, StyledSegment segment);
    
    /**
     * Exports part of the paragraph as a sequence of styled segments.
     * 
     * @param index paragraph's model index
     * @param startOffset start offset
     * @param endOffset end offset.  if -1 then export to the paragraph length
     * @param out
     */
    protected abstract void exportSegments(int index, int startOffset, int endOffset, StyledOutput out);
    
    /**
     * Applies a style to the specified text range.
     * 
     * @param start start position
     * @param end end position (may or may not be after the start position)
     * @param attrs attribute map
     */
    public abstract void applyStyle(TextPos start, TextPos end, StyleAttrs attrs);
    
    /**
     * Removes a style from the specified text range.
     * 
     * @param start start position
     * @param end end position (may or may not be after the start position)
     * @param attrs attribute map
     */
    public abstract void removeStyle(TextPos start, TextPos end, StyleAttrs attrs);

    /**
     * Returns the styled attributes of the first character at the specified position.
     * When at the end of the document, returns the attributes of the last character.
     *
     * @return non-null {@link StyleAttrs}
     */
    public abstract StyleAttrs getStyledAttrs(TextPos pos);
    
    private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList();
    private final HashMap<DataFormat,DataFormatHandler> handlers = new HashMap<>(4);
    private final Markers markers = new Markers();
    // TODO special BEGIN/END markers? especially END (would need a leading/trailing bias then)

    public StyledTextModel() {
    }

    /**
     * Returns the plain text string for the specified paragraph.
     * The caller should never attempt to ask for a paragraph outside of the valid range.
     * 
     * The default implementation requests a plain text string from StyledParagraph;
     * models that have a cheaper access to the plain text should override this method. 
     *
     * @param index paragraph index in the range (0...{@link getParagraphCount()})
     */
    public String getPlainText(int index) {
        StyledParagraph p = getParagraph(index);
        return p.getText();
    }
    
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public DataFormatHandler registerDataFormatHandler(DataFormatHandler h) {
        return handlers.put(h.getDataFormat(), h);
    }

    /** returns supported data formats */
    public DataFormat[] getSupportedDataFormats() {
        // TODO must come in specific order: from richer to simpler
        Set<DataFormat> formats = handlers.keySet();
        return formats.toArray(new DataFormat[formats.size()]);
    }

    public DataFormatHandler getDataFormatHandler(DataFormat format) {
        return handlers.get(format);
    }

    /**
     * Replaces the given range with the provided plain text.
     * This is a convenience method that calls {@link #replace(TextPos,TextPos,StyledInput)}
     *
     * @param start
     * @param end
     * @param text
     * @return
     */
    public TextPos replace(TextPos start, TextPos end, String text) {
        if (isEditable()) {
            // TODO get style
            String direct = null;
            String[] css = null;
            return replace(start, end, StyledInput.of(text, direct, css));
        }
        return null;
    }
    
    /**
     * Replaces the given range with the provided styled text.
     * When inserting a plain text, the style is taken from preceding text segment, or, if the text is being
     * inserted into the beginning of the document, the style is taken from the following text segment.
     * 
     * After the model applies the requested changes, an event is sent to all the registered ChangeListeners.
     * 
     * @param start start position
     * @param end end position
     * @param input StyledInput
     * @return text position at the end of the inserted text, or null if the model is read only
     */
    public TextPos replace(TextPos start, TextPos end, StyledInput input) {
        if (isEditable()) {
            int cmp = start.compareTo(end);
            if (cmp > 0) {
                // make sure start < end
                TextPos p = start;
                start = end;
                end = p;
            }
            
            if(cmp != 0) {
                removeRegion(start, end);
            }

            int index = start.index();
            int offset = start.offset();
            int top = 0;
            int btm = 0;
            
            StyledSegment seg;
            while ((seg = input.nextSegment()) != null) {
                if(seg.isParagraph()) {
                    offset = 0;
                    btm = 0;
                    index++;
                    insertParagraph(index, seg);
                } else if(seg.isText()) {
                    int len = insertTextSegment(index, offset, seg);
                    if(index == start.index()) {
                        top += len;
                    }
                    offset += len;
                    btm += len;
                } else if(seg.isLineBreak()) {
                    insertLineBreak(index, offset);
                    index++;
                    offset = 0;
                    btm = 0;
                }
            }

            int lines = index - start.index();
            if (lines == 0) {
                btm = 0;
            }

            fireChangeEvent(start, end, top, lines, btm);

            return new TextPos(index, offset);
        }
        return null;
    }
    
    protected void fireChangeEvent(TextPos start, TextPos end, int charsTop, int linesAdded, int charsBottom) {
        System.out.println("fireChangeEvent start=" + start + " end=" + end + " top=" + charsTop + " lines=" + linesAdded + " btm=" + charsBottom); // FIX
        markers.update(start, end, charsTop, linesAdded, charsBottom);

        for (ChangeListener li : listeners) {
            li.eventTextUpdated(start, end, charsTop, linesAdded, charsBottom);
        }
    }
    
    protected void fireStyleChangeEvent(TextPos start, TextPos end) {
        //System.out.println("fireChangeEvent start=" + start + " end=" + end); // FIX
        for (ChangeListener li : listeners) {
            li.eventStyleUpdated(start, end);
        }
    }

    public void exportText(TextPos start, TextPos end, StyledOutput out) {
        int cmp = start.compareTo(end);
        if (cmp > 0) {
            // make sure start < end
            TextPos p = start;
            start = end;
            end = p;
        }

        int ix0 = start.index();
        int ix1 = end.index();
        if (ix0 == ix1) {
            // part of one line
            exportSegments(start.index(), start.offset(), end.offset(), out);
        } else {
            // multi-line
            boolean lineBreak = false;
            for(int ix=start.index(); ix<=end.index(); ix++) {
                if(lineBreak) {
                    out.append(StyledSegment.LINE_BREAK);
                } else {
                    lineBreak = true;
                }
                
                int off0;
                int off1;
                if(ix == ix0) {
                    off0 = start.offset();
                    off1 = -1;
                } else if(ix == ix1) {
                    off0 = 0;
                    off1 = end.offset();
                } else {
                    off0 = 0;
                    off1 = -1;
                }
                
                exportSegments(ix, off0, off1, out);
            }
        }
    }

    public Marker getMarker(TextPos pos) {
        TextPos p = clamp(pos);
        return markers.getMarker(p);
    }

    protected TextPos clamp(TextPos p) {
        int ct = getParagraphCount();
        int ix = p.index();
        if (ix < 0) {
            return TextPos.ZERO;
        } else if (ix < ct) {
            return p;
        } else {
            if (ct == 0) {
                return new TextPos(0, 0);
            } else {
                ix = ct - 1;
                String s = getPlainText(ix);
                int len = (s == null) ? 0 : s.length();
                return new TextPos(ct - 1, len);
            }
        }
    }

    /** exports plain text segments only */
    protected void exportPlaintextSegments(int index, int startOffset, int endOffset, StyledOutput out) {
        String text = getPlainText(index);
        text = Util.substring(text, startOffset, endOffset);
        StringStyledSegment seg = new StringStyledSegment(text, null, null);
        out.append(seg);
    }

    /** returns a TextPos corresponding to the end of the document */
    public TextPos getEndTextPos() {
        int ix = getParagraphCount() - 1;
        if (ix < 0) {
            return TextPos.ZERO;
        } else {
            String text = getPlainText(ix);
            int off = text == null ? 0 : text.length();
            return new TextPos(ix, off);
        }
    }
}