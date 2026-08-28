CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     name TEXT NOT NULL,
                                     email TEXT NOT NULL,
                                     password_hash TEXT NOT NULL,
                                     role TEXT NOT NULL CHECK (role IN ('REPORTER', 'ADMIN_VERIFIER')),
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );