package com.tortiki.api.infrastructure.adapter.out.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.domain.exception.StorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests unitaires de {@link MinioStorageAdapter}.
 *
 * <p>Vérifie le comportement de l'adaptateur de stockage avec un client
 * MinIO mocké — aucune connexion réseau réelle n'est établie.</p>
 */
@Epic("Infrastructure")
@Feature("Stockage MinIO")
@ExtendWith(MockitoExtension.class)
@DisplayName("MinioStorageAdapter — tests unitaires")
class MinioStorageAdapterTest {

  @Mock
  private MinioClient minioClient;

  @InjectMocks
  private MinioStorageAdapter adapter;

  private static final String BUCKET       = "tortiki-photos";
  private static final String ENDPOINT     = "http://localhost:9000";
  private static final String FILE_NAME    = "annonce-123.jpg";
  private static final String CONTENT_TYPE = "image/jpeg";
  private static final byte[] FILE_BYTES   = "photo".getBytes();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(adapter, "bucket", BUCKET);
    ReflectionTestUtils.setField(adapter, "endpoint", ENDPOINT);
  }

  @Test
  @Story("Upload photo annonce")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("upload() — cas nominal : bucket existant → retourne l'URL publique")
  void upload_bucketExistant_retourneUrl() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

    String url = adapter.upload(FILE_NAME, FILE_BYTES, CONTENT_TYPE);

    assertThat(url).isEqualTo(ENDPOINT + "/" + BUCKET + "/" + FILE_NAME);
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  @Story("Upload photo annonce")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("upload() — bucket absent → crée le bucket puis upload")
  void upload_bucketAbsent_creeBucketPuisUploade() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

    adapter.upload(FILE_NAME, FILE_BYTES, CONTENT_TYPE);

    verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  @Story("Upload photo annonce")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("upload() — échec MinIO → lève StorageException")
  void upload_echecMinio_leveStorageException() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class)))
        .thenThrow(new RuntimeException("MinIO indisponible"));

    assertThatThrownBy(() -> adapter.upload(FILE_NAME, FILE_BYTES, CONTENT_TYPE))
        .isInstanceOf(StorageException.class)
        .hasMessageContaining(FILE_NAME);
  }
}