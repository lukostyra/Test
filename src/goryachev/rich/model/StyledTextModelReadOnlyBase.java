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
package goryachev.rich.model;

import java.io.IOException;
import java.util.function.Supplier;
import javafx.scene.Node;
import goryachev.rich.TextPos;

/**
 * Read-only StyledTextModel base class.
 */
public abstract class StyledTextModelReadOnlyBase extends StyledTextModel {
    public StyledTextModelReadOnlyBase() {
        registerDataFormatHandler(new PlainTextFormatHandler(), 0);
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    protected void removeRegion(TextPos start, TextPos end) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int insertTextSegment(int index, int offset, StyledSegment text) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void insertLineBreak(int index, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void insertParagraph(int index, Supplier<Node> generator) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean applyStyleImpl(TextPos start, TextPos end, StyleAttrs attrs) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void exportParagraph(int index, int start, int end, StyledOutput out) throws IOException {
        exportPlaintextSegments(index, start, end, out);
    }
}
