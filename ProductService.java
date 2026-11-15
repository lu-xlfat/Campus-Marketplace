
package campusmarketplace.service;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

import campusmarketplace.dao.ProductDAO;
import campusmarketplace.model.Product;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();

    public boolean addProduct(int sellerId, String title,
            String description, String category,
            String priceText) throws SQLException {

        if (sellerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid seller account.");
        }

        BigDecimal price = validateProduct(
                title, description, category, priceText);

        Product product = new Product(
                0,
                sellerId,
                title.trim(),
                description == null ? "" : description.trim(),
                category.trim(),
                price,
                "AVAILABLE",
                null
        );

        return productDAO.addProduct(product);
    }

    public List<Product> getMyProducts(int sellerId)
            throws SQLException {

        if (sellerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid seller account.");
        }

        return productDAO.getProductsBySeller(sellerId);
    }

    public Product getProductById(int productId)
            throws SQLException {

        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product ID.");
        }

        return productDAO.getProductById(productId);
    }

    public boolean updateProduct(int productId,
            int sellerId, String title, String description,
            String category, String priceText)
            throws SQLException {

        if (sellerId <= 0 || productId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product or seller.");
        }

        BigDecimal price = validateProduct(
                title, description, category, priceText);

        Product existing = productDAO.getProductById(productId);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Product not found.");
        }

        if (existing.getSellerId() != sellerId) {
            throw new IllegalArgumentException(
                    "You cannot edit another student's product.");
        }

        if (!"AVAILABLE".equals(existing.getStatus())) {
            throw new IllegalArgumentException(
                    "Only available products can be edited.");
        }

        Product updated = new Product(
                productId,
                sellerId,
                title.trim(),
                description == null ? "" : description.trim(),
                category.trim(),
                price,
                existing.getStatus(),
                existing.getReservedBy()
        );

        return productDAO.updateProduct(updated);
    }

    public boolean removeProduct(int productId, int sellerId)
            throws SQLException {

        if (productId <= 0 || sellerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product or seller.");
        }

        Product existing = productDAO.getProductById(productId);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Product not found.");
        }

        if (existing.getSellerId() != sellerId) {
            throw new IllegalArgumentException(
                    "You cannot remove another student's product.");
        }

        if (!"AVAILABLE".equals(existing.getStatus())) {
            throw new IllegalArgumentException(
                    "Only available products can be removed.");
        }

        return productDAO.removeProduct(productId, sellerId);
    }

    private BigDecimal validateProduct(String title,
            String description, String category,
            String priceText) {

        if (title == null || title.isBlank()
                || title.trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Product title must be 1 to 150 characters.");
        }

        if (category == null || category.isBlank()
                || category.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Select a valid product category.");
        }

        if (description != null
                && description.length() > 5000) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 5000 characters.");
        }

        if (priceText == null || priceText.isBlank()) {
            throw new IllegalArgumentException(
                    "Enter a product price.");
        }

        BigDecimal price;

        try {
            price = new BigDecimal(priceText.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Price must be a valid number.");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0
                || price.compareTo(
                        new BigDecimal("99999999.99")) > 0
                || price.scale() > 2) {
            throw new IllegalArgumentException(
                    "Price must be between 0 and 99999999.99 "
                    + "with at most 2 decimal places.");
        }

        return price;
    }
    
public List<Product> searchMarketplace(
        int currentUserId,
        String keyword,
        String category,
        String sortOption) throws SQLException {

    if (currentUserId <= 0) {
        throw new IllegalArgumentException(
                "Invalid student account.");
    }

    // Get available products from other sellers
    List<Product> products =
            productDAO.getAvailableProducts(currentUserId);

    Stream<Product> stream = products.stream();

    // Search by title or description
    if (keyword != null && !keyword.isBlank()) {

        String searchText =
                keyword.trim().toLowerCase(Locale.ROOT);

        stream = stream.filter(product -> {

            String title = product.getTitle() == null
                    ? "" : product.getTitle().toLowerCase(Locale.ROOT);

            String description = product.getDescription() == null
                    ? "" : product.getDescription().toLowerCase(Locale.ROOT);

            return title.contains(searchText)
                    || description.contains(searchText);
        });
    }

    // Filter by product category
    if (category != null
            && !category.equalsIgnoreCase("All Categories")) {

        stream = stream.filter(product ->
                product.getCategory().equalsIgnoreCase(category));
    }

    // Sort products
    if ("Price: Low to High".equals(sortOption)) {

        stream = stream.sorted(
                Comparator.comparing(Product::getPrice));

    } else if ("Price: High to Low".equals(sortOption)) {

        stream = stream.sorted(
                Comparator.comparing(
                        Product::getPrice).reversed());

    } else if ("Title: A to Z".equals(sortOption)) {

        stream = stream.sorted(
                Comparator.comparing(
                        Product::getTitle,
                        String.CASE_INSENSITIVE_ORDER));
    }

    return stream.toList();
}

public boolean markProductSold(int productId, int sellerId)
        throws SQLException {

    if (productId <= 0 || sellerId <= 0) {
        throw new IllegalArgumentException(
                "Invalid product or seller ID.");
    }

    Product product = productDAO.getProductById(productId);

    if (product == null) {
        throw new IllegalArgumentException(
                "Product not found.");
    }

    if (product.getSellerId() != sellerId) {
        throw new IllegalArgumentException(
                "You cannot sell another student's product.");
    }

    if (!"AVAILABLE".equals(product.getStatus())
            && !"RESERVED".equals(product.getStatus())) {

        throw new IllegalArgumentException(
                "This product cannot be marked as sold.");
    }

    return productDAO.markProductSold(productId, sellerId);
}

}
