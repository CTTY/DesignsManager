import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class FileWriterTest {
    FileWriterTest(){
        String msg = "Hello World";
        File testFile = new File("description.txt");
        if(testFile.exists()){
            testFile.delete();
            try{
                testFile.createNewFile();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        try{
            Files.write(testFile.toPath(), msg.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }



    }


}
