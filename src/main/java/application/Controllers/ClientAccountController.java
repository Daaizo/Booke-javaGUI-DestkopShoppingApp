package application.Controllers;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import users.Order;
import users.OrderTable;
import users.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class ClientAccountController extends Controller {

    private final Lighting lighting = new Lighting();
    @FXML
    private Pane ordersPane, favouritesPane, detailsPane;
    @FXML
    private TableColumn<OrderTable, String> ordersDateColumn, ordersDeliveryDateColumn, ordersPaymentColumn, ordersStatusColumn, ordersIdColumn, ordersButtonColumn, orderDetailsProductColumn, orderDetailsTotalValueColumn, orderDetailsValueColumn;
    @FXML
    private TableColumn<OrderTable, Double> ordersTotalValueColumn;
    @FXML
    private TableColumn<OrderTable, Integer> orderDetailsQuantityColumn;
    @FXML
    private TableView<OrderTable> ordersTableView, orderDetailsTableView;
    @FXML
    private Label orderIdLabel, totalValueLabel, paymentMethodLabel, orderStatusLabel;
    @FXML
    private Button ordersButton, accountButton, favouritesButton, settingsButton, payOrderButton, changePaymentMethodButton, cancelOrderButton;

    @FXML
    public void initialize() {
        prepareScene();
        displayOrders();
        createGoBackButton(event -> switchScene(event, clientScene));
        ordersButton.fire();
    }

    @FXML
    void ordersButtonClicked() {
        makePaneVisible(ordersPane);
        buttonLightingEffect(ordersButton);
    }

    @FXML
    void favouritesButtonClicked() {
        makePaneVisible(favouritesPane);
        buttonLightingEffect(favouritesButton);
    }

    private void buttonLightingEffect(Button button) {
        ordersButton.setEffect(null);
        favouritesButton.setEffect(null);
        settingsButton.setEffect(null);
        accountButton.setEffect(null);
        if (button == ordersButton) ordersButton.setEffect(lighting);
        else if (button == favouritesButton) favouritesButton.setEffect(lighting);
        else if (button == settingsButton) settingsButton.setEffect(lighting);
        else if (button == accountButton) accountButton.setEffect(lighting);
    }

    private void makePaneVisible(Pane pane) {
        ordersPane.setVisible(false);
        detailsPane.setVisible(false);
        favouritesPane.setVisible(false);
        if (pane == ordersPane) {
            ordersPane.setVisible(true);
        } else if (pane == detailsPane) {
            detailsPane.setVisible(true);
        } else if (pane == favouritesPane) {
            favouritesPane.setVisible(true);
        }
    }

    private void makeProperButtonsVisible(String orderStatus) {
        payOrderButton.setDisable(true);
        cancelOrderButton.setDisable(true);
        changePaymentMethodButton.setDisable(true);

        switch (orderStatus) {
            case "Waiting for payment" -> {
                payOrderButton.setDisable(false);
                changePaymentMethodButton.setDisable(false);
                cancelOrderButton.setDisable(false);
            }
            case "In progress" -> cancelOrderButton.setDisable(false);
        }
    }
    private void fillOrdersColumnsWithData(ObservableList<OrderTable> list) {

        ordersIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        ordersButtonColumn.setCellFactory(orderTableStringTableColumn -> orderIdButton());
        ordersDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        ordersDeliveryDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDeliveryDate"));
        ordersTotalValueColumn.setCellValueFactory(new PropertyValueFactory<>("orderTotalValue"));
        ordersStatusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatusName"));
        ordersPaymentColumn.setCellValueFactory(new PropertyValueFactory<>("orderPaymentMethodName"));
        ordersTableView.setItems(list);
        showOnlyRowsWithData(ordersTableView);
    }

    private void fillOrderDetailColumnsWithData(ObservableList<OrderTable> list) {
        orderDetailsProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        orderDetailsQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("productQuantity"));
        orderDetailsTotalValueColumn.setCellValueFactory(new PropertyValueFactory<>("orderTotalValue"));
        orderDetailsValueColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        orderDetailsTableView.setItems(list);
        showOnlyRowsWithData(orderDetailsTableView);
        orderDetailsTableView.setMaxHeight(250);
    }


    private ClientController.ButtonInsideTableColumn<OrderTable, String> orderIdButton() {
        ClientController.ButtonInsideTableColumn<OrderTable, String> button = new ClientController().new ButtonInsideTableColumn<>("", "details");
        EventHandler<MouseEvent> buttonClicked = mouseEvent -> {
            makePaneVisible(detailsPane);
            displayOrderDetails(button.getRowId().getOrderNumber());
            fillOrderDetailLabels(button);
            makeProperButtonsVisible(orderStatusLabel.getText());
        };
        button.setEventHandler(buttonClicked);
        button.setCssId("orderDetailsButton");
        return button;
    }

    private void displayOrderDetails(int orderNumber) {
        checkConnectionWithDb();
        Order order = new Order(CURRENT_USER_LOGIN);
        try {
            ResultSet orders = order.getOrderDetailedInformation(getConnection(), orderNumber);
            ObservableList<OrderTable> listOfOrders = OrderTable.getProductsFromOrder(orders);
            fillOrderDetailColumnsWithData(listOfOrders);
            orders.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillOrderDetailLabels(ClientController.ButtonInsideTableColumn<OrderTable, String> button) {
        orderIdLabel.setText(button.getRowId().getOrderNumber() + "");
        totalValueLabel.setText(button.getRowId().getOrderTotalValue() + "");
        paymentMethodLabel.setText(button.getRowId().getOrderPaymentMethodName());
        orderStatusLabel.setText(button.getRowId().getOrderStatusName());
        setDisableToAllLabels(orderStatusLabel.getText().equals("Canceled"));
    }

    private void setDisableToAllLabels(boolean disable) {
        orderIdLabel.setDisable(disable);
        totalValueLabel.setDisable(disable);
        paymentMethodLabel.setDisable(disable);
        orderStatusLabel.setDisable(disable);
    }

    private void displayOrders() {
        checkConnectionWithDb();
        Order order = new Order(CURRENT_USER_LOGIN);
        try {
            ResultSet orders = order.getOrdersFromCustomer(getConnection());
            ObservableList<OrderTable> listOfOrders = OrderTable.getOrders(orders);
            fillOrdersColumnsWithData(listOfOrders);
            ordersTableView.setMaxHeight(530);
            orders.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reloadTableView(TableView<?> tableView) {
        tableView.getItems().clear();
        displayOrders();
    }

    private void changeOrderStatusAndDisplayProperButtons(String status) {
        try {
            Order order = new Order(Integer.parseInt(orderIdLabel.getText()));
            order.setOrderStatus(getConnection(), status);
            reloadTableView(ordersTableView);
            orderStatusLabel.setText(status);
            makeProperButtonsVisible(status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void changePaymentMethod(String paymentMethod) {
        try {
            Order order = new Order(Integer.parseInt(orderIdLabel.getText()));
            order.setPaymentMethod(getConnection(), paymentMethod);
            reloadTableView(ordersTableView);
            paymentMethodLabel.setText(paymentMethod);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void payOrderButtonClicked() {
        changeOrderStatusAndDisplayProperButtons("In progress");
        showNotification(createNotification(new Label("order successfully paid")), 4000);
    }

    @FXML
    void cancelOrderButtonClicked() {
        Optional<ButtonType> buttonClicked = createAndShowAlert(Alert.AlertType.WARNING, "", "Canceling", "Are you sure about canceling the order ?");
        if (buttonClicked.isPresent() && buttonClicked.get() == ButtonType.OK) {
            changeOrderStatusAndDisplayProperButtons("Canceled");
            setDisableToAllLabels(true);
        }
    }

    @FXML
    void changePaymentMethodButtonClicked() {
        try {
            Optional<String> buttonInsideAlertText = setAvailablePaymentMethodsToAlert(paymentMethodLabel.getText());
            buttonInsideAlertText.ifPresent(this::changePaymentMethod);
            showNotification(createNotification(new Label("payment method changed")), 4000);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Optional<String> setAvailablePaymentMethodsToAlert(String paymentMethod) throws SQLException {
        List<String> choices = new ArrayList<>();
        ResultSet paymentMethods = Product.getPaymentMethods(getConnection());

        while (Objects.requireNonNull(paymentMethods).next()) {
            if (!paymentMethods.getString(2).equals(paymentMethod)) {
                choices.add(paymentMethods.getString(2));
            }
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setHeaderText("Payment method change");
        dialog.setContentText("Available payment methods : ");
        return dialog.showAndWait();
    }


}