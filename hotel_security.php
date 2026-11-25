<?php
// hotel_security.php — Security Layer for Imperial Solis Hotel
if (session_status() === PHP_SESSION_NONE) {
    session_set_cookie_params([
        'lifetime' => 3600,
        'path' => '/',
        'httponly' => true,
        'samesite' => 'Strict'
    ]);
    session_start();
}

// Security Headers
header("X-Frame-Options: DENY");
header("X-Content-Type-Options: nosniff");
header("X-XSS-Protection: 1; mode=block");
header("Referrer-Policy: strict-origin-when-cross-origin");
header("Content-Type: application/json; charset=UTF-8");

// Database Connection (PDO with Prepared Statements)
function getHotelDB(): PDO {
    $host = '127.0.0.1';
    $db   = 'hotel_db';
    $user = 'hotel_user';
    $pass = 'Hotel@2026Secure!';
    $dsn  = "mysql:host=$host;dbname=$db;charset=utf8mb4";

    return new PDO($dsn, $user, $pass, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
}

// Input Sanitization
function cleanInput(string $data): string {
    return htmlspecialchars(strip_tags(trim($data)), ENT_QUOTES, 'UTF-8');
}

// Rate Limiter (Max 25 attempts per 5 minutes per IP)
function checkRateLimit(string $actionKey): void {
    $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
    $key = "rl_{$actionKey}_{$ip}";
    $now = time();

    if (!isset($_SESSION[$key])) {
        $_SESSION[$key] = ['count' => 1, 'start' => $now];
        return;
    }

    if ($now - $_SESSION[$key]['start'] > 300) {
        $_SESSION[$key] = ['count' => 1, 'start' => $now];
        return;
    }

    $_SESSION[$key]['count']++;
    if ($_SESSION[$key]['count'] > 25) {
        http_response_code(429);
        echo json_encode(['status' => 'error', 'message' => 'Too many requests. Please wait 5 minutes.']);
        exit;
    }
}