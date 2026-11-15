
package campusmarketplace.model;

import java.math.BigDecimal;

public class Product {

    private int productId;
    private int sellerId;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private String status;
    private Integer reservedBy;

    public Product(int productId, int sellerId,
                   String title, String description,
                   String category, BigDecimal price,
                   String status, Integer reservedBy) {

        this.productId = productId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.status = status;
        this.reservedBy = reservedBy;
    }

    public int getProductId() {
        return productId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public Integer getReservedBy() {
        return reservedBy;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReservedBy(Integer reservedBy) {
        this.reservedBy = reservedBy;
    }
}
