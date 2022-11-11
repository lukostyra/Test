/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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
package goryachev.apps;

import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 *
 */
public class ComboBoxPane extends ToolPane {
    private ComboBox comboBox;

    public ComboBoxPane() {
        comboBox = new ComboBox();
        comboBox.getItems().addAll("0","1","2","3","4","5","6","7","8","9");
        
        VBox b = new VBox();
        b.getChildren().add(comboBox);
        setCenter(b);
        
        addButton("Set Converter", () -> {
            comboBox.setConverter(new StringConverter() {
                int toStringCounter = 0;
                int fromStringCounter = 0;

                @Override public String toString(Object t) {
                    return "toString-" + t;
                }

                @Override public Object fromString(String t) {
                    return "fromString" + t;
                }
            });
        });
    }
}
