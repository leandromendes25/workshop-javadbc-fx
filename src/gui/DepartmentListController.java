package gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentListController implements Initializable {

	private DepartmentService service;
	// Instanciando a tableView
	@FXML
	private TableView<Department> tableViewDepartment;
	@FXML
	// O tipo da entidade e depois o tipo da coluna
	private TableColumn<Department, Integer> tableColumnId;
	@FXML
	private TableColumn<Department, String> tableColumnName;
	@FXML
	private Button btNew;
	private ObservableList<Department> obsList;

	@FXML
	public void onBtNewAction() {
		System.out.println("butom");
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}

	public void initializeNodes() {
		// para iniciar os comportamento das colunas
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		// window pega referencia do stage, é uma super classe
		Stage stage = (Stage) Main.getMainScene().getWindow();
		// tableView Acompanhar o tamanho da janela
		tableViewDepartment.prefHeightProperty().bind(stage.heightProperty());
	}

	public void updateTableView() {
		if (service == null) {
			throw new IllegalStateException("service is null");
		}
		List<Department> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		// setItems para carregar na view
		tableViewDepartment.setItems(obsList);

	}
}
