-- Default admin user 
INSERT INTO users (id, email, full_name,hashed_password, is_active) VALUES ('b482787c-4021-40a9-bd1b-f70a31b8e8ec', 'admin@admin.com', 'admin','$2a$12$ZDbQ2yu5IPCePB0sCmmwyOdc.cLfmTLZNAidr/rlHc9o4EZEDqHHK', true) ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role) VALUES ('b482787c-4021-40a9-bd1b-f70a31b8e8ec', 'ADMIN') ON CONFLICT (user_id, role) DO NOTHING;

-- Default categories
INSERT INTO categories (id, name, user_id, is_default) VALUES ('457472ac-a682-43df-b261-84d9f324a88e', 'Alimentação', null, true) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, user_id, is_default) VALUES ('f703c0a5-0f66-4a2b-83d2-5cf9b992bc48', 'Transporte', null, true) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, user_id, is_default) VALUES ('345d0424-b07a-45d5-96ab-be90b62abcfd', 'Moradia', null, true) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, user_id, is_default) VALUES ('50c6506d-60ef-4258-b416-9fafe5f8863b', 'Lazer', null, true) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, user_id, is_default) VALUES ('fceb554b-3aa9-4399-97b1-f6b0a521d680', 'Saúde', null, true) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, user_id, is_default) VALUES ('792d9ef9-1196-4282-9488-9deacad9163e', 'Educação', null, true) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, user_id, is_default) VALUES ('e627d98a-bd9f-436c-b03a-d34c46c45ff8', 'Salário', null, true) ON CONFLICT (id) DO NOTHING;
INSERT INTO categories (id, name, user_id, is_default) VALUES ('c9852749-fbb3-419c-b014-9bfa8350c10b', 'Investimentos', null, true) ON CONFLICT (id) DO NOTHING;
