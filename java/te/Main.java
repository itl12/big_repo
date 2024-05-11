public class Main extends Client{

    
    static void show(){
        System.out.println("SHOwing");
    }

    public void shows(){
        System.out.println("Public");
    }

    public static void main(String[] args){
        Client obj = new Client();
        obj.show();

        
    }

}


