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
package goryachev.rich;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

public class RichTextArea extends Control {
    private ObjectProperty<StyledTextModel> model;
    private final ReadOnlyIntegerWrapper currentLine = new ReadOnlyIntegerWrapper(-1);
    private final SimpleBooleanProperty displayCaretProperty = new SimpleBooleanProperty(true);

    public RichTextArea() {
        getStyleClass().add("rich-text-area");
        getStyleClass().add("text-input");
        setAccessibleRole(AccessibleRole.TEXT_AREA);
        setSkin(createDefaultSkin());
    }

    @Override
    protected RichTextAreaSkin createDefaultSkin() {
        return new RichTextAreaSkin(this);
    }

    public void setModel(StyledTextModel m) {
        modelProperty().set(m);
    }

    public StyledTextModel getModel() {
        return (model == null ? null : model.get());
    }

    public ObjectProperty<StyledTextModel> modelProperty() {
        if (model == null) {
            model = new SimpleObjectProperty<>(this, "model") {
                @Override
                protected void invalidated() {
                    updateModel();
                }
            };
        }
        return model;
    }
    
    /**
     * If a run of text exceeds the width of the {@code TextArea},
     * then this variable indicates whether the text should wrap onto
     * another line.
     */
    private StyleableBooleanProperty wrapText = new StyleableBooleanProperty(false) {
        @Override public Object getBean() {
            return RichTextArea.this;
        }

        @Override public String getName() {
            return "wrapText";
        }

        @Override public CssMetaData<RichTextArea,Boolean> getCssMetaData() {
            return StyleableProperties.WRAP_TEXT;
        }
    };
    public final BooleanProperty wrapTextProperty() { return wrapText; }
    public final boolean isWrapText() { return wrapText.getValue(); }
    public final void setWrapText(boolean value) { wrapText.setValue(value); }
    
    public void setDisplayCaret(boolean on) {
        displayCaretProperty.set(on);
    }

    public boolean isDisplayCaret() {
        return displayCaretProperty.get();
    }

    public BooleanProperty displayCaretProperty() {
        return displayCaretProperty;
    }
    
    public int getCurrentLine() {
        return currentLine.get();
    }
    
    public void moveCurrentLine(int n) {
        // TODO clip
    }
    
    public ReadOnlyIntegerProperty currentLineProperty() {
        return currentLine.getReadOnlyProperty();
    }
    
    public boolean isEditable() {
        return false;
    }
    
    protected void updateModel() {
        // TODO
    }
    
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        switch (attribute) {
        // TODO possibly large text - could we send just what is displayed?
//        case TEXT: {
//            String accText = getAccessibleText();
//            if (accText != null && !accText.isEmpty())
//                return accText;
//
//            String text = getText();
//            if (text == null || text.isEmpty()) {
//                text = getPromptText();
//            }
//            return text;
//        }
        case EDITABLE:
            return isEditable();
//        case SELECTION_START:
//            return getSelection().getStart();
//        case SELECTION_END:
//            return getSelection().getEnd();
//        case CARET_OFFSET:
//            return getCaretPosition();
//        case FONT:
//            return getFont();
        default:
            return super.queryAccessibleAttribute(attribute, parameters);
        }
    }
    
    private static class StyleableProperties {
        private static final CssMetaData<RichTextArea,Boolean> WRAP_TEXT =
            new CssMetaData<>("-fx-wrap-text", StyleConverter.getBooleanConverter(), false) {

            @Override
            public boolean isSettable(RichTextArea t) {
                return !t.wrapText.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(RichTextArea t) {
                return (StyleableProperty<Boolean>)t.wrapTextProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = Util.initStyleables(
            Control.getClassCssMetaData(),
            WRAP_TEXT
        );
    }

    /**
     * Gets the {@code CssMetaData} associated with this class, which may include the
     * {@code CssMetaData} of its superclasses.
     * @return the {@code CssMetaData}
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
}
