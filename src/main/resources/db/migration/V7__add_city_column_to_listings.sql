-- Ajoute une colonne ville structurée sur les annonces.
-- Nécessaire pour fiabiliser l'autocomplétion de recherche (Issue 147) :
-- la table ne contenait jusqu'ici que pickup_address en texte libre,
-- insuffisant pour une requête DISTINCT fiable sur la ville.
ALTER TABLE listings
    ADD COLUMN city VARCHAR(100);

-- Rétro-remplissage best-effort à partir de pickup_address existant.
-- Hypothèse : format "numéro rue, ville" — à vérifier manuellement en cas
-- de données historiques non conformes avant mise en production.
UPDATE listings
SET city = TRIM(SPLIT_PART(pickup_address, ',', 2))
WHERE city IS NULL;

-- Colonne rendue obligatoire une fois le rétro-remplissage effectué.
ALTER TABLE listings
    ALTER COLUMN city SET NOT NULL;