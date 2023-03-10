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
package goryachev.rich.simple;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Content pane for TextCell that shows a single region Node.
 * The content gets resized if it cannot fit into available width.
 */
public class NodeCellPane extends Pane {
    private final Region content;
    private final Group imageView;
    private static final Insets PADDING = new Insets(1, 1, 1, 1);

    public NodeCellPane(Region n) {
        this.content = n;

        imageView = new Group(n);
        getChildren().add(imageView);

        setPadding(PADDING);
        getStyleClass().add("node-cell-pane");
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double sc;
        if (width < content.getWidth()) {
            sc = width / content.getWidth();
        } else {
            sc = 1.0;
        }
        imageView.setScaleX(sc);
        imageView.setScaleY(sc);

        double x0 = snappedLeftInset();
        double y0 = snappedTopInset();
        layoutInArea(
            imageView,
            x0,
            y0,
            content.getWidth() * sc,
            content.getHeight() * sc,
            0,
            PADDING,
            true,
            false,
            HPos.CENTER,
            VPos.CENTER
        );
    }

    @Override
    protected double computePrefHeight(double width) {
        double w = content.getWidth();
        if (w == 0.0) {
            return content.prefHeight(width);
        }

        double pad = snappedTopInset() + snappedBottomInset();
        if (width != -1) {
            if (width < w) {
                return pad + (content.getHeight() * width / w);
            }
        }
        return pad + (content.getHeight());
    }
}