package goryachev.apps;

import goryachev.util.FxDebug;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.ResizeFeatures;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ATableViewTester extends Application {
    
    enum Demo {
        ALL("all set: min, pref, max"),
        CONSTRAINED("constrained with pref width"),
        EMPTY("empty"),
        MIN_WIDTH("constrained with min width"),
        ;

        private final String text;
        Demo(String text) { this.text = text; }
        public String toString() { return text; }
    }
    
    enum Cmd {
        CONSTRAINED,
        ROWS,
        COL,
        MIN,
        PREF,
        MAX
    }

    protected BorderPane contentPane;
    protected TableViewSelectionModel<String> oldTableSelectionModel;

    public static void main(String[] args) {
        Application.launch(ATableViewTester.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        contentPane = new BorderPane();
        
        // selector
        ComboBox<Demo> cb = new ComboBox<>();
        cb.getItems().addAll(Demo.values());
        cb.setEditable(false);
        cb.getSelectionModel().selectedItemProperty().addListener((s,p,c) -> {
            Pane n = createPane(c);
            contentPane.setCenter(n);
        });
        
        // https://bugs.openjdk.org/browse/JDK-8087673
//        {
//            table.setTableMenuButtonVisible(true);
//            table.getColumns().get(2).setGraphic(new Slider());
//        }
        

        // layout

        SplitPane split = new SplitPane(contentPane, new BorderPane());

        BorderPane bp = new BorderPane();
        bp.setTop(cb);
        bp.setCenter(split);
        
        Scene sc = new Scene(bp);

        FxDebug.attachNodeDumper(stage);
        stage.setScene(sc);
        stage.setWidth(700);
        stage.setHeight(300);
        stage.setTitle("TableView Tester " + System.getProperty("java.version"));
        stage.show();
        
        cb.getSelectionModel().selectFirst();
    }

    protected Callback<ResizeFeatures,Boolean> wrap(Callback<ResizeFeatures,Boolean> policy) {
        return new Callback<ResizeFeatures,Boolean>() {
            @Override
            public Boolean call(ResizeFeatures f) {
                Boolean rv = policy.call(f);
                int ix = f.getTable().getColumns().indexOf(f.getColumn());
                System.out.println(
                    "col=" + (ix < 0 ? f.getColumn() : ix) + 
                    " delta=" + f.getDelta() + 
                    " w=" + f.getTable().getWidth() + 
                    " rv=" + rv
                    );
                return rv;
            }
        };
    }
    
    protected String describe(TableColumn c) {
        StringBuilder sb = new StringBuilder();
        if(c.getMinWidth() != 10.0) {
            sb.append("min=");
            sb.append((int)c.getMinWidth());
        }
        if(c.getPrefWidth() != 80.0) {
            sb.append(" pref=");
            sb.append((int)c.getPrefWidth());
        }
        if(c.getMaxWidth() != 5000.0) {
            sb.append(" max=");
            sb.append((int)c.getMaxWidth());
        }
        return sb.toString();
    }
    
    // min, pref, max, rows
    protected Pane createTable(Object ... spec) {
        TableView<String> table = new TableView();
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        TableColumn<String,String> lastColumn = null;
        
        for(int i=0; i<spec.length; ) {
            Object x = spec[i++];
            if(x instanceof Cmd cmd) {
                switch(cmd) {
                case CONSTRAINED:
                    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                    break;
                case COL:
                    TableColumn<String,String> c = new TableColumn<>();
                    table.getColumns().add(c);
                    c.setText("C" + table.getColumns().size());
                    c.setCellValueFactory((f) -> new SimpleStringProperty(describe(c)));
                    lastColumn = c;
                    break;
                case MAX:
                    {
                        int w = (int)(spec[i++]);
                        lastColumn.setMaxWidth(w);
                    }
                    break;
                case MIN:
                    {
                        int w = (int)(spec[i++]);
                        lastColumn.setMinWidth(w);
                    }
                    break;
                case PREF:
                    {
                        int w = (int)(spec[i++]);
                        lastColumn.setPrefWidth(w);
                    }
                    break;
                case ROWS:
                    int n = (int)(spec[i++]);
                    for(int j=0; j<n; j++) {
                        table.getItems().add("");
                    }
                    break;
                default:
                    throw new Error("?" + cmd);
                }
            }
        }
        
        BorderPane bp = new BorderPane();
        bp.setCenter(table);
        return bp;
    }

    protected Pane createPane(Demo d) {
        switch(d) {
        case ALL:
            return createTable(
                Cmd.CONSTRAINED,
                Cmd.ROWS, 3,
                Cmd.COL, Cmd.MIN, 20, Cmd.PREF, 20, Cmd.MAX, 20,
                Cmd.COL, Cmd.PREF, 200,
                Cmd.COL, Cmd.PREF, 300, Cmd.MAX, 400
                );           
        case CONSTRAINED:
            return createTable(
                Cmd.CONSTRAINED,
                Cmd.ROWS, 3,
                Cmd.COL, Cmd.PREF, 100,
                Cmd.COL, Cmd.PREF, 200,
                Cmd.COL, Cmd.PREF, 300
                );
        case EMPTY:
            return createTable(
                Cmd.CONSTRAINED,
                Cmd.COL,
                Cmd.COL,
                Cmd.COL
                );
        case MIN_WIDTH:
            return createTable(
                Cmd.CONSTRAINED,
                Cmd.ROWS, 3,
                Cmd.COL,
                Cmd.COL,
                Cmd.COL, Cmd.MIN, 300
                );
        default:
            return new BorderPane();
        }
    }
}
