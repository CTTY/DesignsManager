import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class S3Verticle {
    private S3Client s3;

    private final String fileBucketName = "peicenjiang-design";
    private final String descriptionBucketName = "peicenjiang-design";

    S3Verticle(){
        /*Intializing S3*/
        Region region = Region.US_EAST_1;
        s3 = S3Client.builder().region(region).build();
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

}
