import java.io.File;

public class DeleteDirectoryTest {
    DeleteDirectoryTest(){
        DesignManagerVerticle test = new DesignManagerVerticle();
        boolean result = test.deleteDirectory(new File("./download"));
        System.out.println("Delete result: " + result);
    }
}
