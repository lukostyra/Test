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

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SkinBase;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import goryachev.rich.util.NewAPI;

/**
 * Provides visual representation for RichTextArea.
 * <p>
 * This skin manages a number of components:
 * <ul>
 * <li>virtual flow Region
 * <li>horizontal scroll bar
 * <li>vertical scroll bar
 * </ul>
 */
public class RichTextAreaSkin extends SkinBase<RichTextArea> {
    private final RichTextAreaBehavior behavior;
    private final VFlow vflow;
    private final ScrollBar vscroll;
    private final ScrollBar hscroll;

    protected RichTextAreaSkin(RichTextArea control) {
        super(control);
        
        vscroll = createVScrollBar();
        vscroll.setOrientation(Orientation.VERTICAL);
        vscroll.setManaged(true);
        vscroll.setMin(0.0);
        vscroll.setMax(1.0);
        vscroll.addEventFilter(ScrollEvent.ANY, (ev) -> ev.consume());

        hscroll = createVScrollBar();
        hscroll.setOrientation(Orientation.HORIZONTAL);
        hscroll.setManaged(true);
        hscroll.setMin(0.0);
        hscroll.setMax(1.0);
        hscroll.addEventFilter(ScrollEvent.ANY, (ev) -> ev.consume());
        hscroll.visibleProperty().bind(control.wrapTextProperty().not());

        vflow = new VFlow(control, vscroll, hscroll);

        // TODO corner? only when both scroll bars are visible

        getChildren().addAll(new Pane(vflow, vscroll, hscroll) {
            protected void layoutChildren() {
                double x0 = snappedLeftInset();
                double y0 = snappedTopInset();
                double width = getWidth() - x0 - snappedRightInset();
                double height = getHeight() - y0 - snappedBottomInset();

                double vscrollWidth = 0.0;
                if (vscroll.isVisible()) {
                    vscrollWidth = vscroll.prefWidth(-1);
                }

                double hscrollHeight = 0.0;
                if (hscroll.isVisible()) {
                    hscrollHeight = hscroll.prefHeight(-1);
                }

                double w = snapSizeX(width - vscrollWidth - 1.0);
                double h = snapSizeY(height - hscrollHeight - 1.0);

                layoutInArea(vscroll, w, y0 + 1.0, vscrollWidth, h, -1, null, true, true, HPos.RIGHT, VPos.TOP);
                layoutInArea(hscroll, x0 + 1, h, w, hscrollHeight, -1, null, true, true, HPos.LEFT, VPos.BOTTOM);
                layoutInArea(vflow, x0, y0, w, h, -1, null, true, true, HPos.LEFT, VPos.TOP);
            }
        });

        this.behavior = new RichTextAreaBehavior(control);
        
        createMouseHandler().register(vflow);

        // TODO protect with listener helper (it's internal, shoud be made public) to avoid memory leak when changing skins
        NewAPI.addChangeListener(vflow::updateCaretAndSelection, false, control.getSelectionModel().selectionSegmentProperty());
        NewAPI.addChangeListener(vflow::updateRateRestartBlink, true, control.caretBlinkPeriodProperty());
    }

    @Override
    public void install() {
        // TODO
    }

    @Override
    public void dispose() {
        if (getSkinnable() == null) {
            return;
        }

        behavior.dispose();

        super.dispose();
    }

    /** called from the constructor.  override to provide a custom scroll bar */
    protected ScrollBar createVScrollBar() {
        return new ScrollBar();
    }

    /** called from the constructor.  override to provide a custom scroll bar */
    protected ScrollBar createHScrollBar() {
        return new ScrollBar();
    }

    /** called from the constructor.  override to provide a custom MouseHandler */
    protected MouseHandler createMouseHandler() {
        return new MouseHandler(getSkinnable());
    }

    public VFlow getVFlow() {
        return vflow;
    }
}