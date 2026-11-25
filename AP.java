package ap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class AP extends JFrame {
    private static final Color WINE  = new Color(76, 8, 31);
    private static final Color GOLD  = new Color(205, 169, 91);
    private static final Color CREAM = new Color(250, 247, 235);
    private static final Color INK   = new Color(55, 42, 35);
    private static final Path DATA   = Paths.get(System.getProperty("user.home"), ".imperial-solis");

    // Linked to hotel_db in XAMPP MySQL
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/hotel_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private final List<Room> rooms = Arrays.asList(
        new Room("Garden View Room", 780, 2, "Tranquil escape with lush garden vistas, classic furnishings, queen bed, Wi-Fi, and coffee station."),
        new Room("Deluxe King Room", 820, 2, "Spacious and serene, featuring panoramic views, a king-size bed, soaking tub, high-speed Wi-Fi, and mini bar."),
        new Room("Imperial Suite", 850, 4, "Flagship suite with floor-to-ceiling windows, private terrace, king bed, jacuzzi, and 24/7 butler service."),
        new Room("Executive Twin Room", 980, 4, "Refined comfort suited for business travelers or companions with two queen beds, workspace, high-speed Wi-Fi, and mini bar."),
        new Room("Solis Grand Room", 1250, 3, "Elevated luxury with twin seating areas, walk-in wardrobe, king bed, smart TV, and oversized marble bath."),
        new Room("Penthouse Suite", 1400, 4, "Sprawling two-level suite with a king bed, private rooftop lounge, infinity bath, and dedicated chef."),
        new Room("Royal Ocean Villa", 1650, 4, "Private coastal sanctuary with panoramic deck, plunge pool, and outdoor rain shower."),
        new Room("Presidential Suite", 1900, 6, "Double-height glass atrium, private dining room, grand piano, and personal concierge."),
        new Room("Kiddies Room (Add-on)", 300, 2, "Available in premium rooms — adjoining kids' room with single/bunk bed, beach view, Wi-Fi, and play area.")
    );

    private final List<SpecialOffer> specialOffers = Arrays.asList(
        new SpecialOffer("Stay 3 Nights, Pay for 2", "Save 20% • Extended Luxury", "R 1,640.00 / 3-night package",
            "Unwind longer in our Deluxe King Room or Imperial Suite with a complimentary third night, daily Continental Morning breakfast for two, and 2:00 PM late checkout.", 1),
        new SpecialOffer("Imperial Romance & Spa Retreat", "Most Popular • Couples Escape", "R 1,350.00 / night",
            "An intimate escape featuring a rose-petal suite turndown, signature Rose Petal Martinis, 60-min couples warm stone massage, and private rooftop jacuzzi access.", 2),
        new SpecialOffer("Kids Stay & Dine Free", "Family Deal • Solis Family Getaway", "R 1,150.00 / night",
            "Book any suite and receive an adjoining Kiddies Room included, complimentary Junior Menu meals for kids under 12, and full supervised Kids' Club & WaterSlide access.", 8),
        new SpecialOffer("Advance Purchase Suite Escape", "Save 15% • Early Bird Privilege", "From R 663.00 / night",
            "Reserve your stay at least 14 days in advance to enjoy 15% off our best available room rates, complimentary private chauffeur airport transfers, and priority upgrade.", 0)
    );

    private final List<ExtraItem> extraMenu = Arrays.asList(
        new ExtraItem("Breakfast", "Continental Morning Breakfast", 180, "Full mezze breakfast spread with freshly baked pastries, seasonal fruit, and artisan coffee."),
        new ExtraItem("Breakfast", "The Imperial Spread", 220, "Mediterranean starter plate with artisan dips, poached eggs, and warm sourdough toast."),
        new ExtraItem("Main", "Braised Short Rib Dinner", 320, "Slow-braised beef served with fragrant herb rice and signature reduction."),
        new ExtraItem("Main", "Rotisserie Spiced Chicken", 260, "Whole flame-roasted herb & peri-spiced chicken with roasted baby potatoes."),
        new ExtraItem("Main", "Seared Tuna Salad", 240, "Sesame-crusted seared tuna over crisp garden greens with citrus vinaigrette."),
        new ExtraItem("Kids Meal", "Burger & Fries", 110, "Juicy mini beef or chicken burger served with crispy golden fries."),
        new ExtraItem("Plant-Based", "Heritage Spice Bowl", 195, "Spiced tomato, roasted heirloom vegetables, chickpeas, and fragrant grains."),
        new ExtraItem("Cocktail", "Rose Petal Martini", 140, "Botanical gin infused with Damascus rose water, lychee, and edible petals."),
        new ExtraItem("Dessert", "Acai Bliss Bowl", 120, "Chilled organic acai berry bowl topped with toasted coconut, berries, and honey."),
        new ExtraItem("Dessert", "Warm Syrniki", 130, "Golden cottage cheese pancakes dusted with vanilla sugar and fresh forest berries."),
        new ExtraItem("Dessert", "Solara Matcha Cream Cake", 115, "Delicate ceremonial-grade matcha sponge layered with whipped mascarpone cream."),
        new ExtraItem("Kids Dessert", "Vanilla Cream Cupcake", 65, "Fun vanilla bean cupcake swirled with pastel buttercream for young and old."),
        new ExtraItem("Amenity • Pool", "Outdoor Infinity Pool Day Pass", 150, "Sun-drenched pool deck with premium loungers and swim-up bar access."),
        new ExtraItem("Amenity • Fitness", "Fitness Centre Session", 120, "State-of-the-art equipment, weight racks, and panoramic city views."),
        new ExtraItem("Amenity • Wellness", "Yoga & Pilates Studio Class", 180, "Group session led by certified instructors in our garden-facing studio."),
        new ExtraItem("Amenity • Spa", "Traditional Finnish Sauna Session", 250, "Authentic Finnish wood sauna with birch fragrance and therapeutic steam."),
        new ExtraItem("Amenity • Rooftop", "Rooftop Jacuzzi (Private Sunset Hour)", 350, "Seamless-edge hot tub at sunset overlooking the city skyline."),
        new ExtraItem("Amenity • Events", "Grand Ballroom Event Access", 2500, "Crystal chandeliers, grand stage, and banquet seating access."),
        new ExtraItem("Entertainment", "Private Cinema Screening", 220, "80-seat screening room with Dolby Atmos surround sound and in-seat service."),
        new ExtraItem("Entertainment", "Imperial Solis Bar VIP Table", 200, "Luxury bar table reservation with live music and ambient lighting."),
        new ExtraItem("Kids' Club", "Kids Club & WaterSlide Pass", 150, "Adventure Playroom, WaterSlide, Indoor Soft-Play, Children's Theatre & Jumping Castle.")
    );

    private final List<Booking> bookings = new ArrayList<>();
    private final List<Booking> displayedBookings = new ArrayList<>();
    private String currentUser = "Admin";
    private JLabel welcomeLabel;
    private DefaultListModel<String> bookingListModel = new DefaultListModel<>();
    private JList<String> bookingList;
    private JTextField searchField, alertSearchField;
    private JTextField guestNameField, guestEmailField, checkInField, checkOutField;
    private JComboBox<String> roomChoice, orderBookingChoice;
    private JSpinner guestsSpinner;
    private JTextArea roomDescription, orderSummaryArea;
    private JLabel totalLabel;
    private Booking lastBooking;
    private JTabbedPane tabs;
    private javax.swing.Timer alertAutoRefreshTimer;

    public AP() {
        super("Imperial Solis Hotel | Management & Concierge System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1140, 760);
        setMinimumSize(new Dimension(960, 660));
        setLocationRelativeTo(null);
        showWelcome();
    }

    private void showWelcome() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WINE);
        JPanel hero = new JPanel();
        hero.setBackground(WINE);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(65, 70, 55, 70));

        JLabel crest = new JLabel("♛  S  ♛");
        crest.setForeground(GOLD); crest.setFont(new Font("Serif", Font.BOLD, 42));
        crest.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = new JLabel("IMPERIAL SOLIS HOTEL");
        name.setForeground(GOLD); name.setFont(new Font("Serif", Font.BOLD, 34));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("HOTEL & RESORT MANAGEMENT");
        sub.setForeground(CREAM); sub.setFont(new Font("SansSerif", Font.PLAIN, 15));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tagline = new JLabel("Luxury Stays, Unforgettable Moments");
        tagline.setForeground(CREAM); tagline.setFont(new Font("Serif", Font.ITALIC, 19));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel imageHint = new JLabel("✦  SYNCED WITH WEBSITE & GUEST PORTAL  ✦");
        imageHint.setForeground(GOLD); imageHint.setFont(new Font("SansSerif", Font.BOLD, 14));
        imageHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton start = goldButton("GET STARTED");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.addActionListener(e -> showAuth());
        hero.add(Box.createVerticalGlue()); hero.add(crest); hero.add(Box.createVerticalStrut(18));
        hero.add(name); hero.add(sub); hero.add(Box.createVerticalStrut(22));
        hero.add(tagline); hero.add(Box.createVerticalStrut(55)); hero.add(imageHint);
        hero.add(Box.createVerticalStrut(55)); hero.add(start); hero.add(Box.createVerticalGlue());
root.add(hero, BorderLayout.CENTER);
setContentPane(root); revalidate(); repaint();
}
private boolean authenticateAdmin(String u, String p) {
String sql = "SELECT admin_id FROM admins WHERE username = ? AND password_hash = SHA2(?, 256)";
try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setString(1, u);
ps.setString(2, p);
try (ResultSet rs = ps.executeQuery()) {
return rs.next();
}
} catch (SQLException ex) {
message("Database error: Make sure MySQL is running in XAMPP.\n" + ex.getMessage());
return false;
}
}
private void showAuth() {
JPanel panel = new JPanel(new GridBagLayout());
panel.setBackground(CREAM);
GridBagConstraints g = new GridBagConstraints();
g.insets = new Insets(9, 12, 9, 12); g.fill = GridBagConstraints.HORIZONTAL; g.gridx = 0;
JLabel crest = new JLabel("♛  S  ♛", SwingConstants.CENTER);
crest.setFont(new Font("Serif", Font.BOLD, 34)); crest.setForeground(WINE);
JLabel title = new JLabel("IMPERIAL SOLIS HOTEL", SwingConstants.CENTER);
title.setFont(new Font("Serif", Font.BOLD, 22)); title.setForeground(WINE);
JTextField username = field("");
JPasswordField password = new JPasswordField();
styleField(password);
JButton login = wineButton("ADMIN LOGIN");
g.gridy = 0; panel.add(crest, g);
g.gridy++; panel.add(title, g);
g.gridy++; panel.add(label("Admin Name"), g);
g.gridy++; panel.add(username, g);
g.gridy++; panel.add(label("Password"), g);
g.gridy++; panel.add(password, g);
g.gridy++; panel.add(login, g);
login.addActionListener(e -> {
String u = username.getText().trim();
String p = new String(password.getPassword());
if (u.isEmpty() || p.isEmpty()) {
message("Enter your admin name and password to continue.");
return;
}
if (authenticateAdmin(u, p)) {
currentUser = u;
loadBookings();
showDashboard();
} else {
message("Access denied. Invalid admin name or password.");
password.setText("");
}
});
setContentPane(centered(panel)); revalidate(); repaint();
}
private void showDashboard() {
JPanel root = new JPanel(new BorderLayout());
root.setBackground(CREAM);
JPanel header = new JPanel(new BorderLayout());
header.setBackground(WINE); header.setBorder(new EmptyBorder(14, 22, 14, 22));
JLabel brand = new JLabel("♛  IMPERIAL SOLIS HOTEL  |  CONCIERGE & MANAGEMENT");
brand.setForeground(GOLD); brand.setFont(new Font("Serif", Font.BOLD, 19));
welcomeLabel = new JLabel("Admin: " + currentUser + "  |  Gmail: lifecyclem4@gmail.com");
welcomeLabel.setForeground(CREAM);
header.add(brand, BorderLayout.WEST); header.add(welcomeLabel, BorderLayout.EAST);
root.add(header, BorderLayout.NORTH);
tabs = new JTabbedPane();
tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
tabs.addTab("Home", homePanel(tabs));
tabs.addTab("Available Rooms (9)", roomsPanel());
tabs.addTab("Special Offers (4)", offersPanel());
tabs.addTab("Book a Room", bookingPanel());
tabs.addTab("Extras, Food & Amenities", extrasPanel());
tabs.addTab("Bookings & Payments", bookingsPanel());
tabs.addTab("Guest Reviews", reviewsPanel());
tabs.addTab("Live Alerts & Gmail", notificationsPanel());
root.add(tabs, BorderLayout.CENTER);
JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
footer.setBackground(CREAM);
JButton logout = wineButton("LOG OUT");
logout.addActionListener(e -> showWelcome());
footer.add(logout); root.add(footer, BorderLayout.SOUTH);
setContentPane(root); revalidate(); repaint();
}
private JPanel homePanel(JTabbedPane tabs) {
JPanel p = new JPanel(new GridBagLayout()); p.setBackground(CREAM);
JPanel content = new JPanel(); content.setBackground(CREAM);
content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
JLabel heading = new JLabel("Experience True Luxury");
heading.setFont(new Font("Serif", Font.BOLD, 32)); heading.setForeground(WINE);
JLabel tagline = new JLabel("Live synchronized with Imperial Solis Hotel Website & Guest Portal.");
tagline.setFont(new Font("Serif", Font.ITALIC, 17)); tagline.setForeground(INK);
content.add(heading); content.add(Box.createVerticalStrut(10)); content.add(tagline);
content.add(Box.createVerticalStrut(32));
JPanel cards = new JPanel(new GridLayout(2, 3, 16, 16)); cards.setBackground(CREAM);
List labels = Arrays.asList("AVAILABLE ROOMS", "BOOKINGS & PAYMENTS", "SPECIAL OFFERS", "FOOD & AMENITIES", "GUEST REVIEWS", "LIVE GMAIL ALERTS");
List desc = Arrays.asList("All 9 rooms & suites synced", "Manage stays, payments & 5-day policy", "Discover 4 seasonal packages", "Dining, Kids' Club & Spa extras", "Read ratings from website guests", "Track guest portal logins live");
List targetTabs = Arrays.asList(1, 5, 2, 4, 6, 7);
for (int i = 0; i < labels.size(); i++) {
final int tabIdx = (int) targetTabs.get(i);
JPanel card = new JPanel(new BorderLayout(5, 8)); card.setBackground(Color.WHITE);
card.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(18, 18, 18, 18)));
card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
JLabel l = new JLabel((String) labels.get(i), SwingConstants.CENTER); l.setForeground(WINE);
l.setFont(new Font("SansSerif", Font.BOLD, 15));
JLabel d = new JLabel((String) desc.get(i), SwingConstants.CENTER); d.setForeground(INK);
card.add(l, BorderLayout.CENTER); card.add(d, BorderLayout.SOUTH);
card.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent e) { tabs.setSelectedIndex(tabIdx); }
});
cards.add(card);
}
content.add(cards);
JButton book = goldButton("BOOK YOUR STAY");
book.addActionListener(e -> tabs.setSelectedIndex(3));
content.add(Box.createVerticalStrut(28)); content.add(book);
p.add(content); return p;
}
private JPanel roomsPanel() {
JPanel p = new JPanel(new BorderLayout(15, 15)); p.setBackground(CREAM);
p.setBorder(new EmptyBorder(22, 25, 22, 25));
p.add(sectionTitle("AVAILABLE ROOMS & SUITES"), BorderLayout.NORTH);
JPanel list = new JPanel(new GridLayout(0, 1, 12, 12)); list.setBackground(CREAM);
for (int i = 0; i < rooms.size(); i++) {
Room r = rooms.get(i);
final int roomIndex = i;
JPanel card = new JPanel(new BorderLayout(15, 8)); card.setBackground(Color.WHITE);
card.setBorder(new CompoundBorder(new LineBorder(new Color(220, 210, 185), 1, true), new EmptyBorder(16, 20, 16, 20)));
JLabel name = new JLabel(r.name); name.setFont(new Font("Serif", Font.BOLD, 20)); name.setForeground(WINE);
JLabel price = new JLabel("R " + money(r.price) + " / night  •  Up to " + r.capacity + " guests");
price.setFont(new Font("SansSerif", Font.BOLD, 13)); price.setForeground(INK);
JLabel desc = new JLabel(r.description);
JPanel text = new JPanel(new GridLayout(3, 1)); text.setOpaque(false);
text.add(name); text.add(price); text.add(desc);
JButton select = goldButton("BOOK NOW");
select.addActionListener(e -> {
if (roomChoice != null) roomChoice.setSelectedIndex(roomIndex);
if (tabs != null) tabs.setSelectedIndex(3);
});
card.add(text, BorderLayout.CENTER); card.add(select, BorderLayout.EAST); list.add(card);
}
JScrollPane scroll = new JScrollPane(list);
scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(16);
p.add(scroll, BorderLayout.CENTER);
return p;
}
private JPanel offersPanel() {
JPanel p = new JPanel(new BorderLayout(15, 15)); p.setBackground(CREAM);
p.setBorder(new EmptyBorder(22, 25, 22, 25));
p.add(sectionTitle("SPECIAL OFFERS"), BorderLayout.NORTH);
JPanel list = new JPanel(new GridLayout(0, 1, 14, 14)); list.setBackground(CREAM);
for (SpecialOffer offer : specialOffers) {
JPanel card = new JPanel(new BorderLayout(15, 8)); card.setBackground(Color.WHITE);
card.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(18, 22, 18, 22)));
JLabel badge = new JLabel(offer.badge.toUpperCase());
badge.setFont(new Font("SansSerif", Font.BOLD, 11)); badge.setForeground(new Color(145, 105, 35));
JLabel title = new JLabel(offer.title);
title.setFont(new Font("Serif", Font.BOLD, 21)); title.setForeground(WINE);
JLabel price = new JLabel(offer.priceLabel);
price.setFont(new Font("SansSerif", Font.BOLD, 14)); price.setForeground(INK);
JTextArea desc = new JTextArea(offer.description);
desc.setLineWrap(true); desc.setWrapStyleWord(true); desc.setEditable(false); desc.setOpaque(false);
desc.setFont(new Font("SansSerif", Font.PLAIN, 13)); desc.setForeground(INK);
JPanel text = new JPanel(); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS)); text.setOpaque(false);
text.add(badge); text.add(Box.createVerticalStrut(3));
text.add(title); text.add(Box.createVerticalStrut(4));
text.add(price); text.add(Box.createVerticalStrut(6));
text.add(desc);
JButton claim = wineButton("CLAIM OFFER");
claim.addActionListener(e -> {
if (roomChoice != null && offer.defaultRoomIndex < rooms.size()) {
roomChoice.setSelectedIndex(offer.defaultRoomIndex);
}
if (tabs != null) tabs.setSelectedIndex(3);
message("Offer selected: " + offer.title + "\nEnter Guest name, email, and dates below to reserve.");
});
JPanel btnWrap = new JPanel(new GridBagLayout()); btnWrap.setOpaque(false); btnWrap.add(claim);
card.add(text, BorderLayout.CENTER); card.add(btnWrap, BorderLayout.EAST);
list.add(card);
}
JScrollPane scroll = new JScrollPane(list);
scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(16);
p.add(scroll, BorderLayout.CENTER);
return p;
}
private JPanel bookingPanel() {
JPanel p = new JPanel(new GridBagLayout()); p.setBackground(CREAM);
JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE);
form.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(16, 26, 16, 26)));
GridBagConstraints g = new GridBagConstraints();
g.insets = new Insets(4, 8, 4, 8); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 0.5;
g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
form.add(sectionTitle("BOOK A ROOM (GUEST PAYS ON PORTAL)"), g);
g.gridwidth = 1; g.gridy++;
g.gridx = 0; form.add(label("Guest Name"), g);
g.gridx = 1; form.add(label("Guest Email"), g);
g.gridy++;
g.gridx = 0; guestNameField = compactField(""); form.add(guestNameField, g);
g.gridx = 1; guestEmailField = compactField(""); form.add(guestEmailField, g);
g.gridy++;
g.gridx = 0; form.add(label("Room Type"), g);
g.gridx = 1; form.add(label("Number of guests"), g);
g.gridy++;
g.gridx = 0;
List roomNameList = new ArrayList<>();
for (Room r : rooms) roomNameList.add(r.name);
roomChoice = new JComboBox<>(new Vector<>(roomNameList));
styleCombo(roomChoice); form.add(roomChoice, g);
g.gridx = 1;
guestsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 8, 1));
guestsSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
form.add(guestsSpinner, g);
g.gridy++;
g.gridx = 0; form.add(label("Check-in date (YYYY-MM-DD)"), g);
g.gridx = 1; form.add(label("Check-out date (YYYY-MM-DD)"), g);
g.gridy++;
g.gridx = 0; checkInField = compactField(LocalDate.now().plusDays(1).toString()); form.add(checkInField, g);
g.gridx = 1; checkOutField = compactField(LocalDate.now().plusDays(3).toString()); form.add(checkOutField, g);
g.gridy++; g.gridx = 0; g.gridwidth = 2;
roomDescription = new JTextArea(2, 42);
roomDescription.setLineWrap(true); roomDescription.setWrapStyleWord(true); roomDescription.setEditable(false);
roomDescription.setBackground(CREAM); roomDescription.setFont(new Font("Monospaced", Font.PLAIN, 12));
roomDescription.setBorder(new EmptyBorder(6, 10, 6, 10));
roomDescription.setText(rooms.get(0).description);
form.add(roomDescription, g);
g.gridy++;
totalLabel = new JLabel("Estimated total: R 0.00");
totalLabel.setForeground(WINE); totalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
form.add(totalLabel, g);
g.gridy++;
JButton reserveBtn = wineButton("CONFIRM RESERVATION");
form.add(reserveBtn, g);
roomChoice.addActionListener(e -> updateEstimate());
checkInField.addActionListener(e -> updateEstimate());
checkOutField.addActionListener(e -> updateEstimate());
guestsSpinner.addChangeListener(e -> updateEstimate());
reserveBtn.addActionListener(e -> processBooking());
updateEstimate();
p.add(form);
return p;
}
private JPanel extrasPanel() {
JPanel p = new JPanel(new BorderLayout(14, 14));
p.setBackground(CREAM); p.setBorder(new EmptyBorder(20, 25, 20, 25));
JPanel topBar = new JPanel(new BorderLayout(12, 0));
topBar.setBackground(CREAM);
topBar.add(sectionTitle("EXTRAS, FOOD & AMENITIES"), BorderLayout.WEST);
JPanel selectWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
selectWrap.setBackground(CREAM);
selectWrap.add(label("Select Guest Booking:"));
orderBookingChoice = new JComboBox<>();
styleCombo(orderBookingChoice);
orderBookingChoice.setPreferredSize(new Dimension(350, 34));
orderBookingChoice.addActionListener(e -> updateOrderSummary());
selectWrap.add(orderBookingChoice);
topBar.add(selectWrap, BorderLayout.EAST);
p.add(topBar, BorderLayout.NORTH);
JPanel menuList = new JPanel(new GridLayout(0, 1, 10, 10)); menuList.setBackground(CREAM);
for (ExtraItem item : extraMenu) {
JPanel card = new JPanel(new BorderLayout(12, 6)); card.setBackground(Color.WHITE);
card.setBorder(new CompoundBorder(new LineBorder(new Color(220, 210, 185), 1, true), new EmptyBorder(12, 16, 12, 16)));
JLabel cat = new JLabel(item.category.toUpperCase());
cat.setFont(new Font("SansSerif", Font.BOLD, 10)); cat.setForeground(new Color(145, 105, 35));
JLabel name = new JLabel(item.name + "  —  R " + money(item.price));
name.setFont(new Font("Serif", Font.BOLD, 18)); name.setForeground(WINE);
JLabel desc = new JLabel(item.description);
desc.setFont(new Font("SansSerif", Font.PLAIN, 12)); desc.setForeground(INK);
JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
info.setOpaque(false); info.add(cat); info.add(name); info.add(desc);
JSpinner qtySpin = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
JButton addBtn = goldButton("ADD TO BOOKING");
addBtn.addActionListener(e -> addExtraToSelectedBooking(item, (Integer) qtySpin.getValue()));
JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
rightControls.setOpaque(false); rightControls.add(label("Qty:")); rightControls.add(qtySpin); rightControls.add(addBtn);
card.add(info, BorderLayout.CENTER); card.add(rightControls, BorderLayout.EAST);
menuList.add(card);
}
JScrollPane menuScroll = new JScrollPane(menuList);
menuScroll.setBorder(null); menuScroll.getVerticalScrollBar().setUnitIncrement(16);
JPanel receiptCard = new JPanel(new BorderLayout(8, 8));
receiptCard.setBackground(Color.WHITE);
receiptCard.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(16, 18, 16, 18)));
receiptCard.setPreferredSize(new Dimension(360, 0));
JLabel recTitle = new JLabel("BOOKING BILL & EXTRAS");
recTitle.setFont(new Font("Serif", Font.BOLD, 18)); recTitle.setForeground(WINE);
receiptCard.add(recTitle, BorderLayout.NORTH);
orderSummaryArea = new JTextArea();
orderSummaryArea.setEditable(false); orderSummaryArea.setLineWrap(true); orderSummaryArea.setWrapStyleWord(true);
orderSummaryArea.setBackground(CREAM); orderSummaryArea.setForeground(INK);
orderSummaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
orderSummaryArea.setBorder(new EmptyBorder(12, 12, 12, 12));
receiptCard.add(new JScrollPane(orderSummaryArea), BorderLayout.CENTER);
JPanel receiptButtons = new JPanel(new GridLayout(1, 2, 8, 0));
receiptButtons.setOpaque(false);
JButton printSlipBtn = goldButton("PRINT SLIP");
printSlipBtn.addActionListener(e -> printSelectedBookingSlip());
JButton clearExtrasBtn = wineButton("CLEAR EXTRAS");
clearExtrasBtn.addActionListener(e -> clearExtrasOnSelectedBooking());
receiptButtons.add(printSlipBtn); receiptButtons.add(clearExtrasBtn);
receiptCard.add(receiptButtons, BorderLayout.SOUTH);
p.add(menuScroll, BorderLayout.CENTER);
p.add(receiptCard, BorderLayout.EAST);
refreshOrderBookingDropdown();
return p;
}
private void printSelectedBookingSlip() {
if (bookings.isEmpty() || orderBookingChoice == null || orderBookingChoice.getSelectedIndex() < 0) {
message("Please select a guest booking first to print a slip.");
return;
}
Booking b = bookings.get(orderBookingChoice.getSelectedIndex());
double roomOnly = b.total - b.extrasTotal;
String printedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
StringBuilder slipText = new StringBuilder();
slipText.append("================================================\n");
slipText.append("          IMPERIAL SOLIS HOTEL & RESORT         \n");
slipText.append("             OFFICIAL GUEST BILL SLIP           \n");
slipText.append("================================================\n");
slipText.append("Printed At   : ").append(printedAt).append("\n");
slipText.append("Issued By    : ").append(currentUser).append("\n");
slipText.append("Reference #  : ").append(b.id).append("\n");
slipText.append("Status       : ").append(b.status).append("\n");
slipText.append("------------------------------------------------\n");
slipText.append("Guest Name   : ").append(b.guest).append("\n");
slipText.append("Guest Email  : ").append(b.email.isEmpty() ? "N/A" : b.email).append("\n");
slipText.append("Room Type    : ").append(b.room).append("\n");
slipText.append("Check-in     : ").append(b.in).append("\n");
slipText.append("Check-out    : ").append(b.out).append("\n");
slipText.append("Guests       : ").append(b.guests).append("\n");
slipText.append("Payment Mode : ").append(b.payment).append("\n");
slipText.append("------------------------------------------------\n");
slipText.append("ORDERED EXTRAS & AMENITIES:\n");
if (b.extrasSummary == null || b.extrasSummary.isEmpty()) {
slipText.append("  • No extras ordered\n");
} else {
for (String itemLine : b.extrasSummary.split("; ")) {
slipText.append("  • ").append(itemLine).append("\n");
}
}
slipText.append("------------------------------------------------\n");
slipText.append(String.format("Room Charges : R %s%n", money(roomOnly)));
slipText.append(String.format("Extras Total : R %s%n", money(b.extrasTotal)));
if ("Cancelled".equalsIgnoreCase(b.status)) {
slipText.append(String.format("Cancel Fee   : R %s%n", money(b.cancellationFee)));
}
slipText.append("================================================\n");
slipText.append(String.format("GRAND TOTAL  : R %s%n", money(b.total)));
slipText.append("================================================\n");
JTextArea previewArea = new JTextArea(slipText.toString(), 24, 50);
previewArea.setEditable(false); previewArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
previewArea.setBackground(CREAM); previewArea.setForeground(INK);
previewArea.setBorder(new EmptyBorder(14, 16, 14, 16));
Object[] options = {"SEND TO PRINTER", "SAVE SLIP (.TXT)", "CLOSE"};
int choice = JOptionPane.showOptionDialog(this, new JScrollPane(previewArea), "Print Slip — Ref: " + b.id,
JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options);
if (choice == 0) {
try { if (previewArea.print()) message("Slip sent to printer successfully."); }
catch (Exception ex) { message("Could not open printer: " + ex.getMessage()); }
} else if (choice == 1) {
try {
Files.createDirectories(DATA);
Path slipFile = DATA.resolve("Slip_" + b.id + ".txt");
Files.writeString(slipFile, slipText.toString());
message("Slip saved to:\n" + slipFile.toAbsolutePath());
} catch (IOException ex) { message("Could not save slip file: " + ex.getMessage()); }
}
}
private void refreshOrderBookingDropdown() {
if (orderBookingChoice == null) return;
int prevIdx = orderBookingChoice.getSelectedIndex();
orderBookingChoice.removeAllItems();
if (bookings.isEmpty()) {
orderBookingChoice.addItem("No bookings available — book a room first");
} else {
for (Booking b : bookings) {
orderBookingChoice.addItem(b.id + " - " + b.guest + " [" + b.status + "]");
}
orderBookingChoice.setSelectedIndex((prevIdx >= 0 && prevIdx < bookings.size()) ? prevIdx : 0);
}
updateOrderSummary();
}
private void updateOrderSummary() {
if (orderSummaryArea == null) return;
if (bookings.isEmpty() || orderBookingChoice == null || orderBookingChoice.getSelectedIndex() < 0) {
orderSummaryArea.setText("No active booking selected.");
return;
}
Booking b = bookings.get(orderBookingChoice.getSelectedIndex());
double roomOnly = b.total - b.extrasTotal;
String extrasDisplay = (b.extrasSummary == null || b.extrasSummary.isEmpty())
? "  None added yet"
: "  • " + b.extrasSummary.replace("; ", "\n  • ");
orderSummaryArea.setText(
"Reference : " + b.id + "\n" +
"Status    : " + b.status + "\n" +
"Guest     : " + b.guest + "\n" +
"Email     : " + b.email + "\n" +
"Room      : " + b.room + "\n" +
"Payment   : " + b.payment + "\n" +
"Dates     : " + b.in + " to " + b.out + "\n" +
"------------------------------------\n" +
"Room Base : R " + money(roomOnly) + "\n" +
"Extras    : R " + money(b.extrasTotal) + "\n" +
"Cancel Fee: R " + money(b.cancellationFee) + "\n" +
"------------------------------------\n" +
"GRAND TOT : R " + money(b.total) + "\n\n" +
"Ordered Items:\n" + extrasDisplay
);
}
private void addExtraToSelectedBooking(ExtraItem item, int qty) {
if (bookings.isEmpty() || orderBookingChoice == null || orderBookingChoice.getSelectedIndex() < 0) {
message("Please create a room booking first before adding extras.");
return;
}
Booking b = bookings.get(orderBookingChoice.getSelectedIndex());
if ("Cancelled".equalsIgnoreCase(b.status)) {
message("Cannot add extras to a cancelled reservation.");
return;
}
double addedCost = item.price * qty;
b.extrasTotal += addedCost;
b.total += addedCost;
String entry = qty + "x " + item.name + " (R " + money(addedCost) + ")";
b.extrasSummary = (b.extrasSummary == null || b.extrasSummary.isEmpty()) ? entry : b.extrasSummary + "; " + entry;
updateBookingInDB(b);
refreshBookingList();
updateOrderSummary();
message("Added " + entry + " to " + b.guest + ".\nUpdated Total: R " + money(b.total));
}
private void clearExtrasOnSelectedBooking() {
if (bookings.isEmpty() || orderBookingChoice == null || orderBookingChoice.getSelectedIndex() < 0) return;
Booking b = bookings.get(orderBookingChoice.getSelectedIndex());
if (b.extrasTotal <= 0) {
message("This booking has no extras to clear.");
return;
}
int confirm = JOptionPane.showConfirmDialog(this, "Remove all added food, drinks & amenities from booking " + b.id + "?", "Clear Extras", JOptionPane.YES_NO_OPTION);
if (confirm == JOptionPane.YES_OPTION) {
b.total -= b.extrasTotal;
b.extrasTotal = 0;
b.extrasSummary = "";
updateBookingInDB(b);
refreshBookingList();
updateOrderSummary();
}
}
private void updateEstimate() {
if (roomChoice == null || totalLabel == null) return;
Room r = rooms.get(roomChoice.getSelectedIndex());
try {
LocalDate in = LocalDate.parse(checkInField.getText().trim());
LocalDate out = LocalDate.parse(checkOutField.getText().trim());
long nights = ChronoUnit.DAYS.between(in, out);
if (roomDescription != null) roomDescription.setText(r.description);
totalLabel.setText(nights > 0 ? "Estimated total: R " + money(nights * r.price) + " (" + nights + " nights)" : "Check your dates");
} catch (Exception ex) { totalLabel.setText("Enter valid dates (YYYY-MM-DD)"); }
}
private void processBooking() {
String enteredGuest = guestNameField.getText().trim();
String enteredEmail = guestEmailField.getText().trim();
if (enteredGuest.isEmpty() || enteredEmail.isEmpty() || !enteredEmail.contains("@")) {
message("Please enter both Guest Name and a valid Guest Email.");
return;
}
try {
Room r = rooms.get(roomChoice.getSelectedIndex());
LocalDate in = LocalDate.parse(checkInField.getText().trim());
LocalDate out = LocalDate.parse(checkOutField.getText().trim());
int guests = (Integer) guestsSpinner.getValue();
long nights = ChronoUnit.DAYS.between(in, out);
if (!out.isAfter(in) || in.isBefore(LocalDate.now())) {
message("Please choose valid future check-in and check-out dates.");
return;
}
if (guests > r.capacity) {
message(r.name + " accommodates up to " + r.capacity + " guests.");
return;
}
double total = nights * r.price;
String defaultPaymentStatus = "Pending (Guest Portal)";
lastBooking = new Booking(
UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
enteredGuest + " (Admin: " + currentUser + ")",
enteredEmail, r.name, r.price, in.toString(), out.toString(),
guests, total, defaultPaymentStatus, 0.0, "", "Active", 0.0, LocalDateTime.now()
);
insertBookingToDB(lastBooking);
bookings.add(0, lastBooking);
refreshBookingList();
refreshOrderBookingDropdown();
guestNameField.setText("");
guestEmailField.setText("");
showConfirmation(lastBooking);
} catch (Exception ex) {
message("Please enter dates in YYYY-MM-DD format.");
}
}
private void showConfirmation(Booking b) {
JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(WINE);
JPanel details = new JPanel(); details.setBackground(WINE); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
details.setBorder(new EmptyBorder(25, 35, 25, 35));
JLabel check = new JLabel("✓", SwingConstants.CENTER); check.setForeground(GOLD); check.setFont(new Font("SansSerif", Font.BOLD, 54)); check.setAlignmentX(.5f);
JLabel title = new JLabel("RESERVATION CONFIRMED", SwingConstants.CENTER); title.setForeground(GOLD); title.setFont(new Font("Serif", Font.BOLD, 25)); title.setAlignmentX(.5f);
JTextArea info = new JTextArea(
"Booking Reference : " + b.id + "\n" +
"Guest Name        : " + b.guest + "\n" +
"Guest Email       : " + b.email + "\n" +
"Room              : " + b.room + "\n" +
"Check-in          : " + b.in + "\n" +
"Check-out         : " + b.out + "\n" +
"Payment Status    : " + b.payment + "\n" +
"Room Total        : R " + money(b.total) + "\n\n" +
"Share Reference #" + b.id + " with the guest so they can log into\n" +
"guest.html to add dining/activities and select their payment method."
);
info.setEditable(false); info.setBackground(new Color(92, 18, 43)); info.setForeground(CREAM); info.setFont(new Font("Monospaced", Font.PLAIN, 14)); info.setBorder(new EmptyBorder(18, 18, 18, 18));
JButton home = goldButton("BACK TO DASHBOARD"); home.setAlignmentX(.5f); home.addActionListener(e -> showDashboard());
details.add(check); details.add(title); details.add(Box.createVerticalStrut(20)); details.add(info); details.add(Box.createVerticalStrut(20)); details.add(home);
panel.add(details); setContentPane(centered(panel)); revalidate(); repaint();
}
private JPanel bookingsPanel() {
JPanel p = new JPanel(new BorderLayout(10, 10));
p.setBackground(CREAM); p.setBorder(new EmptyBorder(22, 25, 22, 25));
JPanel topBar = new JPanel(new BorderLayout(15, 0)); topBar.setBackground(CREAM);
topBar.add(sectionTitle("BOOKINGS & GUEST PORTAL SYNC"), BorderLayout.WEST);
JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); searchPanel.setBackground(CREAM);
searchPanel.add(label("Search (Name, Email or Ref #):"));
searchField = compactField("");
searchField.getDocument().addDocumentListener(new DocumentListener() {
public void insertUpdate(DocumentEvent e) { refreshBookingList(); }
public void removeUpdate(DocumentEvent e) { refreshBookingList(); }
public void changedUpdate(DocumentEvent e) { refreshBookingList(); }
});
searchPanel.add(searchField);
topBar.add(searchPanel, BorderLayout.EAST);
p.add(topBar, BorderLayout.NORTH);
bookingList = new JList<>(bookingListModel);
bookingList.setFont(new Font("SansSerif", Font.PLAIN, 13));
refreshBookingList();
p.add(new JScrollPane(bookingList), BorderLayout.CENTER);
JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actions.setBackground(CREAM);
JButton addExtrasShortcut = goldButton("ORDER FOOD / EXTRAS");
addExtrasShortcut.addActionListener(e -> {
int idx = bookingList.getSelectedIndex();
if (idx >= 0 && idx < displayedBookings.size() && orderBookingChoice != null) {
int fullIdx = bookings.indexOf(displayedBookings.get(idx));
if (fullIdx >= 0) orderBookingChoice.setSelectedIndex(fullIdx);
}
if (tabs != null) tabs.setSelectedIndex(4);
});
JButton refresh = goldButton("SYNC WEBSITE & PORTAL");
refresh.addActionListener(e -> {
loadBookings();
refreshBookingList();
refreshOrderBookingDropdown();
message("Synced with hotel_db (Website & Guest Portal updates loaded).");
});
JButton cancelBtn = wineButton("CANCEL (5-DAY POLICY)");
cancelBtn.addActionListener(e -> cancelSelectedBooking());
actions.add(addExtrasShortcut); actions.add(refresh); actions.add(cancelBtn);
p.add(actions, BorderLayout.SOUTH);
return p;
}
private void cancelSelectedBooking() {
int idx = bookingList.getSelectedIndex();
if (idx < 0 || displayedBookings.isEmpty()) {
message("Please select a booking from the list to cancel.");
return;
}
Booking selected = displayedBookings.get(idx);
if ("Cancelled".equalsIgnoreCase(selected.status)) {
message("Booking " + selected.id + " is already cancelled.");
return;
}
long daysElapsed = ChronoUnit.DAYS.between(selected.createdAt.toLocalDate(), LocalDate.now());
double fee = (daysElapsed <= 5) ? 0.0 : Math.round(selected.total * 0.25 * 100.0) / 100.0;
String policyMsg = (fee == 0)
? "Registered " + daysElapsed + " day(s) ago (within 5 days). Cancel FREE of charge?"
: "Registered " + daysElapsed + " day(s) ago (after 5 days). Apply 25% cancellation fee of R " + money(fee) + "?";
if (JOptionPane.showConfirmDialog(this, policyMsg, "Cancel Booking", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
String sql = "UPDATE bookings SET status = 'Cancelled', cancellation_fee = ?, cancelled_at = NOW() WHERE booking_id = ?";
try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setDouble(1, fee);
ps.setString(2, selected.id);
ps.executeUpdate();
selected.status = "Cancelled";
selected.cancellationFee = fee;
refreshBookingList();
refreshOrderBookingDropdown();
message("Booking " + selected.id + " marked as Cancelled. Fee applied: R " + money(fee));
} catch (SQLException ex) {
message("Database cancel error: " + ex.getMessage());
}
}
}
private JPanel reviewsPanel() {
JPanel p = new JPanel(new BorderLayout(12, 12));
p.setBackground(CREAM); p.setBorder(new EmptyBorder(22, 28, 22, 28));
JPanel top = new JPanel(new BorderLayout()); top.setBackground(CREAM);
top.add(sectionTitle("GUEST REVIEWS & RATINGS (FROM WEBSITE)"), BorderLayout.WEST);
JPanel list = new JPanel(); list.setBackground(CREAM);
list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
Runnable loadReviews = () -> {
list.removeAll();
String sql = "SELECT guest_name, guest_email, rating, review_text, created_at FROM reviews ORDER BY created_at DESC LIMIT 40";
try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
boolean hasAny = false;
while (rs.next()) {
hasAny = true;
int stars = rs.getInt("rating");
String title = "★".repeat(Math.max(1, stars)) + "  " + rs.getString("guest_name") + " (" + rs.getString("guest_email") + ")  —  " + rs.getString("created_at");
list.add(notification(title, rs.getString("review_text")));
list.add(Box.createVerticalStrut(10));
}
if (!hasAny) {
list.add(notification("No Guest Reviews Yet", "Reviews submitted under 'Share Your Experience' on the website will appear here."));
}
} catch (SQLException ex) {
list.add(notification("Database Error", ex.getMessage()));
}
list.revalidate(); list.repaint();
};
JButton refreshBtn = goldButton("REFRESH REVIEWS");
refreshBtn.addActionListener(e -> loadReviews.run());
top.add(refreshBtn, BorderLayout.EAST);
loadReviews.run();
p.add(top, BorderLayout.NORTH);
JScrollPane scroll = new JScrollPane(list); scroll.setBorder(null);
p.add(scroll, BorderLayout.CENTER);
return p;
}
private JPanel notificationsPanel() {
JPanel p = new JPanel(new BorderLayout(12, 12));
p.setBackground(CREAM); p.setBorder(new EmptyBorder(22, 28, 22, 28));
JPanel top = new JPanel(new BorderLayout(12, 0));
top.setBackground(CREAM);
top.add(sectionTitle("LIVE GUEST ALERTS (BY GMAIL & REF #)"), BorderLayout.WEST);
JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
rightControls.setBackground(CREAM);
rightControls.add(label("Search (Gmail, Ref # or Name):"));
alertSearchField = compactField("");
rightControls.add(alertSearchField);
JButton refreshBtn = goldButton("REFRESH NOW");
rightControls.add(refreshBtn);
top.add(rightControls, BorderLayout.EAST);
JPanel list = new JPanel();
list.setBackground(CREAM);
list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
Runnable loadNotifs = () -> {
list.removeAll();
String query = (alertSearchField != null) ? alertSearchField.getText().trim().toLowerCase() : "";
String sql = "SELECT category, title, body, sender_email, created_at FROM notifications ORDER BY created_at ASC";
try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
Map<String, List<String[]>> topicItems = new LinkedHashMap<>();
Map<String, Set> topicRefs = new LinkedHashMap<>();
Map<String, String> topicEmails = new LinkedHashMap<>();
List latestTopicOrder = new ArrayList<>();
java.util.regex.Pattern refPattern = java.util.regex.Pattern.compile("#([A-Z0-9]{6,12})");
while (rs.next()) {
String cat    = rs.getString("category").toUpperCase();
String title  = rs.getString("title");
String body   = rs.getString("body");
String sender = rs.getString("sender_email");
String time   = rs.getString("created_at");
java.util.regex.Matcher m = refPattern.matcher(title + " " + body);
String refCode = m.find() ? "#" + m.group(1) : null;
String cleanEmail = (sender != null && !sender.trim().isEmpty()) ? sender.trim().toLowerCase() : "";
String topicKey = !cleanEmail.isEmpty() ? cleanEmail : (refCode != null ? refCode : "general");
if (!cleanEmail.isEmpty()) topicEmails.put(topicKey, sender.trim());
if (refCode != null) topicRefs.computeIfAbsent(topicKey, k -> new LinkedHashSet<>()).add(refCode);
topicItems.computeIfAbsent(topicKey, k -> new ArrayList<>())
.add(new String[]{time, cat, title, body});
latestTopicOrder.remove(topicKey);
latestTopicOrder.add(topicKey);
}
if (topicItems.isEmpty()) {
list.add(notification("No Guest Topics Yet", "Bookings, Guest Portal logins, payment updates, and cancellations will appear under each guest's Gmail & Reference #."));
} else {
Collections.reverse(latestTopicOrder);
int shownCount = 0;
for (Object key : latestTopicOrder) {
String emailDisplay = topicEmails.getOrDefault(key, "");
Set refs = topicRefs.getOrDefault(key, Collections.emptySet());
String refDisplay = refs.isEmpty() ? "" : String.join(", ", refs);
List<String[]> events = topicItems.get(key);
boolean matches = query.isEmpty()
|| emailDisplay.toLowerCase().contains(query)
|| refDisplay.toLowerCase().contains(query);
if (!matches) {
for (String[] ev : events) {
if (ev[1].toLowerCase().contains(query)
|| ev[2].toLowerCase().contains(query)
|| ev[3].toLowerCase().contains(query)) {
matches = true;
break;
}
}
}
if (!matches) continue;
shownCount++;
String mainTopicTitle;
if (!emailDisplay.isEmpty() && !refDisplay.isEmpty()) {
mainTopicTitle = emailDisplay + "   |   Ref: " + refDisplay;
} else if (!emailDisplay.isEmpty()) {
mainTopicTitle = emailDisplay;
} else if (!refDisplay.isEmpty()) {
mainTopicTitle = "Booking Ref: " + refDisplay;
} else {
mainTopicTitle = "General Hotel Notices";
}
list.add(groupedNotificationCard(mainTopicTitle, events));
list.add(Box.createVerticalStrut(14));
}
if (shownCount == 0 && !query.isEmpty()) {
list.add(notification("No Matching Alerts", "No guest alerts found matching '" + alertSearchField.getText().trim() + "'."));
}
}
} catch (SQLException ex) {
list.add(notification("Database Error", ex.getMessage()));
}
list.revalidate();
list.repaint();
};
alertSearchField.getDocument().addDocumentListener(new DocumentListener() {
public void insertUpdate(DocumentEvent e) { loadNotifs.run(); }
public void removeUpdate(DocumentEvent e) { loadNotifs.run(); }
public void changedUpdate(DocumentEvent e) { loadNotifs.run(); }
});
refreshBtn.addActionListener(e -> loadNotifs.run());
loadNotifs.run();
if (alertAutoRefreshTimer != null && alertAutoRefreshTimer.isRunning()) {
alertAutoRefreshTimer.stop();
}
alertAutoRefreshTimer = new javax.swing.Timer(10000, e -> {
if (isDisplayable()) {
loadNotifs.run();
loadBookings();
refreshBookingList();
refreshOrderBookingDropdown();
} else {
((javax.swing.Timer) e.getSource()).stop();
}
});
alertAutoRefreshTimer.start();
p.add(top, BorderLayout.NORTH);
JScrollPane scroll = new JScrollPane(list);
scroll.setBorder(null);
scroll.getVerticalScrollBar().setUnitIncrement(16);
p.add(scroll, BorderLayout.CENTER);
return p;
}
private JPanel groupedNotificationCard(String mainTopic, List<String[]> items) {
JPanel card = new JPanel(new BorderLayout(0, 10));
card.setBackground(Color.WHITE);
card.setBorder(new CompoundBorder(new LineBorder(GOLD, 2, true), new EmptyBorder(14, 18, 16, 18)));
JPanel topicHeader = new JPanel(new BorderLayout());
topicHeader.setBackground(WINE);
topicHeader.setBorder(new EmptyBorder(10, 14, 10, 14));
JLabel topicLbl = new JLabel("✦  " + mainTopic);
topicLbl.setForeground(GOLD);
topicLbl.setFont(new Font("Serif", Font.BOLD, 16));
JLabel countLbl = new JLabel(items.size() + (items.size() == 1 ? " Notification" : " Notifications"));
countLbl.setForeground(CREAM);
countLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
topicHeader.add(topicLbl, BorderLayout.WEST);
topicHeader.add(countLbl, BorderLayout.EAST);
JPanel subList = new JPanel();
subList.setBackground(Color.WHITE);
subList.setLayout(new BoxLayout(subList, BoxLayout.Y_AXIS));
for (int i = 0; i < items.size(); i++) {
String[] ev = items.get(i); // [0]=time, [1]=category, [2]=title, [3]=body
JPanel row = new JPanel(new BorderLayout(6, 3));
row.setBackground(CREAM);
row.setBorder(new CompoundBorder(
new MatteBorder(0, 4, 0, 0, GOLD),
new EmptyBorder(8, 12, 8, 12)
));
JLabel rowTop = new JLabel((i + 1) + ".  [" + ev[0] + "]   " + ev[1] + " — " + ev[2]);
rowTop.setForeground(WINE);
rowTop.setFont(new Font("SansSerif", Font.BOLD, 12));
JTextArea rowBody = new JTextArea(ev[3]);
rowBody.setLineWrap(true);
rowBody.setWrapStyleWord(true);
rowBody.setEditable(false);
rowBody.setOpaque(false);
rowBody.setForeground(INK);
rowBody.setFont(new Font("SansSerif", Font.PLAIN, 12));
row.add(rowTop, BorderLayout.NORTH);
row.add(rowBody, BorderLayout.CENTER);
subList.add(row);
if (i < items.size() - 1) {
subList.add(Box.createVerticalStrut(6));
}
}
card.add(topicHeader, BorderLayout.NORTH);
card.add(subList, BorderLayout.CENTER);
card.setMaximumSize(new Dimension(1040, 75 + (items.size() * 68)));
return card;
}
private JPanel notification(String title, String body) {
List<String[]> single = new ArrayList<>();
single.add(new String[]{"INFO", "NOTICE", title, body});
return groupedNotificationCard(title, single);
}
private void refreshBookingList() {
bookingListModel.clear();
displayedBookings.clear();
String query = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";
for (Booking b : bookings) {
boolean matches = query.isEmpty() || b.id.toLowerCase().contains(query) || b.guest.toLowerCase().contains(query) || b.email.toLowerCase().contains(query);
if (matches) {
displayedBookings.add(b);
String statusTag = "Cancelled".equalsIgnoreCase(b.status)
? "[CANCELLED | Fee: R " + money(b.cancellationFee) + "]"
: "[ACTIVE]";
String extrasTag = (b.extrasSummary != null && !b.extrasSummary.isEmpty()) ? "  |  Extras: [" + b.extrasSummary + "]" : "";
bookingListModel.addElement(
b.id + " " + statusTag + "  |  " + b.guest + " (" + b.email + ")  |  " + b.room + "  |  Pay: " + b.payment + extrasTag + "  |  Total: R " + money(b.total)
);
}
}
if (bookingListModel.isEmpty()) {
bookingListModel.addElement(query.isEmpty() ? "No bookings yet." : "No bookings matching '" + searchField.getText().trim() + "'.");
}
}
private void loadBookings() {
bookings.clear();
String sql = "SELECT * FROM bookings ORDER BY created_at DESC";
try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
while (rs.next()) {
Timestamp ts = rs.getTimestamp("created_at");
bookings.add(new Booking(
rs.getString("booking_id"),
rs.getString("guest_name"),
rs.getString("guest_email"),
rs.getString("room_name"),
rs.getDouble("room_rate"),
rs.getString("check_in"),
rs.getString("check_out"),
rs.getInt("guests"),
rs.getDouble("total_amount"),
rs.getString("payment_method") == null ? "Pending (Guest Portal)" : rs.getString("payment_method"),
rs.getDouble("extras_total"),
rs.getString("extras_summary") == null ? "" : rs.getString("extras_summary"),
rs.getString("status") == null ? "Active" : rs.getString("status"),
rs.getDouble("cancellation_fee"),
ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
));
}
} catch (SQLException ex) {
message("Could not load bookings from MySQL: " + ex.getMessage());
}
}
private void insertBookingToDB(Booking b) {
String sql = "INSERT INTO bookings (booking_id, guest_name, guest_email, room_name, room_rate, check_in, check_out, guests, total_amount, payment_method, extras_total, extras_summary, status, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Active', 'AdminApp')";
try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setString(1, b.id);
ps.setString(2, b.guest);
ps.setString(3, b.email);
ps.setString(4, b.room);
ps.setDouble(5, b.roomRate);
ps.setString(6, b.in);
ps.setString(7, b.out);
ps.setInt(8, b.guests);
ps.setDouble(9, b.total);
ps.setString(10, b.payment);
ps.setDouble(11, b.extrasTotal);
ps.setString(12, b.extrasSummary);
ps.executeUpdate();
} catch (SQLException ex) {
message("Database save error: " + ex.getMessage());
}
}
private void updateBookingInDB(Booking b) {
String sql = "UPDATE bookings SET total_amount = ?, extras_total = ?, extras_summary = ? WHERE booking_id = ?";
try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setDouble(1, b.total);
ps.setDouble(2, b.extrasTotal);
ps.setString(3, b.extrasSummary);
ps.setString(4, b.id);
ps.executeUpdate();
} catch (SQLException ex) {
message("Database update error: " + ex.getMessage());
}
}
private JPanel centered(JComponent c) { JPanel p = new JPanel(new GridBagLayout()); p.setBackground(CREAM); p.add(c); return p; }
private JLabel sectionTitle(String s) { JLabel l = new JLabel(s); l.setForeground(WINE); l.setFont(new Font("Serif", Font.BOLD, 23)); return l; }
private JLabel label(String s) { JLabel l = new JLabel(s); l.setForeground(INK); l.setFont(new Font("SansSerif", Font.BOLD, 13)); return l; }
private JTextField field(String hint) { JTextField f = new JTextField(22); f.setText(hint); styleField(f); return f; }
private JTextField compactField(String hint) { JTextField f = new JTextField(16); f.setText(hint); styleField(f); return f; }
private void styleField(JTextField f) { f.setFont(new Font("SansSerif", Font.PLAIN, 14)); f.setBackground(CREAM); f.setForeground(INK); f.setBorder(new CompoundBorder(new LineBorder(new Color(210, 198, 170), 1, true), new EmptyBorder(7, 10, 7, 10))); }
private void styleCombo(JComboBox<?> c) { c.setFont(new Font("SansSerif", Font.PLAIN, 14)); c.setBackground(CREAM); }
private JButton wineButton(String s) {
JButton b = new JButton(s);
b.setBackground(WINE); b.setForeground(Color.WHITE); b.setOpaque(true);
b.setContentAreaFilled(true); b.setBorderPainted(false); b.setFocusPainted(false);
b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setBorder(new EmptyBorder(10, 18, 10, 18));
return b;
}
private JButton goldButton(String s) {
JButton b = new JButton(s);
b.setBackground(GOLD); b.setForeground(WINE); b.setOpaque(true);
b.setContentAreaFilled(true); b.setBorderPainted(false); b.setFocusPainted(false);
b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setBorder(new EmptyBorder(10, 18, 10, 18));
return b;
}
private void message(String s) { JOptionPane.showMessageDialog(this, s, "Imperial Solis Hotel", JOptionPane.INFORMATION_MESSAGE); }
private String money(double n) { return String.format("%,.2f", n); }
private static class Room {
String name, description; double price; int capacity;
Room(String n, double p, int c, String d) { name = n; price = p; capacity = c; description = d; }
}
private static class SpecialOffer {
String title, badge, priceLabel, description; int defaultRoomIndex;
SpecialOffer(String title, String badge, String priceLabel, String description, int defaultRoomIndex) {
this.title = title; this.badge = badge; this.priceLabel = priceLabel;
this.description = description; this.defaultRoomIndex = defaultRoomIndex;
}
}
private static class ExtraItem {
String category, name, description; double price;
ExtraItem(String category, String name, double price, String description) {
this.category = category; this.name = name; this.price = price; this.description = description;
}
}
private static class Booking {
String id, guest, email, room, in, out, payment, extrasSummary, status;
int guests; double roomRate, total, extrasTotal, cancellationFee; LocalDateTime createdAt;
Booking(String id, String guest, String email, String room, double roomRate, String in, String out, int guests,
double total, String payment, double extrasTotal, String extrasSummary, String status, double cancellationFee, LocalDateTime createdAt) {
this.id = id; this.guest = guest; this.email = email; this.room = room; this.roomRate = roomRate;
this.in = in; this.out = out; this.guests = guests; this.total = total; this.payment = payment;
this.extrasTotal = extrasTotal; this.extrasSummary = extrasSummary; this.status = status;
this.cancellationFee = cancellationFee; this.createdAt = createdAt;
}
}
public static void main(String[] args) {
SwingUtilities.invokeLater(() -> {
try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
new AP().setVisible(true);
});
}
}