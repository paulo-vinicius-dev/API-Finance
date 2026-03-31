INSERT INTO users (id, email, full_name,hashed_password, is_active) VALUES 
('b482787c-4021-40a9-bd1b-f70a31b8e8ec', 'admin@admin.com', 'admin','$2a$12$hXs.KDCwoXz7uKGiYeuAw.7z/E/UzBeg/4rMEw6AaLawKZvYmko.S', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role) VALUES ('b482787c-4021-40a9-bd1b-f70a31b8e8ec', 'ADMIN')
ON CONFLICT (user_id, role) DO NOTHING;