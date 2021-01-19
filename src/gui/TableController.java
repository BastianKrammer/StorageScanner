package gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import scanner.DirectoryScanner;
import scanner.TableContent;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableController {

    // fxml elements
    @FXML
    private TableView table;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn sizeColumn;
    @FXML
    private TableColumn filesColumn;
    @FXML
    private TableColumn dirsColumn;


    // other gui classes
    private PathController pathController;
    private Settings settings;

    // private classes
    private TableUpdate tableUpdateThread;
    private List<TableContent> currentTable;
    private Thread searchThread;


    public TableController(GUIController guiController, TableView table, TableColumn nameColumn, TableColumn sizeColumn, TableColumn filesColumn, TableColumn dirsColumn) {
        this.table = table;
        this.nameColumn = nameColumn;
        this.sizeColumn = sizeColumn;
        this.filesColumn = filesColumn;
        this.dirsColumn = dirsColumn;

        this.pathController = guiController.getPathController();
        this.settings = guiController.getSettings();


        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        filesColumn.setCellValueFactory(new PropertyValueFactory<>("files"));
        dirsColumn.setCellValueFactory(new PropertyValueFactory<>("dirs"));

        // set event handler if a double click on a row happens
        table.setRowFactory( tv -> {
            TableRow<TableContent> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    pathController.enterDirectory(row.getItem().getName());
                }
            });
            return row ;
        });
    }



    // public functions
    public void clearTable() {
        updateTable(new ArrayList<>());
    }

    public void setPath(String path) {
        getNewTable(path);
    }

    public void reloadTable() {
        getNewTable(pathController.getPath());
    }

    public void sortTable() {
        // TODO try table.sort();
        Collections.sort(currentTable);
        updateTable(currentTable);
    }




    // private functions
    private void getNewTable(String path) {
        if(searchThread != null && searchThread.isAlive()) {
            try {
                searchThread.interrupt();
                searchThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // get settings
        DirectoryScanner ds = new DirectoryScanner(path, settings.getIgnoreHiddenElements());

        searchThread = new Thread(ds);
        searchThread.start();
    }

    private void updateTable(List<TableContent> tc) {
        currentTable = new ArrayList<>(tc);

        if(tableUpdateThread != null) {
            tableUpdateThread.interrupt();
        }

        tableUpdateThread = new TableUpdate();
        Platform.runLater(tableUpdateThread);
    }







    // private classes
    class TableUpdate implements Runnable {
        private boolean interrupt;
        @Override
        public void run() {
            interrupt = false;
            List<TableContent> tmpList = new ArrayList<>(currentTable);
            table.getItems().clear();
            for (TableContent content: tmpList) {
                if(interrupt) {
                    break;
                }
                if(content == null) {
                    break;
                }else{
                    table.getItems().add(content);
                }
            }
            //table.scrollTo(actualTable.size()-1);
        }

        public void interrupt() {
            interrupt = true;
        }
    }
}