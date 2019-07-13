import java.io.File;

public class S3Test {

    S3Test(){
        S3Manager s3 = new S3Manager();

        File file = new File("TestFile.txt");
        s3.putDesign(file.toPath(), "This is a big hello");
    }

}
