package com.tortiki.api.infrastructure.adapter.out.storage;

import com.tortiki.api.application.port.out.StoragePort;
import com.tortiki.api.domain.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptateur secondaire pour le stockage de fichiers via MinIO.
 *
 * <p>Implémente {@link StoragePort} en utilisant le SDK MinIO Java.
 * Crée le bucket automatiquement s'il n'existe pas encore.</p>
 *
 * <p>Appartient à la couche {@code infrastructure/adapter/out/storage} —
 * seule couche autorisée à dépendre du SDK MinIO.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageAdapter implements StoragePort {

  private final MinioClient minioClient;

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.bucket}")
  private String bucket;

  /**
   * Téléverse un fichier dans le bucket MinIO et retourne son URL publique.
   *
   * <p>Convertit le tableau d'octets en {@link ByteArrayInputStream}
   * pour satisfaire l'API MinIO SDK. La taille exacte est fournie à
   * {@code PutObjectArgs} pour éviter le buffering en mémoire MinIO.</p>
   *
   * @param fileName    nom du fichier cible dans le bucket
   * @param fileBytes   contenu binaire du fichier à uploader
   * @param contentType type MIME du fichier (ex. {@code image/jpeg})
   * @return l'URL publique d'accès au fichier uploadé
   * @throws StorageException en cas d'échec de l'upload
   */
  @Override
  public String upload(String fileName, byte[] fileBytes, String contentType) {
    try {
      creerBucketSiAbsent();
      final InputStream stream = new ByteArrayInputStream(fileBytes);
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucket)
              .object(fileName)
              .stream(stream, fileBytes.length, -1)
              .contentType(contentType)
              .build()
      );
      final String url = endpoint + "/" + bucket + "/" + fileName;
      log.info("Fichier uploadé avec succès : {}", url);
      return url;
    } catch (Exception ex) {
      log.error("Échec de l'upload du fichier {} : {}", fileName, ex.getMessage());
      throw new StorageException("Échec de l'upload du fichier : " + fileName, ex);
    }
  }

  /**
   * Crée le bucket MinIO s'il n'existe pas encore.
   *
   * @throws Exception en cas d'erreur de communication avec MinIO
   */
  private void creerBucketSiAbsent() throws Exception {
    final boolean exists = minioClient.bucketExists(
        BucketExistsArgs.builder().bucket(bucket).build()
    );
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
      log.info("Bucket créé : {}", bucket);
    }
  }
}