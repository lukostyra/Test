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

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * RichTextArea Behavior.
 *
 * FIX BehaviorBase and InputMap are not public!
 */
public class RichTextAreaBehavior {
    private final RichTextAreaSkin skin;
    private final RichTextArea control;
    private final InputMap2 inputMap;

    public RichTextAreaBehavior(RichTextAreaSkin skin) {
        this.skin = skin;
        this.control = skin.getSkinnable();
        
        inputMap = createInputMap();
    }
    
    protected InputMap2 createInputMap() {
        // we could possibly pass control to the InputMap2 constructor.
        InputMap2 m = new InputMap2();
        m.add(this::pageDown, KeyCode.PAGE_DOWN);
        m.add(this::pageUp, KeyCode.PAGE_UP);
        m.add(this::selectAll, KeyCode.A, InputMap2.Modifier.SHORTCUT);
        return m;
    }

    public void install() {
        VFlow f = vflow();
        f.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
        f.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        f.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        f.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        f.addEventFilter(ScrollEvent.ANY, this::handleScrollEvent);

        control.addEventHandler(KeyEvent.ANY, inputMap);
    }

    public void dispose() {
        control.removeEventHandler(KeyEvent.ANY, inputMap);
    }

    protected VFlow vflow() {
        return skin.getVFlow();
    }

    protected void handleMouseClicked(MouseEvent ev) {
        if (ev.getButton() == MouseButton.PRIMARY) {
            int clicks = ev.getClickCount();
            switch (clicks) {
            case 2:
                control.selectWord(getTextPosition(ev));
                break;
            case 3:
                control.selectLine(getTextPosition(ev));
                break;
            }
        }
    }

    protected void handleMousePressed(MouseEvent ev) {
        // TODO
        if (ev.isPopupTrigger()) {
            // TODO clear selection if click happened outside of said selection?
            return;
        }

        SelectionModel sm = control.getSelectionModel();
        if (sm == null) {
            return;
        }

        Marker pos = getTextPosition(ev);
        if (pos == null) {
            return;
        }

        control.setSuppressBlink(true);

        if (ev.isShiftDown()) {
            // expand selection from the anchor point to the current position
            // clearing existing (possibly multiple) selection
            sm.clearAndExtendLastSegment(pos);
        } else {
            sm.setSelection(pos, pos);
            sm.setAnchor(pos);
        }

        control.setCaretPosition(pos.getTextPos());
        control.requestFocus();
    }

    protected void handleMouseReleased(MouseEvent ev) {
        // TODO is popup trigger?
        //stopAutoScroll(); // TODO
        control.setSuppressBlink(false);
        //control.commitselection TODO
    }

    protected void handleMouseDragged(MouseEvent ev) {
        // TODO
    }

    protected void handleScrollEvent(ScrollEvent ev) {
        if (ev.isShiftDown()) {
            // TODO horizontal scroll
        } else if (ev.isShortcutDown()) {
            // page up / page down
            if (ev.getDeltaY() >= 0) {
                vflow().pageUp();
            } else {
                vflow().pageDown();
            }
        } else {
            // block scroll
            double f = Config.scrollWheelBlockSize;
            if (ev.getDeltaY() >= 0) {
                f = -f;
            }
            vflow().scroll(f);
        }
    }

    protected Marker getTextPosition(MouseEvent ev) {
        double x = ev.getScreenX();
        double y = ev.getScreenY();
        return control.getTextPosition(x, y);
    }
    
    public void pageUp() {
        // TODO
    }
    
    public void pageDown() {
        // TODO
    }
    
    public void selectAll() {
        StyledTextModel m = control.getModel();
        if(m != null) {
            int ix = m.getParagraphCount() - 1;
            if (ix >= 0) {
                String text = m.getPlainText(ix);
                int cix = (text == null ? 0 : Math.max(0, text.length() - 1));
                Marker end = control.newMarker(ix, cix, false);
                control.getSelectionModel().setSelection(Marker.ZERO, end);
            }
        }
    }
}
