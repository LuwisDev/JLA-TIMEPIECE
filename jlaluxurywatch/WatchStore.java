package jlaluxurywatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// WatchStore
// The list of watches of the store, saved in data/watches.txt.
//   - The Customer program only READS the watches.
//   - The Admin program can ADD, EDIT and DELETE them (CRUD = Create, Read, Update, Delete).
//
// The first time the program runs there is no file yet, so the built-in watches below are
// written to it. After that the file is the truth: changes made by the admin are kept.
//
// ENCAPSULATION: the list is private; getWatches() gives out a COPY.
public class WatchStore {

    private static final String WATCHES_FILE = "data/watches.txt";

    private ArrayList<Watch> watches = new ArrayList<Watch>();

    public WatchStore() {
        if (new File(WATCHES_FILE).exists()) {
            load();
        } else {
            loadFeaturedWatches();      // first run: start with the built-in watches
            loadSampleWatches();
            save();
        }
    }

    //  Adds a built-in watch and marks it as featured (shown on the customer's Home page).
    private void addFeatured(Watch watch) {
        watch.setFeatured(true);
        watches.add(watch);
    }

    // brand, model, price, image, case size, movement, water resistance, strap/bracelet, dial, description
    //  Small helper so every watch below can be written in one short line.
    private Watch createWatch(String brand, String model, double price, String imagePath,
                              String caseSize, String movement, String waterResistance,
                              String strap, String dial, String description) {
        return new Watch(brand, model, price, imagePath, caseSize, movement, waterResistance,
                strap, dial, description);
    }

    //  The 6 featured watches. They show on the Home screen and also in Browse.
    private void loadFeaturedWatches() {
        addFeatured(createWatch("Rolex", "Submariner", 850000, "images/watch1rolexsubmariner.png",
                "41 mm", "Automatic, Cal. 3230", "300 m", "Oyster stainless steel bracelet", "Black",
                "The benchmark dive watch, with a black Cerachrom bezel, luminous markers and a robust Oyster case."));
        addFeatured(createWatch("Omega", "Seamaster Diver 300M", 400000, "images/watch2omegaseamasterDiver300m.png",
                "42 mm", "Automatic, Co-Axial Master Chronometer", "300 m", "Stainless steel bracelet", "Blue (wave pattern)",
                "A modern professional diver with a ceramic bezel, laser-engraved wave dial and helium escape valve."));
        addFeatured(createWatch("Cartier", "Santos de Cartier", 450000, "images/watch3cartiersantosdecartier.png",
                "Large model", "Automatic", "100 m", "Stainless steel bracelet (QuickSwitch)", "Silver with Roman numerals",
                "One of the first modern wristwatches, recognised by its square case and exposed screws on the bezel."));
        addFeatured(createWatch("Tissot", "PRX Powermatic 80", 45000, "images/watch4tissotprxpowermatic80.png",
                "40 mm", "Automatic, Powermatic 80 (80-hour power reserve)", "100 m", "Integrated stainless steel bracelet", "Blue (textured)",
                "A 1970s-inspired integrated-bracelet watch with a textured dial and an 80-hour power reserve."));
        addFeatured(createWatch("Seiko", "Presage Cocktail Time", 40000, "images/watch5seikopresagecocktailtime.png",
                "About 40 mm", "Automatic", "", "Stainless steel bracelet", "Green sunburst",
                "A dress watch inspired by Japanese cocktail culture, with a deep sunburst dial and a date window."));
        addFeatured(createWatch("Longines", "Master Collection", 160000, "images/watch6longinesmastercollection.png",
                "40 mm", "Automatic", "30 m", "Brown alligator-pattern leather strap", "Silver, Arabic numerals",
                "A classic dress watch with a silver barleycorn dial, blue steel hands and a date window."));
    }

    //  The other watches of the store, grouped by brand (3 per brand).
    private void loadSampleWatches() {
        watches.add(createWatch("Rolex", "Datejust", 650000, "images/rolex1.png",
                "", "Automatic, Perpetual rotor", "100 m", "", "",
                "The classic Rolex everyday watch, with a date window under the signature Cyclops lens."));
        watches.add(createWatch("Rolex", "GMT-Master II Pepsi", 1000000, "images/rolex2.png",
                "40 mm", "Automatic, Cal. 3285", "100 m", "", "Black",
                "A dual-time pilot's watch with the famous red-and-blue Cerachrom bezel."));
        watches.add(createWatch("Rolex", "Cosmograph Daytona", 1500000, "images/rolex3.png",
                "40 mm", "Automatic chronograph, Cal. 4131", "100 m", "", "",
                "The racing chronograph made for drivers, with a tachymetric bezel and three sub-dials."));

        watches.add(createWatch("Omega", "Constellation", 250000, "images/omega1.png",
                "", "Automatic, Co-Axial", "", "", "",
                "An elegant Omega dress watch known for its distinctive claws and refined dial."));
        watches.add(createWatch("Omega", "Speedmaster Professional", 420000, "images/omega2.png",
                "42 mm", "Manual-winding chronograph, Cal. 3861", "50 m", "", "Black",
                "The legendary Moonwatch, worn on the Apollo missions."));
        watches.add(createWatch("Omega", "Vintage Seamaster", 150000, "images/omega3.png",
                "", "", "", "", "",
                "A vintage Seamaster with the charm and patina of an earlier era."));

        watches.add(createWatch("Cartier", "Tank Louis Cartier", 670000, "images/cartier1.png",
                "", "Manual-winding", "", "Leather strap", "",
                "A rectangular icon of watchmaking, with slim, timeless lines."));
        watches.add(createWatch("Cartier", "Tortue Silver", 1000000, "images/cartier2.png",
                "", "", "", "", "Silver",
                "Cartier's distinctive tortoise-shaped case, in a silver-dial version."));
        watches.add(createWatch("Cartier", "Tortue Gold", 1400000, "images/cartier3.png",
                "", "", "", "", "",
                "The Tortue in gold, a rare and refined piece for collectors."));

        watches.add(createWatch("Tissot", "Seastar 1000", 30000, "images/tissot1.png",
                "40 mm", "Quartz", "300 m", "Stainless steel bracelet", "Black",
                "A sporty diver with a rotating black bezel, luminous markers and a date window."));
        watches.add(createWatch("Tissot", "Gentleman Powermatic 80 Silicium", 35000, "images/tissot2.png",
                "40 mm", "Automatic, Powermatic 80 with silicon balance spring", "100 m", "Stainless steel bracelet", "Blue sunray",
                "A refined everyday watch with a blue sunray dial and an 80-hour power reserve."));
        watches.add(createWatch("Tissot", "Gent Powermatic 80 Rose Gold", 40000, "images/tissot3.png",
                "", "Automatic, Powermatic 80", "", "Brown leather strap", "Silver sunray",
                "A dressy watch with a rose gold PVD case, silver dial and a brown leather strap."));

        watches.add(createWatch("Seiko", "5 Automatic Day-Date", 20000, "images/seiko1.png",
                "", "Automatic, day-date", "", "Stainless steel bracelet", "Green with gold accents",
                "A Seiko 5 with a green dial, gold-tone markers and a day-date window."));
        watches.add(createWatch("Seiko", "5 Sports GMT", 25000, "images/seiko2.png",
                "42.5 mm", "Automatic GMT, Cal. 4R34", "100 m", "Stainless steel Jubilee-style bracelet", "Black",
                "A sporty GMT with a black and grey 24-hour bezel and a red GMT hand for a second time zone."));
        watches.add(createWatch("Seiko", "Presage Classic Series", 30000, "images/seiko3.png",
                "", "Automatic, 23 jewels", "", "Stainless steel bracelet", "Silver",
                "A dress watch with a silver dial, slim sharp-edged hands and a multi-link bracelet."));

        watches.add(createWatch("Longines", "Master Collection Moonphase", 250000, "images/longines1.png",
                "40 mm", "Automatic, Cal. L899", "30 m", "", "",
                "A classic Longines with a moon phase display, calendar and a refined dial."));
        watches.add(createWatch("Longines", "Master Collection Chronograph", 300000, "images/longines2.png",
                "40 mm", "Automatic chronograph, Cal. L688", "30 m", "", "",
                "A dressy chronograph from the Master Collection, with two sub-dials and a date."));
        watches.add(createWatch("Longines", "Conquest Green Dial", 150000, "images/longines3.png",
                "", "Automatic", "", "", "Green",
                "A sporty-elegant Conquest with a rich green dial."));
    }

    // ---------------------------------------------------------------------------
    //                      READ  (Customer + Admin)
    // ---------------------------------------------------------------------------

    //  A copy of the list of all watches.
    public ArrayList<Watch> getWatches() {
        return new ArrayList<Watch>(watches);
    }

    //  A copy of the list of featured watches (shown on the Home screen).
    public ArrayList<Watch> getFeaturedWatches() {
        ArrayList<Watch> result = new ArrayList<Watch>();
        for (int i = 0; i < watches.size(); i++) {
            if (watches.get(i).isFeatured()) {
                result.add(watches.get(i));
            }
        }
        return result;
    }

    //  Finds a watch by its name like "Rolex Submariner". Returns null if there is none.
    public Watch findByName(String name) {
        for (int i = 0; i < watches.size(); i++) {
            if (watches.get(i).getDisplayName().equalsIgnoreCase(name.trim())) {
                return watches.get(i);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------
    //                      CREATE / UPDATE / DELETE  (Admin)
    // ---------------------------------------------------------------------------

    //  CREATE: adds a new watch. Returns an error message, or null if it worked.
    public String addWatch(Watch watch) {
        if (findByName(watch.getDisplayName()) != null) {
            return "A watch named \"" + watch.getDisplayName() + "\" already exists.";
        }
        watches.add(watch);
        save();
        return null;
    }

    //  UPDATE: replaces oldWatch with newWatch (same place in the list). Returns an error message or null.
    public String updateWatch(Watch oldWatch, Watch newWatch) {
        Watch sameName = findByName(newWatch.getDisplayName());
        if (sameName != null && sameName != oldWatch) {
            return "Another watch is already named \"" + newWatch.getDisplayName() + "\".";
        }
        int index = watches.indexOf(oldWatch);
        if (index < 0) {
            return "That watch no longer exists.";
        }
        watches.set(index, newWatch);
        save();
        return null;
    }

    //  DELETE: removes a watch. Past orders keep their own copy of the watch, so history is not lost.
    public void deleteWatch(Watch watch) {
        watches.remove(watch);
        save();
    }

    // ---------------------------------------------------------------------------
    //                      SAVING AND LOADING THE FILE
    // ---------------------------------------------------------------------------

    //  Reads data/watches.txt. One watch per line, fields separated by TAB.
    private void load() {
        watches.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(WATCHES_FILE));
            String line = reader.readLine();
            while (line != null) {
                String[] f = line.split("\t", -1);
                if (f.length >= 11) {
                    try {
                        Watch watch = new Watch(f[0], f[1], Double.parseDouble(f[2]), f[3],
                                f[4], f[5], f[6], f[7], f[8], f[9]);
                        watch.setFeatured(f[10].equals("1"));
                        watches.add(watch);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipped a bad line in " + WATCHES_FILE);
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Could not read " + WATCHES_FILE);
        }
    }

    //  Writes ALL watches to the file.
    private void save() {
        try {
            File dataFolder = new File("data");
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
            PrintWriter writer = new PrintWriter(new FileWriter(WATCHES_FILE));
            for (int i = 0; i < watches.size(); i++) {
                Watch w = watches.get(i);
                writer.println(clean(w.getBrand()) + "\t" + clean(w.getModel()) + "\t" + w.getPrice() + "\t"
                        + clean(w.getImagePath()) + "\t" + clean(w.getCaseSize()) + "\t" + clean(w.getMovement()) + "\t"
                        + clean(w.getWaterResistance()) + "\t" + clean(w.getStrap()) + "\t" + clean(w.getDial()) + "\t"
                        + clean(w.getDescription()) + "\t" + (w.isFeatured() ? "1" : "0"));
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not save " + WATCHES_FILE);
        }
    }

    //  Removes tabs and new lines so a value cannot break the one-line-per-watch format.
    private String clean(String text) {
        return text.replace("\t", " ").replace("\r", " ").replace("\n", " ");
    }
}
