public class TestObject {

    private String message;
     @Override
    public String toString(){
         return message;
     }

     public TestObject(){
         StringBuilder sb = new StringBuilder("Hello Vertx");

         this.message = sb.toString();
     }
}
