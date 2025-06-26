-- Insertion des rôles
INSERT INTO role (nom, description) VALUES ('ADMIN', 'Administrateur système');
INSERT INTO role (nom, description) VALUES ('VALIDATEUR', 'Validateur de documents');
INSERT INTO role (nom, description) VALUES ('CONTRIBUTEUR', 'Contributeur de documents');

-- Insertion de l'utilisateur admin (mot de passe: 'password123')
INSERT INTO "utilisateur" (
    nom,
    prenom,
    email,
    username,
    password,
    statut,
    is_first_login,
    date_creation,
    compte_verrouille,
    tentatives_connexion
) VALUES (
    'Admin',
    'System',
    'admin@adcsa.cm',
    'admin',
    '$2y$10$yGUls1hry2DJjnVZQTIIoucOf7biOEz2LMmGq.VNYAvzYLXHTIE5W', -- password123
    'ACTIF',
    false,
    CURRENT_TIMESTAMP,
    false,
    0
);

-- Association de l'utilisateur admin au rôle ADMIN
INSERT INTO utilisateur_role (utilisateur_id, role_id)
SELECT u.id, r.id
FROM "utilisateur" u, role r
WHERE u.username = 'admin' AND r.nom = 'ADMIN'; 