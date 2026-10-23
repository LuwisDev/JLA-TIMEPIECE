package jlaluxurywatch;

// Watch
// One watch of the store: its brand, model, price, picture and details.
//
// OOP IDEA: ENCAPSULATION
// Every field is "private", so other classes cannot change a watch's price by accident.
// They can only READ the values through the public "getter" methods (getPrice(), getBrand()...).
public class Watch {
    // ----- the data of a watch (private = hidden from other classes) -----
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
    private boolean featured;      // true = shown on the customer's Home page

    // ----- constructors: they build a new Watch object -----

    //  Constructor with everything.
    public Watch(String brand, String model, double price, String imagePath,
                 String caseSize, String movement, String waterResistance,
                 String strap, String dial, String description) {
        // "this.brand" is the field of the object, "brand" is the value that was passed in
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

    // CONSTRUCTOR OVERLOADING (a kind of polymorphism): same name, different parameters.
    // Java picks the right one from what you pass in. These shorter ones fill the rest with "".

    public Watch(String brand, String model, double price, String imagePath) {
        this(brand, model, price, imagePath, "", "", "", "", "", "");
    }

    public Watch(String brand, String model, double price) {
        this(brand, model, price, "");
    }

    // ----- getters: the only way other classes can read the private fields -----
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

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    //  Brand and model together, like "Rolex Submariner".
    public String getDisplayName() {
        return (brand + " " + model).trim();
    }

    // The specifications that are filled in, one per line, like "Case size: 41 mm".
    // Empty ones are left out. Used by the details window and the admin preview.
    public String getSpecsText() {
        String specs = "";
        if (!caseSize.isEmpty()) specs += "Case size: " + caseSize + "\n";
        if (!movement.isEmpty()) specs += "Movement: " + movement + "\n";
        if (!waterResistance.isEmpty()) specs += "Water resistance: " + waterResistance + "\n";
        if (!strap.isEmpty()) specs += "Strap / Bracelet: " + strap + "\n";
        if (!dial.isEmpty()) specs += "Dial: " + dial + "\n";
        return specs;
    }

    // toString() is what you get when a Watch is turned into text (for example in println).
    @Override
    public String toString() {
        return getDisplayName() + " - " + Money.format(price);
    }
}
