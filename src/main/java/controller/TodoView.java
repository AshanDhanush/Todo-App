package controller;

import dbconnection.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.AddTodo;
import model.CompleteTask;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TodoView implements Initializable {
    public ListView viewList;
    public TextField txtAddTask;
    public DatePicker dateEnd;
    public TextField txtSearch;

    private String[] idArr = new String[0];
    private String newID;

    public void addBtnOnAction(ActionEvent actionEvent) {
        try {
            String startDate = LocalDate.now().toString();
            createID();
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("insert into addtodolist values(?,?,?,?)");
            ps.setString(1, newID);
            ps.setString(2, txtAddTask.getText());
            ps.setString(3, startDate);
            ps.setString(4, dateEnd.getValue().toString());
            boolean rs = ps.executeUpdate()>0;
            if(rs) {
                loadListView();
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Added Successfully");
                alert.showAndWait();
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR,"Added Failed");
                alert.showAndWait();
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void viewDoneTaskBtnOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/donetaskview.fxml"))));
        stage.show();
    }

    public void deleteBtnOnaction(ActionEvent actionEvent) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("delete from addtodolist where name = ?");
            ps.setString(1, txtSearch.getText() );
            boolean rs = ps.executeUpdate()>0;
            if(rs) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Deleted Successfully");
                alert.showAndWait();
                loadListView();
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR,"Deleted Failed");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void createID(){
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery("select id from addtodolist");
            while (rs.next()) {
                extendArray();
                idArr[idArr.length-1] = rs.getString("id");
            }
            if (idArr.length==0){
                this.newID ="T001";
            }else{
                String lastId = idArr[idArr.length-1];
                int lastno = Integer.parseInt(lastId.substring(1));
                lastno++;
                this.newID = String.format("T%03d", lastno);
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public void extendArray(){
        String[] tempArr = new String[idArr.length+1];
        for (int i = 0; i < idArr.length; i++) {
            tempArr[i] = idArr[i];
        }
        idArr = tempArr;
    }
    public void loadListView() {
        ObservableList<AddTodo> todolist = FXCollections.observableArrayList();
        HashMap<String, String> completedMap = new HashMap<>();

        try {

            ResultSet rsCompleted = DBConnection.getInstance().getConnection()
                    .createStatement().executeQuery("SELECT id, status FROM completetask");
            while (rsCompleted.next()) {
                completedMap.put(rsCompleted.getString("id"), rsCompleted.getString("status"));
            }


            ResultSet rs = DBConnection.getInstance().getConnection()
                    .createStatement().executeQuery("SELECT * FROM addtodolist");
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String dateStart = rs.getString("dateStart");
                String dateEnd = rs.getString("dateEnd");
                todolist.add(new AddTodo(id, name, dateStart, dateEnd));
            }

            this.viewList.setItems(todolist);

            viewList.setCellFactory(lv -> new ListCell<AddTodo>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(event -> {
                        AddTodo todo = getItem();
                        if (todo != null && checkBox.isSelected()) {
                            String status;
                            String today = LocalDate.now().toString();
                            if (todo.getDateEnd().compareToIgnoreCase(today) > 0) {
                                status = "Done ! early submit";
                            } else if (todo.getDateEnd().compareToIgnoreCase(today) < 0) {
                                status = "Done ! late";
                            } else {
                                status = "Done ! On the date";
                            }

                            try {
                                CompleteTask ct = new CompleteTask(
                                        todo.getId(),
                                        todo.getName(),
                                        status,
                                        todo.getDateStart(),
                                        todo.getDateEnd(),
                                        today
                                );

                                PreparedStatement ps = DBConnection.getInstance().getConnection()
                                        .prepareStatement("INSERT INTO completetask VALUES(?,?,?,?,?,?)");
                                ps.setString(1, ct.getId());
                                ps.setString(2, ct.getName());
                                ps.setString(3, ct.getStatus());
                                ps.setString(4, ct.getStartDate());
                                ps.setString(5, ct.getDeadline());
                                ps.setString(6, ct.getCompletedDate());
                                ps.executeUpdate();

                                completedMap.put(ct.getId(), ct.getStatus());

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(AddTodo todo, boolean empty) {
                    super.updateItem(todo, empty);
                    if (empty || todo == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        checkBox.setText(todo.getName());


                        String status = completedMap.get(todo.getId());
                        boolean done = false;
                        if (status != null) {
                            done = status.trim().toLowerCase().startsWith("done");
                        }
                        checkBox.setSelected(done);

                        setGraphic(checkBox);
                    }
                }
            });

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadListView();
    }
}
