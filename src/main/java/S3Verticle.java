import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class S3Verticle {
    private S3Client s3;
    private S3Utilities s3Utilities;

    private final String fileBucketName = "peicenjiang-design";
    private final String descriptionBucketName = "peicenjiang-design";

    S3Verticle(){
        /*Intializing S3*/
        Region region = Region.US_EAST_1;
        s3 = S3Client.builder().region(region).build();
        s3Utilities = S3Utilities.builder().region(region).build();
    }


    /** Put design on the website*/
    public void putDesign(Path path, String description){
        String key = path.getFileName().toString();
        try{
            s3.putObject(PutObjectRequest.builder()
                            .bucket(fileBucketName)
                            .key(key)
                            .build()
                    , path);
        }catch(Exception e){
            // Upload file failed
            e.printStackTrace();
            return;
        }

        File tempFile = new File("description.txt");
        if(tempFile.exists()){
            tempFile.delete();
            try{
                tempFile.createNewFile();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        try{
            Files.write(tempFile.toPath(), description.getBytes());
        }catch (Exception e){
            System.out.println("Cannot write");
            e.printStackTrace();
        }
        try{
            s3.putObject(PutObjectRequest.builder()
                    .bucket(descriptionBucketName)
                    .key(key + "-description.txt")
                    .build(), tempFile.toPath());
        }catch(Exception e){
            //Upload description failed
            e.printStackTrace();
            return;
        }


    }
    /** Get design from the s3*/
    public void getDesign(String key){

        String descriptionKey = key + "-description.txt";
        GetObjectRequest designReq;
        GetObjectRequest descriptionReq;
        File tempDir = new File("./download");
        if(!tempDir.exists())
            tempDir.mkdir();

        tempDir = new File("./download/design");
        if(!tempDir.exists())
            tempDir.mkdir();

        tempDir = new File("./download/description");
        if(!tempDir.exists())
            tempDir.mkdir();

        File design = new File("download/design/" + key);
        File description = new File("download/description/" + descriptionKey);
        if(design.exists()){
            try{
                design.delete();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        if(description.exists()){
            try{
                description.delete();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        Path designPath = design.toPath();
        Path descriptionPath = description.toPath();
        //Compose requests
        designReq = GetObjectRequest.builder()
                .bucket(fileBucketName)
                .key(key)
                .build();

        descriptionReq = GetObjectRequest.builder()
                .bucket(fileBucketName)
                .key(descriptionKey)
                .build();

        //Get design and description
        try{
            s3.getObject(designReq, designPath);
            s3.getObject(descriptionReq, descriptionPath);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("getObject Failure");
        }
    }

    /** Delete design on the website*/
    public void deleteDesign(String fileName){
        String key = fileName;

        //Delete file
        try{
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(fileBucketName)
                    .key(key)
                    .build());
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Delete file failed");
        }


        //Delete description
        try{
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(descriptionBucketName)
                    .key(key + "-description.txt")
                    .build());
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Delete description failed");
        }
    }

    public List<S3Object> listDesign(){
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(fileBucketName)
                .build();

        ListObjectsResponse response = s3.listObjects(request);

        List<S3Object> res = new ArrayList<>();
        for(S3Object obj:response.contents()){
            if(!obj.key().endsWith("-description.txt")){
                res.add(obj);
            }
        }
        return res;
    }

    public URL getDesignURL(String key){
        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(fileBucketName)
                .key(key)
                .build();
        return s3Utilities.getUrl(request);
    }

}
