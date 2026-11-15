
package campusmarketplace.service;

import campusmarketplace.dao.ProductDAO;
import campusmarketplace.model.Product;

import java.sql.SQLException;
import java.util.List;

public class ReservationService {

    private final ProductDAO productDAO = new ProductDAO();

    // Reserve an available product
    public boolean reserveProduct(int productId, int buyerId)
            throws SQLException {

        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product ID.");
        }

        if (buyerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid buyer account.");
        }

        // The DAO performs an atomic conditional UPDATE.
        // It also prevents reserving your own product.
        return productDAO.reserveProduct(productId, buyerId);
    }

    // Cancel a reservation
    public boolean cancelReservation(int productId, int userId)
            throws SQLException {

        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid product ID.");
        }

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user account.");
        }

        // Only the buyer or seller can cancel.
        return productDAO.cancelReservation(productId, userId);
    }

    // Retrieve products reserved by the logged-in student
    public List<Product> getMyReservations(int buyerId)
            throws SQLException {

        if (buyerId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid student account.");
        }

        return productDAO.getMyReservations(buyerId);
    }
    
public List<Product> getMyPurchases(int buyerId)
        throws SQLException {

    if (buyerId <= 0) {
        throw new IllegalArgumentException(
                "Invalid student account.");
    }

    return productDAO.getMyPurchases(buyerId);
}
 
}
