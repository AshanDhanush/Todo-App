package controller;

import dbconnection.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CompleteTask;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Donetaskview implements Initializable {
    public TableColumn colID;
    public TableColumn ColTask;
    public TableColumn colStatus;
    public TableColumn colStartDate;
    public TableColumn colDeadline;
    public TableColumn colEndDate;
    public TableView tableCompletedTask;

    public void loadTable(){
        try {
            ResultSet rs = DBConnection.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM COMPLETETASK");
            ArrayList<CompleteTask> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(new CompleteTask(rs.getString("id"),rs.getString("name"),rs.getString("status"),rs.getString("startDate"),rs.getString("deadline"),rs.getString("completeDate")));
            }

            colID.setCellValueFactory(new PropertyValueFactory("id"));
            ColTask.setCellValueFactory(new PropertyValueFactory("name"));
            colStatus.setCellValueFactory(new PropertyValueFactory("status"));
            colStartDate.setCellValueFactory(new PropertyValueFactory("startDate"));
            colDeadline.setCellValueFactory(new PropertyValueFactory("deadline"));
            colEndDate.setCellValueFactory(new PropertyValueFactory("completedDate"));

            ObservableList<CompleteTask> completeTaskObservableList = FXCollections.observableArrayList();
            tasks.forEach((customer)->completeTaskObservableList.add(customer));
            this.tableCompletedTask.setItems(completeTaskObservableList);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTable();
    }
}
