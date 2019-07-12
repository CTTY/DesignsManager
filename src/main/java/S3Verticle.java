import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

public class S3Verticle {
    private S3Client s3;

    private final String fileBucketName = "Designs";
    private final String descriptionBucketName = "Descriptions";

    S3Verticle(){
        /*Intializing S3*/
        Region region = Region.US_EAST_1;
        s3 = S3Client.builder().region(region).build();
    }


    /** Put design on the website*/
    private void putDesign(Path path, String description){
        s3.putObject(PutObjectRequest.builder()
                        .bucket(fileBucketName)
                        .key(path.getFileName().toString())
                        .build()
                , path);

        File tempFile = new File("tempFile");
        if(tempFile.exists()){
            tempFile.delete();
            try{
                tempFile.createNewFile();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        try{
            FileWriter fw = new FileWriter(tempFile);
            fw.write(description);
        }catch (Exception e){
            e.printStackTrace();
        }

        s3.putObject(PutObjectRequest.builder()
                        .bucket(descriptionBucketName)
                        .key(path.getFileName().toString())
                        .build(), tempFile.toPath());

    }

    /** Delete design on the website*/
    private void deleteDesign(String fileName){

    }

    /** Replace design on the website*/
    private void replaceDesign(Path path, String description){

    }
}
