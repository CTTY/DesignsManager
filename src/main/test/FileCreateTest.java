import java.io.File;

public class FileCreateTest{
    FileCreateTest(){
        new File("./download");
        File file = new File("download/HelloNewDir.txt");
        try{
            file.createNewFile();
        }catch(Exception e){

        }

    }
}