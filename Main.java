public class Main {

    public static void main(String[] args) {

        //calculateSha256UseDataFile(args);
        //calculateSha256("486F7573746F6E2C207765206861766520612070726F626C656D2100");
        runTest();

        System.out.println("Finished main.");
    }
    ////////////////////////////////////////////////
    public static String calculateSha256(String inputHex){

        Sha256 sha = new Sha256(inputHex, false);

        System.out.println("In hex form, hash output is:");
        System.out.println(sha.getHashAsHex());

        return sha.getHashAsHex();

    }
    ////////////////////////////////////////////////
    public static void calculateSha256UseDataFile(String[] args){
        String pathToFile;
        if (args.length > 0) {
            pathToFile = args[0];

        } else {
            //String userDirectory = System.getProperty("user.dir");
            //System.out.println("User dir: " + userDirectory);
            //pathToFile = new String("data/seven_eleven.txt");
            //pathToFile = new String("data/zero.txt");
            //pathToFile = new String("data/fourZeros.txt");
            //pathToFile = new String("data/hw.txt");
            pathToFile = new String("data/hexData.txt");
            //pathToFile = new String("data/houston.txt");

            // pathToFile = new String("data/fourOneFourTwo.txt");
        }

        Sha256 sha = new Sha256(pathToFile, true);

        System.out.println("In hex form, hash output is:");
        System.out.println(sha.getHashAsHex());
        System.out.println("Finished main.");
    }
    ////////////////////////////////////////////////
    public static void runTest(){
        String input = "ABBA";
        Sha256 sha = new Sha256(input, false);
        String hash = sha.getHashAsHex();

        String comparison = "84ECFC628F1576ED179241E240DB0A77F986954546ADBC207E9C43E032F18450";

        if ( hash.equals(comparison)){
            System.out.println("\n\nThe test passed. \n\n");
        } else {
            System.out.println("\n\nThe test failed, did not get the hash that was expected.\n\n");
            System.out.println("Was expecting hash: " + comparison);
        }
        System.out.println("Got the hash:       " + hash);

    }
}   // end of class Main
