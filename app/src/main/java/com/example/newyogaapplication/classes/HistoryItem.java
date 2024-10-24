package com.example.newyogaapplication.classes;

public class HistoryItem {
    private String CheckoutDate;
    private String ClassId;
    private String ClassName;
    private int PricePerClass;
    private int Quantity;
    private int TotalPrice;
    private String userId;
    private String email;  // Thêm trường email

    // Constructor mặc định (yêu cầu của Firebase)
    public HistoryItem() {}

    // Getter và setter
    public String getCheckoutDate() {
        return CheckoutDate;
    }

    public void setCheckoutDate(String checkoutDate) {
        this.CheckoutDate = checkoutDate;
    }

    public String getClassId() {
        return ClassId;
    }

    public void setClassId(String classId) {
        this.ClassId = classId;
    }

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        this.ClassName = className;
    }

    public int getPricePerClass() {
        return PricePerClass;
    }

    public void setPricePerClass(int pricePerClass) {
        this.PricePerClass = pricePerClass;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        this.Quantity = quantity;
    }

    public int getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.TotalPrice = totalPrice;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {  // Getter cho email
        return email;
    }

    public void setEmail(String email) {  // Setter cho email
        this.email = email;
    }
}
