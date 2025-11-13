-- Create default admin user. Password hash and salt left blank so admin must set on first run or use provided utility
-- Insert admin without specifying id so the IDENTITY/auto-increment value is maintained
INSERT INTO
    users (
        username,
        password_hash,
        salt,
        role,
        last_login,
        force_password_reset
    )
VALUES
    (
        'admin',
        'SklFFbpsrNp1IJUagGARbMhpQ5cgGBpsOjDD2fl6pWg=',
        'YOZOEvmdmZqG6DgrIRNgQA==',
        'ADMIN',
        NULL,
        FALSE
    );