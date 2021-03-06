package users;

import application.Controllers.Controller;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductTable {
    private final SimpleIntegerProperty productId;
    private final SimpleIntegerProperty numberOfOrdersPerProduct;

    private final SimpleStringProperty productName;
    private final SimpleStringProperty productCategory;
    private final SimpleStringProperty productPrice;
    private final SimpleStringProperty productSubcategory;
    private final SimpleIntegerProperty productQuantity;
    private final SimpleStringProperty productTotalValue;
    private final SimpleStringProperty isProductFavourite;

    public ProductTable(Product product) {
        this.productId = new SimpleIntegerProperty(product.getProductId());
        this.productName = new SimpleStringProperty(product.getProductName());
        this.productPrice = new SimpleStringProperty(product.getProductPrice());
        this.productCategory = new SimpleStringProperty(product.getProductCategory());
        this.productSubcategory = new SimpleStringProperty(product.getProductSubcategory());
        this.productQuantity = new SimpleIntegerProperty(product.getProductInCartQuantity());
        this.productTotalValue = new SimpleStringProperty(product.getProductTotalValue());
        this.isProductFavourite = new SimpleStringProperty(product.isFavourite());
        this.numberOfOrdersPerProduct = new SimpleIntegerProperty(product.getProductNumberOfOrders());
    }


    public static ObservableList<ProductTable> getProducts(ResultSet products) {
        ObservableList<ProductTable> listOfProducts = FXCollections.observableArrayList();
        try {
            while (products.next()) {
                int id = products.getInt(1);
                String name = products.getString(2);
                String price = products.getString(3) + Controller.CURRENCY;
                String subcategory = products.getString(4);
                String category = products.getString(5);
                listOfProducts.add(new ProductTable(new Product(id, name, price, subcategory, category)));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        return listOfProducts;
    }

    public static ObservableList<ProductTable> getProductsFromCategoryWithInformationIfTheyAreInUsersFavourite(ResultSet products) {
        ObservableList<ProductTable> listOfProducts = FXCollections.observableArrayList();
        try {
            while (products.next()) {
                String name = products.getString(2);
                String price = products.getString(3) + Controller.CURRENCY;
                String subcategory = products.getString(4);
                int numberOfProducts = products.getInt(5);
                String isFavourite = products.getString(6);
                listOfProducts.add(new ProductTable(new Product(name, price, subcategory, isFavourite, numberOfProducts)));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        return listOfProducts;
    }

    public static ObservableList<ProductTable> getProductsFromSubcategoryWithInformationIfTheyAreInUsersFavourite(ResultSet products) {
        ObservableList<ProductTable> listOfProducts = FXCollections.observableArrayList();
        try {
            while (products.next()) {
                String name = products.getString(2);
                String price = products.getString(3) + Controller.CURRENCY;
                int numberOfProducts = products.getInt(4);
                String isFavourite = products.getString(5);

                listOfProducts.add(new ProductTable(new Product(name, price, isFavourite, numberOfProducts)));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        return listOfProducts;
    }

    public static ObservableList<ProductTable> getAllProductsWithInformationIfTheyAreInUsersFavourite(ResultSet products) {
        ObservableList<ProductTable> listOfProducts = FXCollections.observableArrayList();
        try {
            while (products.next()) {
                String name = products.getString(2);
                String price = products.getString(3) + Controller.CURRENCY;
                String subcategory = products.getString(4);
                String category = products.getString(5);
                int numberOfProducts = products.getInt(6);
                String isFavourite = products.getString(7);
                listOfProducts.add(new ProductTable(new Product(name, price, subcategory, category, isFavourite, numberOfProducts)));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        return listOfProducts;
    }

    public static ObservableList<ProductTable> getProductsBasicInfo(ResultSet products) {
        ObservableList<ProductTable> listOfProducts = FXCollections.observableArrayList();
        try {
            while (products.next()) {
                String name = products.getString(1);
                String price = products.getString(2) + Controller.CURRENCY;
                String subcategory = products.getString(3);
                listOfProducts.add(new ProductTable(new Product(name, price, subcategory)));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        return listOfProducts;
    }

    public static ObservableList<ProductTable> getProductsFromShoppingCart(ResultSet products) {
        ObservableList<ProductTable> listOfProducts = FXCollections.observableArrayList();
        try {
            while (products.next()) {
                String name = products.getString(1);
                String price = products.getString(2) + Controller.CURRENCY;
                int quantity = products.getInt(3);
                String totalValue = products.getString(4) + Controller.CURRENCY;
                listOfProducts.add(new ProductTable(new Product(name, price, quantity, totalValue)));
            }
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        return listOfProducts;
    }

    public int getNumberOfOrdersPerProduct() {
        return numberOfOrdersPerProduct.get();
    }

    public void setNumberOfOrdersPerProduct(int numberOfOrdersPerProduct) {
        this.numberOfOrdersPerProduct.set(numberOfOrdersPerProduct);
    }

    public SimpleIntegerProperty numberOfOrdersPerProductProperty() {
        return numberOfOrdersPerProduct;
    }

    public SimpleStringProperty isProductFavouriteProperty() {
        return isProductFavourite;
    }

    public String getProductSubcategory() {
        return productSubcategory.get();
    }

    public void setProductSubcategory(String productSubcategory) {
        this.productSubcategory.set(productSubcategory);
    }

    public int getProductId() {
        return productId.get();
    }


    public void setProductId(int productId) {
        this.productId.set(productId);
    }

    public String getProductName() {
        return productName.get();
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public String getProductCategory() {
        return productCategory.get();
    }


    public void setProductCategory(String productCategory) {
        this.productCategory.set(productCategory);
    }

    public String getProductPrice() {
        return productPrice.get();
    }


    public void setProductPrice(String productPrice) {
        this.productPrice.set(productPrice);
    }


    public int getProductQuantity() {
        return productQuantity.get();
    }


    public void setProductQuantity(int productQuantity) {
        this.productQuantity.set(productQuantity);
    }

    public String getProductTotalValue() {
        return productTotalValue.get();
    }


    public void setProductTotalValue(String productTotalValue) {
        this.productTotalValue.set(productTotalValue);
    }

    public String getIsProductFavourite() {
        return isProductFavourite.get();
    }

    public void setIsProductFavourite(String isProductFavourite) {
        this.isProductFavourite.set(isProductFavourite);
    }
}
