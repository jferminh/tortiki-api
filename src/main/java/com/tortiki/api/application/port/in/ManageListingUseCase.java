package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Port primaire — cas d'usage : gestion des annonces de plats.
 *
 * <p>Regroupe les opérations CRUD sur les annonces ainsi que la gestion
 * du statut et de la photo. L'implémentation est assurée par
 * {@code ListingService} dans la couche {@code application/service/}.</p>
 *
 * <p>Ce port est appelé par {@code ListingController} dans
 * {@code infrastructure/adapter/in/web/}.</p>
 */
public interface ManageListingUseCase {

  /**
   * Crée une nouvelle annonce de plat pour un vendeur.
   *
   * @param sellerId identifiant du vendeur propriétaire
   * @param command  données de l'annonce à créer
   * @return l'annonce créée avec son identifiant technique
   * @throws com.tortiki.api.domain.exception.UserNotFoundException
   *         si le vendeur est introuvable
   * @throws com.tortiki.api.domain.exception.CuisineTypeNotFoundException
   *         si l'origine culinaire est introuvable
   */
  Listing create(Long sellerId, ManageListingUseCase.Command command);

  /**
   * Met à jour une annonce existante.
   *
   * <p>Seul le vendeur propriétaire peut modifier son annonce.</p>
   *
   * @param listingId identifiant de l'annonce à modifier
   * @param sellerId  identifiant du vendeur demandant la modification
   * @param command   nouvelles données de l'annonce
   * @return l'annonce mise à jour
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   * @throws com.tortiki.api.domain.exception.UnauthorizedActionException
   *         si le vendeur n'est pas propriétaire de l'annonce
   */
  Listing update(Long listingId, Long sellerId, ManageListingUseCase.Command command);

  /**
   * Met à jour la photo d'une annonce via upload vers le stockage.
   *
   * <p>Le port reste agnostique du transport HTTP — il reçoit un
   * {@link PhotoCommand} contenant les bytes, jamais un
   * {@code MultipartFile}.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param sellerId  identifiant du vendeur propriétaire
   * @param command   données de la photo à uploader
   * @return l'annonce mise à jour avec la nouvelle URL photo
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   * @throws com.tortiki.api.domain.exception.UnauthorizedActionException
   *         si le vendeur n'est pas propriétaire de l'annonce
   */
  Listing updatePhoto(Long listingId, Long sellerId, ManageListingUseCase.PhotoCommand command);

  /**
   * Supprime une annonce (suppression logique — statut {@code INACTIVE}).
   *
   * @param listingId identifiant de l'annonce à supprimer
   * @param sellerId  identifiant du vendeur propriétaire
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   * @throws com.tortiki.api.domain.exception.UnauthorizedActionException
   *         si le vendeur n'est pas propriétaire de l'annonce
   */
  void delete(Long listingId, Long sellerId);

  /**
   * Retourne toutes les annonces actives de la plateforme.
   *
   * @return liste des annonces au statut {@code ACTIVE}, vide si aucune
   */
  List<Listing> findAll();

  /**
   * Retourne toutes les annonces appartenant à un vendeur donné,
   * quel que soit leur statut (actif, inactif, épuisé).
   *
   * <p>Contrairement à {@link #findAll()} qui ne retourne que les annonces
   * actives et publiques, cette méthode sert le tableau de bord vendeur :
   * Sofia doit pouvoir voir et gérer ses annonces désactivées.</p>
   *
   * @param sellerId identifiant du vendeur, résolu depuis l'authentification
   * @return liste des annonces du vendeur, triées par date de création
   */
  List<Listing> findBySeller(Long sellerId);

  /**
   * Retourne une annonce par son identifiant.
   *
   * @param listingId identifiant de l'annonce
   * @return l'annonce correspondante
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   */
  Listing findById(Long listingId);

  /**
   * Retourne toutes les annonces d'un vendeur pour son tableau de bord privé,
   * quel que soit leur statut (active, inactive, épuisée).
   *
   * <p>À distinguer de {@link #findBySeller(Long)}, qui ne retourne que les
   * annonces {@code ACTIVE} et sert un usage public (ex. profil vendeur
   * consultable par les acheteurs). Cette méthode-ci est réservée au vendeur
   * authentifié consultant ses propres annonces.</p>
   *
   * @param sellerId identifiant du vendeur, résolu depuis l'authentification
   * @return liste des annonces du vendeur, triées par date de création décroissante
   */
  List<Listing> findAllForSeller(Long sellerId);

  /**
   * Retourne la liste des villes distinctes ayant au moins une annonce active.
   *
   * <p>Alimente l'autocomplétion du champ de recherche par ville côté
   * frontend, sans exposer aucune donnée personnelle du vendeur —
   * seule la ville de retrait est retournée.</p>
   *
   * @return liste triée des villes distinctes, vide si aucune annonce active
   */
  List<String> findDistinctActiveCities();

  /**
   * Change le statut d'une annonce.
   *
   * <p>La modération vers {@code MODERATED} est réservée au rôle
   * {@code ROLE_ADMIN}. La vérification est faite par Spring Security
   * en amont dans le contrôleur.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param status    nouveau statut
   * @return l'annonce avec le statut mis à jour
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   */
  Listing changeStatus(Long listingId, ListingStatus status);

  /**
   * Commande d'entrée pour la création et la modification d'une annonce.
   *
   * <p>Record immuable Java 21. Remplace l'ancien ListingCommand
   * fichier séparé — le Command appartient sémantiquement à ce port.</p>
   *
   * @param title          titre de l'annonce
   * @param description    description détaillée du plat
   * @param price          prix unitaire en euros
   * @param portions       nombre de portions disponibles
   * @param pickupAddress  adresse de retrait saisie par le vendeur
   * @param pickupDatetime date et heure du créneau de retrait
   * @param cuisineTypeId  identifiant de l'origine culinaire
   * @param allergenIds    identifiants des allergènes présents
   */
  record Command(
      String title,
      String description,
      BigDecimal price,
      Integer portions,
      String pickupAddress,
      LocalDateTime pickupDatetime,
      Long cuisineTypeId,
      List<Long> allergenIds
  ) {}

  /**
   * Commande d'entrée pour la mise à jour de la photo d'une annonce.
   *
   * <p>Encapsule le contenu binaire afin que le port primaire reste
   * agnostique de {@code MultipartFile} (couche HTTP).</p>
   *
   * <p>{@code equals}, {@code hashCode} et {@code toString} sont surchargés
   * explicitement : les implémentations générées par défaut pour un
   * {@code record} comparent {@code photoBytes} par référence mémoire
   * (identité du tableau), jamais par contenu binaire. Sans cette
   * surcharge, deux commandes portant une photo strictement identique
   * en octets seraient jugées différentes.</p>
   *
   * @param photoBytes  contenu binaire de la photo
   * @param contentType type MIME (ex. {@code image/jpeg})
   * @param fileName    nom du fichier cible dans le bucket MinIO
   */
  record PhotoCommand(
      byte[] photoBytes,
      String contentType,
      String fileName
  ) {

    /**
     * Compare deux commandes photo par contenu binaire réel du tableau,
     * et non par référence mémoire.
     *
     * <p>Utilise la déconstruction de record (Java 21 record pattern)
     * pour extraire directement les composants de {@code other} sans
     * variable intermédiaire.</p>
     *
     * @param other objet à comparer
     * @return {@code true} si tous les champs, y compris le contenu
     *         binaire de {@code photoBytes}, sont égaux
     */
    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof PhotoCommand(
          byte[] otherBytes, String otherContentType, String otherFileName))) {
        return false;
      }
      return Arrays.equals(photoBytes, otherBytes)
          && Objects.equals(contentType, otherContentType)
          && Objects.equals(fileName, otherFileName);
    }

    /**
     * Calcule le hash à partir du contenu binaire réel du tableau,
     * et non de sa référence mémoire.
     *
     * @return le code de hachage cohérent avec {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
      int result = Objects.hash(contentType, fileName);
      result = 31 * result + Arrays.hashCode(photoBytes);
      return result;
    }

    /**
     * Représentation textuelle lisible, affichant la taille du tableau
     * plutôt que son adresse mémoire.
     *
     * @return une représentation textuelle de la commande photo
     */
    @Override
    @NotNull
    public String toString() {
      return "PhotoCommand[photoBytes.length=" + photoBytes.length
          + ", contentType=" + contentType
          + ", fileName=" + fileName + "]";
    }
  }
}