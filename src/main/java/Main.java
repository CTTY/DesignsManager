import io.vertx.core.Vertx;

public class Main {

    public static void main (String[] args){
        Vertx vertx = Vertx.vertx();
        try{
            vertx.deployVerticle(new UploadVerticle());

        }catch (Exception e){
            System.out.println("Wrong");
        }



    }
}
