package com.mocafelab.web.file;

import java.io.File;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.newfrom.lib.file.FileUploader;
import net.newfrom.lib.file.S3UploadOption;
import net.newfrom.lib.file.UploadFileInfo;
import net.newfrom.lib.file.UploadOptions;

@Component
public class S3Service {
	@Value("${cloud.aws.credentials.profile-name}")
	private String AWS_CREDENTIALS_PROFILE_NAME;
	
	@Value("${cloud.aws.s3.bucket}")
	private String BUCKET_NAME;
	
	@Value("${file.path.upload.s3}")
	private String FILE_PATH_UPLOAD_S3;
	
	@Value("${file.path.ad.sg.default}")
	private String FILE_PATH_AD_SG_DEFAULT;
	
	private FileUploader fileUploader;
	
	private UploadOptions uploadOptions;
	
	@PostConstruct
	private void init() throws Exception { 
		uploadOptions = new S3UploadOption(AWS_CREDENTIALS_PROFILE_NAME, BUCKET_NAME, FILE_PATH_UPLOAD_S3);
		fileUploader = new FileUploader(uploadOptions); 
	}
	
	public void setUploadOption(S3UploadOption uploadOptions) throws Exception{
		this.uploadOptions = uploadOptions;
		this.fileUploader = new FileUploader(uploadOptions);
	}
	
	public UploadOptions getUploadOption() throws Exception {
		return uploadOptions;
	}
	
	public UploadFileInfo uploadFile(MultipartFile mFile, String filePath) throws Exception {
		UploadFileInfo uploadFileInfo = fileUploader.encodeUploadFile(mFile, filePath);
		return uploadFileInfo;
	}
	
	public boolean removeFile(String fullPath) throws Exception {
		return fileUploader.removeFile(fullPath);
	}
	
	public Map<String, Object> copyFile(String oldFullPath, String newFullPath) throws Exception {
		return fileUploader.encodeCopyFile(oldFullPath, newFullPath);
	}
}
