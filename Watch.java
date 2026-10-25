package jlaluxurywatch;

public class Watch {
    private String brand;
    private String model;
    private String imagePath;
    private double price;
    private String description;
    private String caseSize;
    private String movement;
    private String waterResistance;
    private String strap;
    private String dial;
    private boolean featured;
    private boolean available = true;

    public Watch(String brand, String model, double price, String imagePath,
                 String caseSize, String movement, String waterResistance,
                 String strap, String dial, String description) {
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.imagePath = imagePath;
        this.caseSize = caseSize;
        this.movement = movement;
        this.waterResistance = waterResistance;
        this.strap = strap;
        this.dial = dial;
        this.description = description;
    }

    public Watch(String brand, String model, double price, String imagePath) {
        this(brand, model, price, imagePath, "", "", "", "", "", "");
    }

    public Watch(String brand, String model, double price) {
        this(brand, model, price, "");
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public String getDescription() { return description; }
    public String getCaseSize() { return caseSize; }
    public String getMovement() { return movement; }
    public String getWaterResistance() { return waterResistance; }
    public String getStrap() { return strap; }
    public String getDial() { return dial; }
    public boolean isFeatured() { return featured; }
    public boolean isAvailable() { return available; }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAvailabilityText() {
        if (available) {
            return "Available";
        }
        return "Not Available";
    }

    public String getDisplayName() {
        return (brand + " " + model).trim();
    }

    public String getSpecsText() {
        String specs = "";
        if (!caseSize.isEmpty()) specs += "Case size: " + caseSize + "\n";
        if (!movement.isEmpty()) specs += "Movement: " + movement + "\n";
        if (!waterResistance.isEmpty()) specs += "Water resistance: " + waterResistance + "\n";
        if (!strap.isEmpty()) specs += "Strap / Bracelet: " + strap + "\n";
        if (!dial.isEmpty()) specs += "Dial: " + dial + "\n";
        return specs;
    }

    @Override
    public String toString() {
        return getDisplayName() + " - " + Money.format(price);
    }
}
