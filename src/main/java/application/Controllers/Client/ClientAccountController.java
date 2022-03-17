package application.Controllers.Client;

import application.Controllers.ButtonInsideTableColumn;
import application.Controllers.Controller;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import users.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;


public class ClientAccountController extends Controller {

    private final Lighting lighting = new Lighting();
    protected final Client currentUser = new Client(CURRENT_USER_LOGIN);
    private HashMap<String, String> orderStatusHashMap = new HashMap<>();
    @FXML
    private Pane ordersPane, favouritesPane, detailsPane, accountSettingsPane;
    @FXML
    public Label emptyTableViewLabel;

    @FXML
    private TableColumn<OrderTable, Integer> orderDetailsQuantityColumn;
    @FXML
    private TableColumn<OrderTable, String> orderDetailsProductColumn, orderDetailsTotalValueColumn, orderDetailsValueColumn;

    @FXML
    private TextField tfLogin, tfName, tfLastName;
    @FXML
    private TableView<OrderTable> orderDetailsTableView;
    @FXML
    private Button ordersButton, accountSettingsButton, favouritesButton, payOrderButton, changePaymentMethodButton, cancelOrderButton;
    @FXML
    private Label orderIdLabel, totalValueLabel, paymentMethodLabel, orderStatusLabel, informationLabel, nameLabel, loginLabel,
            lastNameLabel, noOrdersLabel, valueOfOrdersLabel, noCanceledOrdersLabel, noInProgressOrdersLabel, noUnpaidOrdersLabel, noFinishedOrdersLabel;
    //TODO database DIAGRAMS update needed !

    @FXML
    public void initialize() {
        prepareScene();
        createGoBackButton(event -> switchScene(event, clientScene));
        createLightingEffect();
        createInformationImageAndAttachItToLabel();
        orderStatusHashMap = createHashMapWithOrderStatuses();
        createEmptyTableViewLabel();
        ordersButton.fire();

    }

    @FXML
    void ordersButtonClicked() {
        makePaneVisible(ordersPane);
        setButtonLightingEffect(ordersButton);
    }

    @FXML
    void favouritesButtonClicked() {
        makePaneVisible(favouritesPane);
        setButtonLightingEffect(favouritesButton);
    }

    @FXML
    void allOrderedProductsButtonClicked() {
        Dialog<ButtonType> dialog = new Dialog<>();
        VBox content = new VBox();

        TableView<ProductTable> orderedProductTable = createAllOrderedProductTable();
        try {
            displayOrderedProducts(orderedProductTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        orderedProductTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        content.setSpacing(10);
        if (orderedProductTable.getItems().isEmpty()) {
            content.getChildren().add(new Label("There are no ordered products"));
        } else {
            content.getChildren().add(orderedProductTable);

        }
        dialog.getDialogPane().setContent(content);
        dialog.setHeaderText("All ordered products");
        setLogoAndCssToCustomDialog(dialog);
        dialog.getDialogPane().setMinWidth(650);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
        anchor.requestFocus();
    }

    private TableView<ProductTable> createAllOrderedProductTable() {
        TableView<ProductTable> orderedProductTable = new TableView<>();
        orderedProductTable.setPlaceholder(new Label("There are no ordered products "));
        orderedProductTable.setPrefWidth(500);
        TableColumn<ProductTable, String> productName = new TableColumn<>("name");
        productName.setMinWidth(200);
        TableColumn<ProductTable, String> productPrice = new TableColumn<>("price");
        productPrice.setPrefWidth(200);
        TableColumn<ProductTable, String> productSubcategory = new TableColumn<>("subcategory");
        productSubcategory.setPrefWidth(230);
        orderedProductTable.getColumns().addAll(productName, productPrice, productSubcategory);
        productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productPrice.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        productSubcategory.setCellValueFactory(new PropertyValueFactory<>("productSubcategory"));
        return orderedProductTable;
    }

    private void displayOrderedProducts(TableView<ProductTable> tableView) throws SQLException {
        ResultSet products = Product.getAllProductsOrderedByUser(getConnection(), CURRENT_USER_LOGIN);
        ObservableList<ProductTable> listOfProducts = ProductTable.getProductsBasicInfo(products);
        tableView.setItems(listOfProducts);
        showOnlyRowsWithData(tableView);
        tableView.setMaxHeight(250);
    }

    @FXML
    void accountSettingsButtonClicked() {
        makePaneVisible(accountSettingsPane);
        setButtonLightingEffect(accountSettingsButton);
        setDetailsOfOrdersLabels();
        try {
            ResultSet data = currentUser.getClientData(getConnection());
            currentUser.setClientData(data);
            data.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setAccountDetailsLabels();
    }




    private void setAccountDetailsLabels() {
        tfLogin.setText(currentUser.getLogin());
        tfLastName.setText(currentUser.getLastName());
        tfName.setText(currentUser.getName());
    }

    private void setDetailsOfOrdersLabels() {
        try {
            noOrdersLabel.setText(currentUser.getNumberOfOrders(getConnection()) + "");
            valueOfOrdersLabel.setText(currentUser.getTotalValueOfAllOrders(getConnection()) + CURRENCY);
            noCanceledOrdersLabel.setText(currentUser.getNumbersOfOrdersWithStatus("Canceled", getConnection()) + "");
            noInProgressOrdersLabel.setText(currentUser.getNumbersOfOrdersWithStatus("In progress", getConnection()) + "");
            noUnpaidOrdersLabel.setText(currentUser.getNumbersOfOrdersWithStatus("Waiting for payment", getConnection()) + "");
            noFinishedOrdersLabel.setText(currentUser.getNumbersOfOrdersWithStatus("Finished", getConnection()) + "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void confirmChangesButtonClicked() {
        if (isAnyDataChanged()) {
            if (!isAndFieldEmpty()) {
                try {
                    if (updateChangesInDatabase()) {
                        showNotification(createNotification(new Label("Data successfully changed")), 3000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            createAndShowAlert(Alert.AlertType.INFORMATION, "", "Account", "There are no changes to save !");
        }
        anchor.requestFocus();
    }

    private boolean isAndFieldEmpty() {
        return isTextFieldEmpty(tfLogin, loginLabel) || isTextFieldEmpty(tfLastName, lastNameLabel) || isTextFieldEmpty(tfName, nameLabel);
    }

    private boolean isTextFieldEmpty(TextField tf, Label label) {
        if (tf.getText().trim().isEmpty()) {
            colorField(tf, label, Color.RED);
            label.setVisible(true);
            return true;
        } else {
            basicTheme(tf, label);
            return false;
        }
    }

    private boolean updateChangesInDatabase() throws SQLException {

        String login = tfLogin.getText();
        String name = tfName.getText();
        String lastName = tfLastName.getText();
        if (!login.equals(currentUser.getLogin())) {
            if (isLoginUnique(login)) {
                currentUser.updateClientLogin(getConnection(), login);
                CURRENT_USER_LOGIN = login;
                return true;
            }

        }
        if (!name.equals(currentUser.getName())) {
            currentUser.updateClientName(getConnection(), name);
            return true;
        }
        if (!lastName.equals(currentUser.getLastName())) {
            currentUser.updateClientLastName(getConnection(), lastName);
            return true;
        }
        return false;

    }

    private boolean isAnyDataChanged() {
        return !(tfLogin.getText().equals(currentUser.getLogin()) && tfName.getText().equals(currentUser.getName()) && tfLastName.getText().equals(currentUser.getLastName()));
    }

    private boolean isLoginUnique(String login) throws SQLException {
        if (Client.isClientInDataBase(getConnection(), login)) {
            createAndShowAlert(Alert.AlertType.WARNING, "A user with this login already exists.", "Login", "Please choose a new login and try again.");
            return false;
        } else return true;

    }

    @FXML
    void changePasswordButtonClicked() {
        Optional<String> result = createAndShowConfirmPasswordAlert();
        result.ifPresent(s -> {
            if (result.get().equals(currentUser.getPassword())) {
                Optional<String> newAlert = enterNewPasswordAlert();
                newAlert.ifPresent(newPassword -> {
                    if (Pattern.matches(PASSWORDS_REGEX, newPassword)) {
                        try {
                            currentUser.updateClientPassword(getConnection(), newPassword);
                            showNotification(createNotification(new Label("Password changed")), 3000);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                ButtonType tryAgain = new ButtonType("Try again");
                ButtonType cancel = new ButtonType("Cancel");
                Optional<ButtonType> res = createAndShowAlert(tryAgain, cancel, "Incorrect password, please try again", "Wrong password");
                res.ifPresent(buttonType -> {
                    if (res.get() == tryAgain) changePasswordButtonClicked();
                });
            }
        });
        anchor.requestFocus();
    }

    private Optional<String> enterNewPasswordAlert() {
        PasswordField passwordField = new PasswordField();
        Dialog<String> dialog = createCustomEnterPasswordAlert(passwordField);
        dialog.setTitle("New password");
        dialog.setHeaderText("A password can only be saved if the following conditions are met :\n6-20 characters, one number, one uppercase letter, one lowercase letter, one special character ");
        ButtonType savePass = new ButtonType("Save new password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(savePass);
        Node savaPasswordButton = dialog.getDialogPane().lookupButton(savePass);
        dialog.getDialogPane().getButtonTypes().removeAll(ButtonType.OK);
        savaPasswordButton.setDisable(true);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == savePass) {
                return passwordField.getText();
            }
            return null;
        });
        passwordField.textProperty().addListener((observableValue, oldValue, newValue) -> savaPasswordButton.setDisable(!Pattern.matches(PASSWORDS_REGEX, newValue)));
        return dialog.showAndWait();
    }

    private Dialog<String> createCustomEnterPasswordAlert(PasswordField passwordField) {
        Button button = new Button();
        setPasswordVisibilityButton(button, passwordField);
        Dialog<String> dialog = new Dialog<>();
        passwordField.setPrefSize(600, 30);
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        content.getChildren().addAll(new Label("Enter password here :"), passwordField, button);
        dialog.getDialogPane().setContent(content);
        setLogoAndCssToCustomDialog(dialog);
        dialog.getDialogPane().setMinWidth(650);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        return dialog;
    }

    private Optional<String> createAndShowConfirmPasswordAlert() {
        PasswordField passwordField = new PasswordField();
        Dialog<String> dialog = createCustomEnterPasswordAlert(passwordField);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return passwordField.getText();
            }
            return null;
        });
        dialog.setTitle("Password");
        dialog.setHeaderText("Confirm that it is you ");
        return dialog.showAndWait();
    }

    private void deleteConfirmation(ActionEvent event) {
        Optional<ButtonType> buttonAlert = createAndShowAlert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your account?", "DELETE ACCOUNT"
                , "Account deletion is permanent and cannot be undone.\nAll your orders will be closed and deleted.");
        if (buttonAlert.isPresent() && buttonAlert.get() == ButtonType.OK) {
            try {
                checkConnectionWithDb();
                currentUser.deleteClient(getConnection());
                createAndShowAlert(Alert.AlertType.WARNING, "", "", "The account has been successfully deleted.");
                switchScene(event, loginScene);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void deleteAccountButtonClicked(ActionEvent event) {
        Optional<String> result = createAndShowConfirmPasswordAlert();
        result.ifPresent(s -> {
            if (result.get().equals(currentUser.getPassword())) {
                deleteConfirmation(event);
            } else {
                ButtonType tryAgain = new ButtonType("Try again");
                ButtonType cancel = new ButtonType("Cancel");
                Optional<ButtonType> res = createAndShowAlert(tryAgain, cancel, "Incorrect password, please try again", "Wrong password");
                res.ifPresent(buttonType -> {
                    if (res.get() == tryAgain) deleteAccountButtonClicked(event);
                });
            }
        });

    }

    private void createLightingEffect() {
        Light.Distant light = new Light.Distant();
        light.setColor(Color.LIGHTPINK);
        lighting.setLight(light);
    }

    private void createEmptyTableViewLabel() {
        emptyTableViewLabel.setId("titleLabel");
        emptyTableViewLabel.setVisible(false);
        emptyTableViewLabel.setAlignment(Pos.CENTER);
    }

    private void createInformationImageAndAttachItToLabel() {
        ImageView informationImage = setImageFromIconsFolder("information.png");
        informationImage.setLayoutX(725);
        informationImage.setLayoutY(91);
        orderStatusLabel.setGraphic(informationImage);
        orderStatusLabel.setContentDisplay(ContentDisplay.RIGHT);
        orderStatusLabel.setOnMouseEntered(mouseEvent -> informationLabel.setVisible(true));
        orderStatusLabel.setOnMouseExited(mouseEvent -> informationLabel.setVisible(false));
    }

    private HashMap<String, String> createHashMapWithOrderStatuses() {
        HashMap<String, String> orderStatus = new HashMap<>();
        orderStatus.put("Canceled", "Your order has been cancelled and your payment will be refunded");
        orderStatus.put("In progress", "Your order has been paid and is awaiting approval");
        orderStatus.put("Finished", "Order has been sent to the email assigned to your account");
        orderStatus.put("Waiting for payment", "The order has not been paid");
        return orderStatus;
    }

    private void setInformationAboutOrderStatus(HashMap<String, String> orderStatusHashMap) {
        informationLabel.setWrapText(true);
        informationLabel.setText(orderStatusHashMap.get(orderStatusLabel.getText()));
        informationLabel.setVisible(false);
    }

    private void setButtonLightingEffect(Button button) {
        ordersButton.setEffect(null);
        favouritesButton.setEffect(null);
        accountSettingsButton.setEffect(null);
        if (button == ordersButton) ordersButton.setEffect(lighting);
        else if (button == favouritesButton) favouritesButton.setEffect(lighting);
        else if (button == accountSettingsButton) accountSettingsButton.setEffect(lighting);
    }

    private void makePaneVisible(Pane pane) {
        ordersPane.setVisible(false);
        detailsPane.setVisible(false);
        favouritesPane.setVisible(false);
        accountSettingsPane.setVisible(false);
        emptyTableViewLabel.setVisible(false);
        if (pane == ordersPane) {
            ordersPane.setVisible(true);
        } else if (pane == detailsPane) {
            detailsPane.setVisible(true);
        } else if (pane == favouritesPane) {
            favouritesPane.setVisible(true);
        } else if (pane == accountSettingsPane) {
            accountSettingsPane.setVisible(true);
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


    private void fillOrderDetailColumnsWithData(ObservableList<OrderTable> list) {
        orderDetailsProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        orderDetailsQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("productQuantity"));
        orderDetailsTotalValueColumn.setCellValueFactory(new PropertyValueFactory<>("orderTotalValue"));
        orderDetailsValueColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        orderDetailsTableView.setItems(list);
        showOnlyRowsWithData(orderDetailsTableView);
        orderDetailsTableView.setMaxHeight(250);
    }

    private ButtonInsideTableColumn<OrderTable, String> createOrderIdButton() {
        ButtonInsideTableColumn<OrderTable, String> button = new ButtonInsideTableColumn<>("", "details");
        EventHandler<MouseEvent> buttonClicked = mouseEvent -> {
            //order id pane on
            makePaneVisible(detailsPane);
            displayOrderDetails(button.getRowId().getOrderNumber());
            fillOrderDetailLabels(button);
            makeProperButtonsVisible(orderStatusLabel.getText());
            setButtonLightingEffect(null);
            setInformationAboutOrderStatus(orderStatusHashMap);
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

    private void fillOrderDetailLabels(ButtonInsideTableColumn<OrderTable, String> button) {
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
    }






    private void changeOrderStatusAndDisplayProperButtons(String status) {
        try {
            Order order = new Order(Integer.parseInt(orderIdLabel.getText()));
            order.setOrderStatus(getConnection(), status);
            orderStatusLabel.setText(status);
            setInformationAboutOrderStatus(orderStatusHashMap);
            makeProperButtonsVisible(status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void changePaymentMethod(String paymentMethod) {
        try {
            Order order = new Order(Integer.parseInt(orderIdLabel.getText()));
            order.setPaymentMethod(getConnection(), paymentMethod);
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
        Optional<ButtonType> buttonClicked = createAndShowAlert(Alert.AlertType.CONFIRMATION, "", "Canceling", "Are you sure about canceling the order ?");
        if (buttonClicked.isPresent() && buttonClicked.get() == ButtonType.OK) {
            changeOrderStatusAndDisplayProperButtons("Canceled");
            setDisableToAllLabels(true);
        }
        anchor.requestFocus();
    }

    @FXML
    void changePaymentMethodButtonClicked() {
        try {
            Optional<String> buttonInsideAlertText = setAvailablePaymentMethodsToAlert(paymentMethodLabel.getText());
            if (buttonInsideAlertText.isPresent() && !buttonInsideAlertText.get().equals(ButtonType.CANCEL.getText())) {
                buttonInsideAlertText.ifPresent(this::changePaymentMethod);
                showNotification(createNotification(new Label("payment method changed")), 4000);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        anchor.requestFocus();

    }

    private Optional<String> setAvailablePaymentMethodsToAlert(String paymentMethod) throws SQLException {
        List<String> choices = new ArrayList<>();
        ResultSet paymentMethods = Product.getPaymentMethods(getConnection());

        while (Objects.requireNonNull(paymentMethods).next()) {
            if (!paymentMethods.getString(2).equals(paymentMethod)) {
                choices.add(paymentMethods.getString(2));
            }
        }
        choices.sort(null); // now on 0 position is the longest name, and it will nicely fit in window
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setHeaderText("Payment method change");
        dialog.setContentText("Available payment methods :  ");
        setLogoAndCssToCustomDialog(dialog);
        return dialog.showAndWait();
    }

    private void setLogoAndCssToCustomDialog(Dialog<?> dialog) {
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(iconsUrl + "transparentLogo.png"));
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(cssUrl).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("alert");
    }


}