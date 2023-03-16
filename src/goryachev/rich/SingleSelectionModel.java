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
package goryachev.rich;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * This SelectionModel supports a single selection segment.
 */
public class SingleSelectionModel implements SelectionModel {
    private final ReadOnlyObjectWrapper<SelectionSegment> segment = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<TextPos> anchorPosition = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<TextPos> caretPosition = new ReadOnlyObjectWrapper<>();
    private final ChangeListener<TextPos> listener;

    public SingleSelectionModel() {
        // TODO somehow we are missing marker changes here... why?
        this.listener = (src, old, val) -> {
            if (isAnchor(src)) {
                //System.out.println("SelectionModel.listener anchor=" + val); // FIX
                anchorPosition.set(val);
            } else {
                //System.out.println("SelectionModel.listener caret=" + val); // FIX
                caretPosition.set(val);
            }
        };
    }

    @Override
    public void clear() {
        setSelectionSegment(null);
    }

    @Override
    public void setSelection(Marker an, Marker ca) {
        SelectionSegment seg = new SelectionSegment(an, ca);
        setSelectionSegment(seg);
    }

    @Override
    public void extendSelection(Marker pos) {
        Marker a = anchor();
        if (a == null) {
            a = pos;
        }
        setSelection(a, pos);
    }

    @Override
    public ReadOnlyProperty<TextPos> anchorPositionProperty() {
        return anchorPosition.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyProperty<TextPos> caretPositionProperty() {
        return caretPosition;
    }

    private void setSelectionSegment(SelectionSegment seg) {
        Marker m = anchor();
        if (m != null) {
            m.textPosProperty().removeListener(listener);
        }

        m = caret();
        if (m != null) {
            m.textPosProperty().removeListener(listener);
        }

        if (seg == null) {
            anchorPosition.set(null);
            caretPosition.set(null);
        } else {
            seg.getAnchor().textPosProperty().addListener(listener);
            seg.getCaret().textPosProperty().addListener(listener);
            anchorPosition.set(seg.getAnchor().getTextPos());
            caretPosition.set(seg.getCaret().getTextPos());
        }

        segment.set(seg);
    }

    private Marker anchor() {
        SelectionSegment seg = getSelectionSegment();
        if (seg == null) {
            return null;
        }
        return seg.getAnchor();
    }

    private Marker caret() {
        SelectionSegment seg = getSelectionSegment();
        if (seg == null) {
            return null;
        }
        return seg.getCaret();
    }

    private boolean isAnchor(ObservableValue<? extends TextPos> src) {
        Marker an = anchor();
        if (an != null) {
            return an.textPosProperty() == src;
        } else {
            Marker ca = caret();
            if (ca != null) {
                return ca.textPosProperty() != src;
            }
        }
        return false;
    }

    @Override
    public ReadOnlyProperty<SelectionSegment> selectionSegmentProperty() {
        return segment.getReadOnlyProperty();
    }

    @Override
    public SelectionSegment getSelectionSegment() {
        return segment.get();
    }
}
