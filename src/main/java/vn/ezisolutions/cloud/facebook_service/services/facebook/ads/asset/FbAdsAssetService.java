package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.asset;

import com.facebook.ads.sdk.AdImage;
import com.facebook.ads.sdk.AdVideo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAsset;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsAssetRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account.FbAdsAccountService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FbAdsAssetService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsAssetService.class);
    private final FbAdsAssetClientService assetClientService;
    private final FacebookTokenService tokenService;
    private final FbAdsAssetRepository assetRepo;
    private final FbAdsAccountService accountService;

    public FbAdsAsset uploadAsset(String ownerId, String adAccountId, MultipartFile file, FbAdsAsset.AssetType type) throws CustomException {
        validateFileFormatAndSize(file.getOriginalFilename(), file.getSize(), type);
        FbAdsAccount account = accountService.validateAndGetAccount(adAccountId, ownerId);

        String token = tokenService.getUserAccessToken(account.getFbUserId());
        if (token == null) throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");

        File tempFile = convertMultiPartToFile(file);
        UploadedFileDetails details = new UploadedFileDetails(tempFile, file.getOriginalFilename(), file.getSize(), null);

        return processAndSaveAsset(ownerId, adAccountId, token, details, type);
    }

    public FbAdsAsset uploadAssetFromUrl(String ownerId, String adAccountId, String fileUrl, FbAdsAsset.AssetType type) throws CustomException {
        FbAdsAccount account = accountService.validateAndGetAccount(adAccountId, ownerId);

        String token = tokenService.getUserAccessToken(account.getFbUserId());
        if (token == null) throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");

        File tempFile = downloadFileFromUrl(fileUrl);
        try {
            validateFileFormatAndSize(tempFile.getName(), tempFile.length(), type);
        } catch (CustomException e) {
            try {
                Files.delete(tempFile.toPath());
            } catch (IOException ioEx) {
                logger.warn("Không thể xóa file tạm thời: {}", tempFile.getAbsolutePath(), ioEx);
            }
            throw e;
        }
        UploadedFileDetails details = new UploadedFileDetails(tempFile, tempFile.getName(), tempFile.length(), fileUrl);
        return processAndSaveAsset(ownerId, adAccountId, token, details, type);
    }

    private FbAdsAsset processAndSaveAsset(String ownerId, String adAccountId, String token, UploadedFileDetails details, FbAdsAsset.AssetType type) {
        FbAdsAsset asset = FbAdsAsset.builder()
                .ownerId(ownerId)
                .adAccountId(adAccountId)
                .type(type)
                .name(details.fileName())
                .fileSizeBytes(details.fileSize())
                .url(details.url())
                .status(FbAdsAsset.AssetStatus.UPLOADING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        asset = assetRepo.save(asset);

        try {
            if (type == FbAdsAsset.AssetType.IMAGE) {
                AdImage fbImage = assetClientService.uploadImageToFacebook(adAccountId, token, details.tempFile());
                asset.setHash(fbImage.getFieldHash());
                if (details.url() == null) asset.setUrl(fbImage.getFieldUrl());
                asset.setStatus(FbAdsAsset.AssetStatus.READY);
            } else {
                AdVideo fbVideo = assetClientService.uploadVideoToFacebook(adAccountId, token, details.tempFile(), details.fileName());
                asset.setFbAssetId(fbVideo.getId());
                asset.setStatus(FbAdsAsset.AssetStatus.PROCESSING);
            }
            return assetRepo.save(asset);
        } catch (FacebookApiException e) {
            asset.setStatus(FbAdsAsset.AssetStatus.FAILED);
            asset.setErrorMessage(e.getUserFriendlyMessage() != null ? e.getUserFriendlyMessage() : e.getMessage());
            assetRepo.save(asset);
            throw e;
        } catch (Exception e) {
            asset.setStatus(FbAdsAsset.AssetStatus.FAILED);
            asset.setErrorMessage(e.getMessage());
            assetRepo.save(asset);
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi upload asset: " + e.getMessage(), null);
        } finally {
            if (details.tempFile().exists()) {
                try {
                    Files.delete(details.tempFile().toPath());
                } catch (IOException ioEx) {
                    logger.warn("Không thể xóa file tạm thời sau khi xử lý: {}", details.tempFile().getAbsolutePath(), ioEx);
                }
            }
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws CustomException {
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir"), System.currentTimeMillis() + "_" + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(file.getBytes());
            }
            return convFile;
        } catch (IOException e) {
            throw new CustomException(500, "Lỗi xử lý file tạm thời ");
        }
    }

    private File downloadFileFromUrl(String fileUrl) throws CustomException {
        try {
            URL url = java.net.URI.create(fileUrl).toURL();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            if (fileName.contains("?")) fileName = fileName.substring(0, fileName.indexOf('?'));
            if (fileName.isEmpty() || fileName.length() > 50) fileName = "url_download_" + System.currentTimeMillis();

            File tempFile = new File(System.getProperty("java.io.tmpdir"), System.currentTimeMillis() + "_" + fileName);
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        } catch (Exception e) {
            logger.error("Lỗi khi tải file từ URL: {}", fileUrl, e);
            throw new CustomException(400, "Không thể tải dữ liệu từ đường link được cung cấp.");
        }
    }

    private void validateFileFormatAndSize(String originalFilename, long sizeInBytes, FbAdsAsset.AssetType type) throws CustomException {
        String filename = originalFilename != null ? originalFilename.toLowerCase() : "";

        if (type == FbAdsAsset.AssetType.IMAGE) {
            if (sizeInBytes > 30 * 1024 * 1024) {
                throw new CustomException(400, "Dung lượng ảnh tối đa là 30MB");
            }
            if (!filename.matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
                throw new CustomException(400, "Định dạng ảnh không hợp lệ. Chỉ hỗ trợ: JPG, JPEG, PNG, GIF, BMP");
            }
        } else if (type == FbAdsAsset.AssetType.VIDEO) {
            if (sizeInBytes > 4000L * 1024 * 1024) {
                throw new CustomException(400, "Dung lượng video tối đa là 4GB");
            }
            if (!filename.matches(".*\\.(mp4|mov|avi)$")) {
                throw new CustomException(400, "Định dạng video không hợp lệ. Chỉ hỗ trợ: MP4, MOV, AVI");
            }
        }
    }

    private record UploadedFileDetails(File tempFile, String fileName, long fileSize, String url) {}
}
