package application.Controllers.Client.Products;

import application.Controllers.Client.ClientStartSceneController;
import application.Controllers.Controller;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import users.Product;
import users.ProductTable;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class AllEbooksTableController extends ClientStartSceneController {
    public Pane ebooksPane;
    @FXML
    private TableColumn<ProductTable, String> ebooksNameColumn, ebooksSubcategoryColumn, ebooksPriceColumn,
            ebooksStarButtonColumn, ebooksCartButtonColumn, ebooksNumberOfOrdersColumn;
    @FXML
    private TableView<ProductTable> ebooksTableView;

    @FXML
    public void initialize() {
        try {
            ebooksTableView.getItems().clear();
            displayEbooks();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createSortingButtons();
        prepareSortingButtons(ebooksTableView, ebooksNumberOfOrdersColumn, ebooksStarButtonColumn);
    }


    private void fillEbooksColumnsWithData(ObservableList<ProductTable> list) {
        ebooksNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        ebooksPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        ebooksSubcategoryColumn.setCellValueFactory(new PropertyValueFactory<>("productSubcategory"));
        ebooksStarButtonColumn.setCellValueFactory(buttonInsideCell -> buttonInsideCell.getValue().isProductFavouriteProperty());
        ebooksStarButtonColumn.setCellFactory(buttonInsideCell -> createStarButtonInsideTableView());
        ebooksCartButtonColumn.setCellFactory(buttonInsideCell -> createCartButtonInsideTableView());
        ebooksNumberOfOrdersColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfOrdersPerProduct"));
        ebooksTableView.setItems(list);
        prepareTableView(ebooksTableView, ebooksPriceColumn);
    }


    private void displayEbooks() throws SQLException {
        checkConnectionWithDb();
        ResultSet products = Product.getProductsFromCategoryAndInformationIfProductIsInUsersFavouriteFromDatabase(getConnection(), Controller.CURRENT_USER_LOGIN, "ebooks");
        assert products != null;
        ObservableList<ProductTable> listOfEbooks = ProductTable.getProductsFromCategoryWithInformationIfTheyAreInUsersFavourite(products);
        fillEbooksColumnsWithData(listOfEbooks);
    }
}
