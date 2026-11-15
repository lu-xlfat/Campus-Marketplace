
package campusmarketplace.dao;

import campusmarketplace.model.Product;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public boolean addProduct(Product product) throws SQLException {

        String sql = """
            INSERT INTO products
            (seller_id, title, description, category, price, status)
            VALUES (?, ?, ?, ?, ?, 'AVAILABLE')
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product.getSellerId());
            stmt.setString(2, product.getTitle());
            stmt.setString(3, product.getDescription());
            stmt.setString(4, product.getCategory());
            stmt.setBigDecimal(5, product.getPrice());

            return stmt.executeUpdate() == 1;
        }
    }

    public Product getProductById(int productId)
            throws SQLException {

        String sql = """
            SELECT * FROM products
            WHERE product_id = ? AND status <> 'REMOVED'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        }

        return null;
    }

    public List<Product> getProductsBySeller(int sellerId)
            throws SQLException {

        List<Product> products = new ArrayList<>();

        String sql = """
            SELECT * FROM products
            WHERE seller_id = ? AND status <> 'REMOVED'
            ORDER BY created_at DESC, product_id DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        }

        return products;
    }

    public boolean updateProduct(Product product)
            throws SQLException {

        String sql = """
            UPDATE products
            SET title = ?, description = ?,
                category = ?, price = ?
            WHERE product_id = ?
              AND seller_id = ?
              AND status = 'AVAILABLE'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getTitle());
            stmt.setString(2, product.getDescription());
            stmt.setString(3, product.getCategory());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getProductId());
            stmt.setInt(6, product.getSellerId());

            return stmt.executeUpdate() == 1;
        }
    }

    public boolean removeProduct(int productId, int sellerId)
            throws SQLException {

        String sql = """
            UPDATE products
            SET status = 'REMOVED', reserved_by = NULL
            WHERE product_id = ?
              AND seller_id = ?
              AND status = 'AVAILABLE'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            stmt.setInt(2, sellerId);

            return stmt.executeUpdate() == 1;
        }
    }

    private Product mapProduct(ResultSet rs)
            throws SQLException {

        int reservedId = rs.getInt("reserved_by");
        Integer reservedBy =
                rs.wasNull() ? null : reservedId;

        return new Product(
                rs.getInt("product_id"),
                rs.getInt("seller_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getBigDecimal("price"),
                rs.getString("status"),
                reservedBy
        );
    }
    
public List<Product> getAvailableProducts(int currentUserId)
        throws SQLException {

    List<Product> products = new ArrayList<>();

    String sql = """
        SELECT *
        FROM products
        WHERE status = 'AVAILABLE'
          AND seller_id <> ?
        ORDER BY created_at DESC, product_id DESC
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, currentUserId);

        try (ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
    }

    return products;
}

public boolean reserveProduct(int productId, int buyerId)
        throws SQLException {

    if (productId <= 0 || buyerId <= 0) {
        throw new IllegalArgumentException(
                "Invalid product or buyer ID.");
    }

    String sql = """
        UPDATE products
        SET status = 'RESERVED',
            reserved_by = ?
        WHERE product_id = ?
          AND status = 'AVAILABLE'
          AND seller_id <> ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, buyerId);
        stmt.setInt(2, productId);
        stmt.setInt(3, buyerId);

        int rowsUpdated = stmt.executeUpdate();

        return rowsUpdated == 1;
    }
}

public boolean cancelReservation(int productId, int userId)
        throws SQLException {

    if (productId <= 0 || userId <= 0) {
        throw new IllegalArgumentException(
                "Invalid product or user ID.");
    }

    String sql = """
        UPDATE products
        SET status = 'AVAILABLE',
            reserved_by = NULL
        WHERE product_id = ?
          AND status = 'RESERVED'
          AND (reserved_by = ? OR seller_id = ?)
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, productId);
        stmt.setInt(2, userId);
        stmt.setInt(3, userId);

        return stmt.executeUpdate() == 1;
    }
}

public List<Product> getMyReservations(int buyerId)
        throws SQLException {

    if (buyerId <= 0) {
        throw new IllegalArgumentException(
                "Invalid buyer ID.");
    }

    List<Product> reservations = new ArrayList<>();

    String sql = """
        SELECT *
        FROM products
        WHERE reserved_by = ?
          AND status = 'RESERVED'
        ORDER BY created_at DESC, product_id DESC
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, buyerId);

        try (ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapProduct(rs));
            }
        }
    }

    return reservations;
}

public boolean markProductSold(int productId, int sellerId)
        throws SQLException {

    if (productId <= 0 || sellerId <= 0) {
        throw new IllegalArgumentException(
                "Invalid product or seller ID.");
    }

    String sql = """
        UPDATE products
        SET status = 'SOLD'
        WHERE product_id = ?
          AND seller_id = ?
          AND status IN ('AVAILABLE', 'RESERVED')
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, productId);
        stmt.setInt(2, sellerId);

        return stmt.executeUpdate() == 1;
    }
}

public List<Product> getMyPurchases(int buyerId)
        throws SQLException {

    if (buyerId <= 0) {
        throw new IllegalArgumentException(
                "Invalid buyer ID.");
    }

    List<Product> purchases = new ArrayList<>();

    String sql = """
        SELECT *
        FROM products
        WHERE reserved_by = ?
          AND status = 'SOLD'
        ORDER BY created_at DESC, product_id DESC
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, buyerId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                purchases.add(mapProduct(rs));
            }
        }
    }

    return purchases;
}

}
