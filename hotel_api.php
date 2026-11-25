<?php
// hotel_api.php — Unified Backend API for Imperial Solis Hotel
require_once __DIR__ . '/hotel_security.php';

$action = $_POST['action'] ?? '';
checkRateLimit($action ?: 'general');

try {
    $pdo = getHotelDB();

    // Helper: Send Email to lifecyclem4@gmail.com
    function notifyHotelGmail(string $subject, string $message): void {
        $to = 'lifecyclem4@gmail.com';
        $headers = "From: Imperial Solis Hotel <lifecyclem4@gmail.com>\r\n" .
                   "Reply-To: lifecyclem4@gmail.com\r\n" .
                   "X-Mailer: PHP/" . phpversion();
        @mail($to, $subject, $message, $headers);
    }

    // Exact Room Nightly Rates Synced with index.html & AP.java
    $roomRates = [
        'Available in premium' => 300,
        'Kiddies Room (Add-on)' => 300,
        'Garden View Room'     => 780,
        'Deluxe King Room'     => 820,
        'Imperial Suite'       => 850,
        'Executive Twin Room'  => 980,
        'Solis Grand Room'     => 1250,
        'Penthouse Suite'      => 1400,
        'Royal Ocean Villa'    => 1650,
        'Presidential Suite'   => 1900,
        'Presidential Loft'    => 1900
    ];

    // 1. BOOK ROOM FROM index.html
    if ($action === 'book_room') {
        $name     = cleanInput($_POST['name'] ?? '');
        $email    = filter_var(trim($_POST['email'] ?? ''), FILTER_SANITIZE_EMAIL);
        $checkIn  = cleanInput($_POST['check_in'] ?? '');
        $checkOut = cleanInput($_POST['check_out'] ?? '');
        $room     = cleanInput($_POST['room'] ?? '');
        $guests   = max(1, (int)($_POST['guests'] ?? 1));
        $requests = cleanInput($_POST['requests'] ?? '');

        if (!$name || !filter_var($email, FILTER_VALIDATE_EMAIL) || !$checkIn || !$checkOut || !$room) {
            echo json_encode(['status' => 'error', 'message' => 'Please complete all required fields with a valid email.']);
            exit;
        }

        $d1 = new DateTime($checkIn);
        $d2 = new DateTime($checkOut);
        if ($d2 <= $d1) {
            echo json_encode(['status' => 'error', 'message' => 'Check-out date must be after check-in date.']);
            exit;
        }

        $nights = (int)$d1->diff($d2)->days;
        $rate   = $roomRates[$room] ?? 850;
        $total  = $nights * $rate;
        $ref    = strtoupper(substr(bin2hex(random_bytes(4)), 0, 8));

        $stmt = $pdo->prepare("INSERT INTO bookings 
            (booking_id, guest_name, guest_email, room_name, room_rate, check_in, check_out, guests, total_amount, special_requests, source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Website')");
        $stmt->execute([$ref, $name, $email, $room, $rate, $checkIn, $checkOut, $guests, $total, $requests]);

        $notifBody = "New booking #{$ref} by {$name} for {$room} ({$checkIn} to {$checkOut}, {$nights} night(s)). Total: R " . number_format($total, 2);
        $pdo->prepare("INSERT INTO notifications (category, title, body, sender_email) VALUES ('Booking', ?, ?, ?)")
            ->execute(["New Room Booking #{$ref}", $notifBody, $email]);

        notifyHotelGmail("Imperial Solis Hotel Booking #{$ref} - {$name}", $notifBody);

        echo json_encode([
            'status'    => 'success',
            'reference' => $ref,
            'email'     => $email,
            'total'     => number_format($total, 2)
        ]);
        exit;
    }

    // 2. GUEST PORTAL LOGIN (guest.html) -> ALERTS GMAIL & AP.java
    if ($action === 'guest_login') {
        $email = filter_var(trim($_POST['email'] ?? ''), FILTER_SANITIZE_EMAIL);
        $ref   = strtoupper(cleanInput($_POST['reference'] ?? ''));

        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ? AND LOWER(guest_email) = LOWER(?)");
        $stmt->execute([$ref, $email]);
        $booking = $stmt->fetch();

        if (!$booking) {
            echo json_encode(['status' => 'error', 'message' => 'Invalid email or booking reference number.']);
            exit;
        }

        $_SESSION['guest_ref']   = $booking['booking_id'];
        $_SESSION['guest_email'] = $booking['guest_email'];

        $created = new DateTime($booking['created_at']);
        $now     = new DateTime();
        $daysSinceBooking = (int)$created->diff($now)->days;

        $alertTitle = "Guest Portal Login: {$booking['guest_name']} (#{$ref})";
        $alertBody  = "Guest {$booking['guest_name']} ({$email}) logged into the Imperial Solis Hotel Guest Portal for Booking #{$ref} ({$booking['room_name']}).";
        $pdo->prepare("INSERT INTO notifications (category, title, body, sender_email) VALUES ('Guest Login', ?, ?, ?)")
            ->execute([$alertTitle, $alertBody, $email]);

        notifyHotelGmail("Imperial Solis Hotel - {$alertTitle}", $alertBody);

        echo json_encode([
            'status'             => 'success',
            'booking'            => $booking,
            'days_since_booking' => $daysSinceBooking
        ]);
        exit;
    }

    // 3. ADD FOOD OR ACTIVITY FROM GUEST PORTAL
    if ($action === 'add_guest_extra') {
        $ref   = $_SESSION['guest_ref'] ?? strtoupper(cleanInput($_POST['reference'] ?? ''));
        $email = $_SESSION['guest_email'] ?? filter_var(trim($_POST['email'] ?? ''), FILTER_SANITIZE_EMAIL);
        $item  = cleanInput($_POST['item_name'] ?? '');
        $price = (float)($_POST['item_price'] ?? 0);
        $qty   = max(1, (int)($_POST['qty'] ?? 1));

        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ? AND LOWER(guest_email) = LOWER(?) AND status = 'Active'");
        $stmt->execute([$ref, $email]);
        $booking = $stmt->fetch();

        if (!$booking) {
            echo json_encode(['status' => 'error', 'message' => 'Active booking session not found.']);
            exit;
        }

        $addedCost      = $price * $qty;
        $newExtrasTotal = (float)$booking['extras_total'] + $addedCost;
        $newTotal       = (float)$booking['total_amount'] + $addedCost;
        $entry          = "{$qty}x {$item} (R " . number_format($addedCost, 2) . ")";
        $newSummary     = empty($booking['extras_summary']) ? $entry : $booking['extras_summary'] . "; " . $entry;

        $upd = $pdo->prepare("UPDATE bookings SET extras_total = ?, total_amount = ?, extras_summary = ? WHERE booking_id = ?");
        $upd->execute([$newExtrasTotal, $newTotal, $newSummary, $ref]);

        $pdo->prepare("INSERT INTO notifications (category, title, body, sender_email) VALUES ('Booking', ?, ?, ?)")
            ->execute(["Guest Added Extra (#{$ref})", "{$booking['guest_name']} added {$entry}.", $email]);

        echo json_encode([
            'status'         => 'success',
            'extras_total'   => $newExtrasTotal,
            'total_amount'   => $newTotal,
            'extras_summary' => $newSummary
        ]);
        exit;
    }

    // 3B. UPDATE GUEST PAYMENT METHOD
    if ($action === 'update_payment') {
        $ref    = $_SESSION['guest_ref'] ?? strtoupper(cleanInput($_POST['reference'] ?? ''));
        $email  = $_SESSION['guest_email'] ?? filter_var(trim($_POST['email'] ?? ''), FILTER_SANITIZE_EMAIL);
        $method = cleanInput($_POST['payment_method'] ?? 'Pay at Hotel');

        $stmt = $pdo->prepare("SELECT guest_name FROM bookings WHERE booking_id = ? AND LOWER(guest_email) = LOWER(?) AND status = 'Active'");
        $stmt->execute([$ref, $email]);
        $booking = $stmt->fetch();

        if (!$booking) {
            echo json_encode(['status' => 'error', 'message' => 'Active booking not found.']);
            exit;
        }

        $pdo->prepare("UPDATE bookings SET payment_method = ? WHERE booking_id = ?")
            ->execute([$method, $ref]);

        $pdo->prepare("INSERT INTO notifications (category, title, body, sender_email) VALUES ('Booking', ?, ?, ?)")
            ->execute(["Payment Method Selected (#{$ref})", "{$booking['guest_name']} selected payment method: {$method}.", $email]);

        echo json_encode(['status' => 'success', 'payment_method' => $method]);
        exit;
    }

    // 4. CANCEL BOOKING (Free within first 5 days; 25% fee after 5 days)
    if ($action === 'cancel_booking') {
        $ref   = $_SESSION['guest_ref'] ?? strtoupper(cleanInput($_POST['reference'] ?? ''));
        $email = $_SESSION['guest_email'] ?? filter_var(trim($_POST['email'] ?? ''), FILTER_SANITIZE_EMAIL);

        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE booking_id = ? AND LOWER(guest_email) = LOWER(?)");
        $stmt->execute([$ref, $email]);
        $booking = $stmt->fetch();

        if (!$booking) {
            echo json_encode(['status' => 'error', 'message' => 'Booking not found.']);
            exit;
        }
        if ($booking['status'] === 'Cancelled') {
            echo json_encode(['status' => 'error', 'message' => 'This booking is already cancelled.']);
            exit;
        }

        $created     = new DateTime($booking['created_at']);
        $now         = new DateTime();
        $daysElapsed = (int)$created->diff($now)->days;

        $fee = ($daysElapsed <= 5) ? 0.00 : round((float)$booking['total_amount'] * 0.25, 2);

        $upd = $pdo->prepare("UPDATE bookings SET status = 'Cancelled', cancellation_fee = ?, cancelled_at = NOW() WHERE booking_id = ?");
        $upd->execute([$fee, $ref]);

        $policyMsg = ($fee == 0)
            ? "Cancelled within 5 days of registration (FREE cancellation)."
            : "Cancelled after {$daysElapsed} days (25% cancellation fee applied: R " . number_format($fee, 2) . ").";

        $pdo->prepare("INSERT INTO notifications (category, title, body, sender_email) VALUES ('Cancellation', ?, ?, ?)")
            ->execute(["Booking Cancelled (#{$ref})", "{$booking['guest_name']} cancelled {$booking['room_name']}. {$policyMsg}", $email]);

        notifyHotelGmail("Imperial Solis Hotel Cancellation #{$ref}", "{$booking['guest_name']} cancelled booking #{$ref}. {$policyMsg}");

        echo json_encode(['status'           => 'success',
'cancellation_fee' => $fee,
'days_elapsed'     => $daysElapsed,
'message'          => $policyMsg
]);
exit;
}
// 5. CONTACT MESSAGE & GUEST REVIEWS
if ($action === 'send_message' || $action === 'submit_review') {
$name   = cleanInput($_POST['name'] ?? '');
$email  = filter_var(trim($_POST['email'] ?? ''), FILTER_SANITIZE_EMAIL);
$detail = cleanInput($_POST['message'] ?? $_POST['review'] ?? '');
$rating = max(1, min(5, (int)($_POST['rating'] ?? 5)));
if ($action === 'submit_review') {
$pdo->prepare("INSERT INTO reviews (guest_name, guest_email, rating, review_text) VALUES (?, ?, ?, ?)")
->execute([$name, $email, $rating, $detail]);
}
$cat   = ($action === 'submit_review') ? 'Review' : 'Message';
$title = ($action === 'submit_review') ? "New {$rating}★ Review from {$name}" : "New Inquiry from {$name}";
$pdo->prepare("INSERT INTO notifications (category, title, body, sender_email) VALUES (?, ?, ?, ?)")
->execute([$cat, $title, $detail, $email]);
notifyHotelGmail("Imperial Solis Hotel - {$title}", "From: {$name} ({$email})\n\n{$detail}");
echo json_encode(['status' => 'success']);
exit;
}
echo json_encode(['status' => 'error', 'message' => 'Invalid API action.']);
} catch (Exception $e) {
http_response_code(500);
echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $e->getMessage()]);
}