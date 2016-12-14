/*
 * Copyright (c) 2016 David Shen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.pantherman594.todo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 12/13.
 *
 * @author david
 */
class Row {

    private int index;
    private String text;
    private boolean checked;

    Row(int index, String text, boolean checked) {
        this.index = index;
        this.text = text;
        this.checked = checked;

        GridPane grid = Todo.getInstance().getGrid();

        CheckBox complete = new CheckBox();
        complete.setSelected(checked);
        complete.setOnAction(ev -> {
            this.checked = complete.isSelected();
            for (Node child : Todo.getInstance().getGrid().getChildren()) {
                if (GridPane.getRowIndex(child).equals(GridPane.getRowIndex(complete)) && child instanceof Text) {
                    Text itemText = (Text) child;
                    Font font = itemText.getFont();
                    if (this.checked) {
                        itemText.setStrikethrough(true);
                        itemText.setFont(Font.font(font.getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, font.getSize()));
                        itemText.setFill(Color.GRAY);
                    } else {
                        itemText.setStrikethrough(false);
                        itemText.setFont(Font.font(font.getFamily(), FontWeight.NORMAL, FontPosture.REGULAR, font.getSize()));
                        itemText.setFill(Color.BLACK);
                    }
                    break;
                }
            }
            Todo.getInstance().updateTitle();
        });
        grid.add(complete, 0, index);

        Text item = new Text(text);
        if (checked) {
            Font font = item.getFont();
            item.setStrikethrough(true);
            item.setFont(Font.font(font.getFamily(), FontWeight.NORMAL, FontPosture.ITALIC, font.getSize()));
            item.setFill(Color.GRAY);
        }
        grid.add(item, 1, index);

        // Edit task button
        HBox editBox = new HBox(10);
        editBox.setMinWidth(25);
        editBox.setAlignment(Pos.CENTER_RIGHT);

        Button edit = new Button("\u270E");
        edit.setTooltip(new Tooltip("Edit this task."));
        edit.setFont(Font.font(18));
        edit.setPadding(new Insets(3, 3, 3, 3));

        edit.setOnAction(e -> this.remove(true));

        editBox.getChildren().add(edit);
        grid.add(editBox, 3, index);

        // Delete task button
        HBox trashBox = new HBox(10);
        trashBox.setAlignment(Pos.CENTER_RIGHT);

        Button trashCan = new Button("\uD83D\uDDD1");
        trashCan.setTooltip(new Tooltip("Delete this task."));
        trashCan.setFont(Font.font(18));
        trashCan.setPadding(new Insets(3, 6, 3, 6));

        trashCan.setOnAction(e -> this.remove(false));

        trashBox.getChildren().add(trashCan);
        grid.add(trashBox, 4, index);
    }

    void remove(boolean edit) {
        Todo.getInstance().getGrid().getChildren().removeIf(child -> GridPane.getRowIndex(child) == index);
        Todo.getInstance().getRows().remove(index);
        Map<Integer, Row> rows = new HashMap<>(Todo.getInstance().getRows());
        for (Row row : rows.values()) {
            if (row.getIndex() > index) {
                row.move(row.getIndex() - 1);
                break;
            }
        }

        Todo.getInstance().getGrid().getChildren().removeIf(child -> child instanceof TextField);
        TextField entry = Todo.getInstance().newEntry();

        if (edit) entry.setText(text);
        Todo.getInstance().updateTitle();
    }

    private void move(int toIndex) {
        Todo.getInstance().getRows().remove(index);
        Todo.getInstance().getRows().put(toIndex, new Row(toIndex, text, checked));
        this.remove(false);
    }

    private int getIndex() {
        return index;
    }

    String getText() {
        return text;
    }

    boolean isChecked() {
        return checked;
    }
}
