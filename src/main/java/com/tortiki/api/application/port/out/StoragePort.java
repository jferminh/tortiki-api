package com.tortiki.api.application.port.out;

/**
 * Port secondaire pour le stockage de fichiers.
 *
 * <p>Définit le contrat de téléversement d'un fichier média sans exposer
 * les détails de l'implémentation (MinIO, S3, disque local, etc.).</p>
 *
 * <p>Appartient à la couche {@code application/port/out} — aucune dépendance
 * vers l'infrastructure n'est autorisée dans cette interface.</p>
 *
 * <p>Le contenu binaire est passé sous forme de {@code byte[]} afin de
 * rester cohérent avec {@code ManageListingUseCase.PhotoCommand} et de
 * faciliter les tests unitaires sans mock de flux.</p>
 */
public interface StoragePort {

  /**
   * Téléverse un fichier et retourne son URL publique.
   *
   * @param fileName    nom du fichier cible dans le bucket
   * @param fileBytes   contenu binaire du fichier à uploader
   * @param contentType type MIME du fichier (ex. {@code image/jpeg})
   * @return l'URL publique d'accès au fichier uploadé
   */
  String upload(String fileName, byte[] fileBytes, String contentType);
}