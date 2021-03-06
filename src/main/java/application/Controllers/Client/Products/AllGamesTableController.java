package application.Controllers.Client.Products;

import application.Controllers.Client.ClientStartSceneController;
import application.Controllers.Controller;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import users.Product;
import users.ProductTable;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AllGamesTableController extends ClientStartSceneController {
    @FXML
    private TableColumn<ProductTable, String> gamesNameColumn, gamesSubcategoryColumn, gamesPriceColumn, gamesNumberOfOrdersColumn, gamesCartButtonColumn, gamesStarButtonColumn;

    @FXML
    private TableView<ProductTable> gamesTableView;

    @FXML
    public void initialize() throws IOException {
        try {
            displayGames();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createSortingButtons();
        prepareSortingButtons(gamesTableView, gamesNumberOfOrdersColumn, gamesStarButtonColumn);
    }

    private void fillGamesColumnsWithData(ObservableList<ProductTable> list) {
        gamesNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        gamesPriceColumn.setCellValueFactory(new PropertyValueFactory<>("productPrice"));
        gamesSubcategoryColumn.setCellValueFactory(new PropertyValueFactory<>("productSubcategory"));
        gamesStarButtonColumn.setCellValueFactory(buttonInsideCell -> buttonInsideCell.getValue().isProductFavouriteProperty());
        gamesStarButtonColumn.setCellFactory(productTableStringTableColumn -> createStarButtonInsideTableView());
        gamesCartButtonColumn.setCellFactory(buttonInsideCell -> createCartButtonInsideTableView());
        gamesNumberOfOrdersColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfOrdersPerProduct"));
        gamesTableView.setItems(list);
        prepareTableView(gamesTableView, gamesPriceColumn);
    }

    private void displayGames() throws SQLException {

        checkConnectionWithDb();
        ResultSet products = Product.getProductsFromCategoryAndInformationIfProductIsInUsersFavouriteFromDatabase(getConnection(), Controller.CURRENT_USER_LOGIN, "games");
        assert products != null;
        ObservableList<ProductTable> listOfGames = ProductTable.getProductsFromCategoryWithInformationIfTheyAreInUsersFavourite(products);
        fillGamesColumnsWithData(listOfGames);
    }

}
