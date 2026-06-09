package com.tortiki.api.infrastructure.adapter.out.storage;

import com.tortiki.api.application.port.out.StoragePort;
import com.tortiki.api.domain.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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
   * @param fileName    nom du fichier cible dans le bucket
   * @param inputStream flux binaire du fichier à uploader
   * @param contentType type MIME du fichier (ex. {@code image/jpeg})
   * @return l'URL publique d'accès au fichier uploadé
   * @throws StorageException en cas d'échec de l'upload
   */
  @Override
  public String upload(String fileName, InputStream inputStream, String contentType) {
    try {
      creerBucketSiAbsent();
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(bucket)
              .object(fileName)
              .stream(inputStream, -1, 10_485_760)
              .contentType(contentType)
              .build()
      );
      String url = endpoint + "/" + bucket + "/" + fileName;
      log.info("Fichier upload avec succès : {}", url);
      return url;
    } catch (Exception e) {
      log.error("Échec de upload du fichier {} : {}", fileName, e.getMessage());
      throw new StorageException("Échec de upload du fichier : " + fileName, e);
    }
  }

  /**
   * Crée le bucket MinIO s'il n'existe pas encore.
   *
   * @throws Exception en cas d'erreur de communication avec MinIO
   */
  private void creerBucketSiAbsent() throws Exception {
    boolean exists = minioClient.bucketExists(
        BucketExistsArgs.builder().bucket(bucket).build()
    );
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
      log.info("Bucket créé : {}", bucket);
    }
  }
}