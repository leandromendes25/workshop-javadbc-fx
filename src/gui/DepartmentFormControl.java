package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.DataChangeListener;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.exceptions.ValidationException;
import model.services.DepartmentService;

public class DepartmentFormControl implements Initializable {

	private Department entity;

	private DepartmentService service;
	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private Label labelErrorName;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	public void setDeparment(Department entity) {
		this.entity = entity;
	}

	public void setDeparmentService(DepartmentService service) {
		this.service = service;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);

	}

	@FXML
	private void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity is null");
		}
		if (service == null) {
			throw new IllegalStateException("Service is null");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			// Fecha a janela
			Utils.currentStage(event).close();

		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error saving obj", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() {
		// para cada dataChangelister pertence a lista listeners
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChange();
		}

	}

	private Department getFormData() {
		Department obj = new Department();
		ValidationException exception = new ValidationException("Validation Exception");

		obj.setId(Utils.tryParseToInt(txtId.getText()));
		// trim elimina qualquer espaço que esteja no inicio ou no final
		// equals("") significa vazio
		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			// o exception está sendo instanciado logo acima
			exception.addError("name", "Field can't be empty");
		}
		obj.setName(txtName.getText());
		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		return obj;
	}

	@FXML
	private void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		// Para aceitar somente do tipo Inteiro
		Constraints.setTextFieldInteger(txtId);
		// o campo e o tamanho máximo
		Constraints.setTextFieldMaxLength(txtName, 30);

	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
	}

	// set é conjunto
	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		if (fields.contains("name")) {
			// pegando nome referente ao campo name e setando lá no label
			labelErrorName.setText(errors.get("name"));
		}
	}
}
