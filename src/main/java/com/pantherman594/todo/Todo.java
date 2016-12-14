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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 12/13.
 *
 * @author david
 */
public class Todo extends Application {
    private static Todo instance;
    private GridPane grid;
    private Map<Integer, Row> rows;

    private Text title;
    private File data;

    public static void main(String[] args) {
        launch(args);
    }

    static Todo getInstance() {
        return instance;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void start(Stage pS) throws Exception {
        instance = this;
        rows = new HashMap<>();

        data = new File("todo_data.txt");
        grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 300, 400);
        if (Integer.parseInt(System.getProperty("java.version").split("\\.")[1]) >= 8) {

            title = new Text("Todo List (0/0)");
            title.setFont(Font.font("Lato, Tahoma", FontWeight.NORMAL, 18));
            grid.add(title, 0, 0, 4, 1);

            HBox trashBox = new HBox(10);
            trashBox.setAlignment(Pos.CENTER_RIGHT);

            Button trashCan = new Button("\uD83D\uDDD1");
            trashCan.setTooltip(new Tooltip("Delete all completed tasks."));
            trashCan.setFont(Font.font(18));
            trashCan.setPadding(new Insets(3, 6, 3, 6));

            trashCan.setOnAction(e -> {
                while (rows.values().stream().filter(Row::isChecked).count() > 0) {
                    for (Row row : rows.values()) {
                        if (row.isChecked()) {
                            row.remove(false);
                            break;
                        }
                    }
                }
            });

            trashBox.getChildren().add(trashCan);
            grid.add(trashBox, 4, 0);

            if (data.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(data));
                String line;
                int i = 1;
                while ((line = reader.readLine()) != null) {
                    String text = line.substring(4);
                    if (line.startsWith("com;")) {
                        rows.put(i, new Row(i, text, true));
                    } else if (line.startsWith("inc;")) {
                        rows.put(i, new Row(i, text, false));
                    } else {
                        i--;
                    }
                    i++;
                }
                updateTitle();
            }

            newEntry();

            pS.setTitle("Todo");
            pS.setScene(scene);
            pS.show();

            scene.getWindow().setOnCloseRequest(ev -> {
                if (!rows.isEmpty()) {
                    if (!data.exists()) {
                        try {
                            data.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(data)))) {
                        for (Row row : rows.values()) {
                            if (row.isChecked()) writer.print("com;");
                            else writer.print("inc;");
                            writer.println(row.getText());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (data.exists()) {
                    data.delete();
                }
            });
        } else {
            title = new Text("Please use Java 8 or higher!");
            title.setFont(Font.font("Lato, Tahoma", FontWeight.NORMAL, 18));
            title.setFill(Color.RED);
            grid.add(title, 0, 0, 4, 1);

            pS.setScene(scene);
            pS.show();
        }
    }

    void updateTitle() {
        title.setText(String.format("Todo List (%d/%d)", rows.values().stream().filter(Row::isChecked).count(), rows.size()));
    }

    TextField newEntry() {
        int rowIndex = 0;
        for (Node child : grid.getChildren()) {
            rowIndex = GridPane.getRowIndex(child);
        }
        final int rowIndexF = ++rowIndex;

        TextField entry = new TextField();
        entry.setPromptText("Enter todo item here.");

        entry.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER && !entry.getText().isEmpty()) {
                grid.getChildren().removeIf(child -> child instanceof TextField);

                rows.put(rowIndexF, new Row(rowIndexF, entry.getText(), false));

                updateTitle();
                newEntry();
            }
        });

        grid.add(entry, 0, rowIndex, 5, 1);
        entry.requestFocus();
        return entry;
    }

    GridPane getGrid() {
        return grid;
    }

    Map<Integer, Row> getRows() {
        return rows;
    }
}
