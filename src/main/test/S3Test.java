import java.io.File;

public class S3Test {

    S3Test(){
        S3Manager s3 = new S3Manager();

        File file = new File("TestFile.txt");
        try{
            s3.putDesign(file.toPath(), "This is a big hello");
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Put design failed");
        }

        try{
            s3.deleteDesign("TestFile.txt");
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Delete design failed");
        }

        System.out.println("S3Test Success");


    }

}
