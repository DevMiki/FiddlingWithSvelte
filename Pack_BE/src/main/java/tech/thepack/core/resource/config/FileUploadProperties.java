package tech.thepack.core.resource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "pack.file-upload")
public class FileUploadProperties {
    DataSize maxFileSize;

    public DataSize getMaxFileSize() {
        return maxFileSize;
    }

    public String getStringMaxSizeInMB() {
        double sizeInMB = (double) maxFileSize.toBytes() / (1024 * 1024);
        return String.format("%.2f MB", sizeInMB);
    }

    public void setMaxFileSize(DataSize maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}
