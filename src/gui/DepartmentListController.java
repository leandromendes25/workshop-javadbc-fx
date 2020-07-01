package gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.util.Alerts;
import gui.util.DataChangeListener;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentListController implements Initializable, DataChangeListener {

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
	private TableColumn<Department, Department> tableColumnEDIT;

	@FXML
	private TableColumn<Department, Department> tableColumnRemove;

	@FXML
	private Button btNew;

	private ObservableList<Department> obsList;

	@FXML // referencia ao controle que recebeu o evento
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		// Quando for criar um departmento ele vai estar vazio então
		// e colocamos o obj ali para injetá-lo no formulario
		Department obj = new Department();
		createDialogForm(obj, "/gui/DepartmentForm.fxml", parentStage);
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
		initEditButtons();
		initRemoveButtons();
	}

	// ao criar uma janela de dialogo temos q dizer quem é o stage q vai cria o
	// dialogo
	private void createDialogForm(Department obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();
			Stage dialogStage = new Stage();
			DepartmentFormControl controller = loader.getController();
			controller.setDeparment(obj);
			controller.setDeparmentService(new DepartmentService());
			// me inscrevendo para receber o evento, e o onDataChange será executado
			controller.subscribeDataChangeListener(this);
			// para carregar o obj num formulario
			controller.updateFormData();
			// quando o dialogo tem q aparecer na frente, tem q instanciar um novo stage
			dialogStage.setTitle("Enter Department Data");
			// elemento raiz da cena, pane
			dialogStage.setScene(new Scene(pane));
			// redimensiona janela
			dialogStage.setResizable(false);
			// Quem é o stage pai da janela
			dialogStage.initOwner(parentStage);
			// enquanto não fechá-la não poderá acessar a janela anterior
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();
		} catch (IOException e) {
			Alerts.showAlert("IO Exception", "ERROR loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChange() {
		updateTableView();

	}

	// Acrescenta um novo botão edit para cada linha da tabela
	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Department obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
						event -> createDialogForm(obj, "/gui/DepartmentForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnRemove.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnRemove.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Department obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});

	}

	private void removeEntity(Department obj) {
		// colocamos get ali pq o optional carrega um outro objeto dentro dele
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete?");
		if (result.get() == ButtonType.OK) {
			if (service == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				service.remove(obj);
				updateTableView();
			} catch (DbIntegrityException e) {
				Alerts.showAlert("Error removing object", "null", e.getMessage(), AlertType.ERROR);
			}

		}
	}

}
