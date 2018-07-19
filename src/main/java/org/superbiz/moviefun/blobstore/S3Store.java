package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AbstractAmazonS3;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.tika.Tika;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client s3Client;
    private String photoStorageBucket;


    public S3Store( AmazonS3Client s3Client, String photoStorageBucket) {
        this.photoStorageBucket=photoStorageBucket;
        this.s3Client = s3Client;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        objectMetadata.setContentLength(blob.inputStream.available());
        s3Client.putObject(photoStorageBucket, blob.name, blob.inputStream, objectMetadata);


    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        if (!s3Client.doesObjectExist(photoStorageBucket, name)) {
            return Optional.empty();
        }
        S3Object s3Object = s3Client.getObject(photoStorageBucket, name);


        S3ObjectInputStream inputStream =  s3Object.getObjectContent();
        Blob blob = new Blob(name,inputStream, s3Object.getObjectMetadata().getContentType());
        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {
        List<S3ObjectSummary> summaries = s3Client
                .listObjects(photoStorageBucket)
                .getObjectSummaries();

        for (S3ObjectSummary summary : summaries) {
            s3Client.deleteObject(photoStorageBucket, summary.getKey());
        }
    }
}
