import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files


//////////////////////////////////////////////////////////
public class Sha256 {
    private String m_inputHex;
    private Bit[]  m_inputBinary;

    private ThirtyTwoBits[] m_w;
    private ThirtyTwoBits[] m_h;
    private ThirtyTwoBits[] m_k;

    private Bit[] m_hash;

    //////////////////////////////////////////////////////
    String getHashAsHex(){

        String ret = new String("");
        for(int i = 0; i < 64; i++){

            int y = 1;
            int x = 0;
            for(int j = 0; j < 4; j++){
                if ( m_hash[i*4 + 3 - j].getBoolean()){
                    x += y;
                }
                y *= 2;

            }
            ret += Integer.toHexString(x);
        }
        return ret.toUpperCase();

    }
    //////////////////////////////////////////////////////
    Bit[] getHash() {
        if(m_hash == null)
            calcHashFromInput();

        return m_hash;
    }
    //////////////////////////////////////////////////////
    Sha256(String input, boolean inputIsFilePath){

        if(inputIsFilePath){
            m_inputHex = getInputHexFromFile(input);
        } else {
            m_inputHex = input;
        }
        //System.out.println("Input: " + input);
        System.out.println("The input hex is: " + m_inputHex);
        setInputBinary(m_inputHex);
        System.out.println("In binary, the input is:");
        printBitArr(m_inputBinary);
        generateHash();

    }
    ////////////////////////////////////////////////////////
    private void initializeConstants(){
        m_w = new ThirtyTwoBits[16];

        initializeH();
        initializeK();
    }
    ////////////////////////////////////////////////////////
    private void calcHashFromInput(){
        doCalcPrelim();
        doCalcMainLoop();
    }
    //////////////////////////////////////////////////////
    private void doCalcPrelim(){
         /*
            begin with the original message of length L bits
            append a single '1' bit
            append K '0' bits, where K is the minimum number >= 0 such that (L + 1 + K + 64) is a multiple of 512
            append L as a 64-bit big-endian integer, making the total post-processed length a multiple of 512 bits
            such that the bits in the message are: <original message of length L> 1 <K zeros> <L as 64 bit integer>
         */
         int L = m_inputBinary.length;
         // L + 65 + K = n * 512

         int K = 512 - 65 - (L % 512);
         if (K < 0)
             K+= 512;

         Bit[] rest = new Bit[K + 65];
         rest[0] = new Bit(true, 0,0);

         for(int i = 1; i < (K + 34); i++){
            rest[i] = new Bit( false, 0,0 );
         }

         int y = 1;
         System.out.println("When converted to binary, input has length: " + Integer.toString(L));
         for(int i = 0; i < 31; i++){

             int b = (L / y) - 2 * (L / (y*2));

             rest[K+64-i] = new Bit ( b == 1, 0, 0);

             y *= 2;
         }

         //System.out.println("rest array:");  printBooleanArr(rest);

         m_w = new ThirtyTwoBits[64];

         for(int j = 0; j < 16; j++){
             m_w[j] = new ThirtyTwoBits();
             for( int r = 0; r < 32; r++){
                 int elm = j * 32 + r;

                 if( elm < L)
                     m_w[j].set(r, m_inputBinary[elm]);
                 else
                     m_w[j].set(r, rest[elm - L]);
             }
         }
    }
    //////////////////////////////////////////////////////
    private void doCalcMainLoop(){

        //Initialize working variables to current hash value:
        ThirtyTwoBits a[] = new ThirtyTwoBits[8];

        for(int n = 0; n < 8; n++){
            a[n] = m_h[n];
        }

        for(int i = 0; i < 64; i++) {

            if (i > 15) {
                ThirtyTwoBits t0 = m_w[(i - 15)%16].rotateRight(7).xor(m_w[(i - 15)%16].rotateRight(18));
                ThirtyTwoBits s0 = t0.xor(m_w[(i - 15)%16].shiftRight(3));

                ThirtyTwoBits t1 = m_w[(i - 2)%16].rotateRight(17).xor(m_w[(i - 2)%16].rotateRight(19));
                ThirtyTwoBits s1 = t1.xor(m_w[(i - 2)%16].shiftRight(10));

                ThirtyTwoBits t3 = m_w[i%16].integerAddition(s0);
                ThirtyTwoBits t4 = t3.integerAddition(m_w[(i - 7)%16]);

                m_w[i%16] = t4.integerAddition(s1);
            }

            ThirtyTwoBits t4  = a[4].rotateRight(6).xor(a[4].rotateRight(11));
            ThirtyTwoBits s3  = t4.xor(a[4].rotateRight(25));

            ThirtyTwoBits t5  = a[4].and(a[5]);
            ThirtyTwoBits t6  = a[4].not().and(a[6]);
            ThirtyTwoBits ch  = t5.xor(t6);

            ThirtyTwoBits t7  = a[7].integerAddition(s3).integerAddition(ch);
            ThirtyTwoBits t8  = t7.integerAddition(m_k[i]);
            ThirtyTwoBits t9  = t8.integerAddition(m_w[i%16]);

            ThirtyTwoBits t10 = a[0].rotateRight(2).xor(a[0].rotateRight(13));
            ThirtyTwoBits s4  = t10.xor(a[0].rotateRight(22));

            ThirtyTwoBits t11 = a[0].and(a[1]);
            ThirtyTwoBits t12 = a[0].and(a[2]);
            ThirtyTwoBits t13 = a[1].and(a[2]);

            ThirtyTwoBits t14 = t11.xor(t12);
            ThirtyTwoBits maj = t14.xor(t13);

            ThirtyTwoBits t15 = s4.integerAddition(maj);

            a[7] = a[6];
            a[6] = a[5];
            a[5] = a[4];
            a[4] = a[3].integerAddition(t9);
            a[3] = a[2];
            a[2] = a[1];
            a[1] = a[0];
            a[0] = t9.integerAddition(t15);
        }

        // Add the compressed chunk to the current hash value:
        for(int k = 0; k < 8; k++) {
            m_h[k] = m_h[k].integerAddition(a[k]);
        }

        reportLayers(m_h);

        m_hash = new Bit[256];

        //digest := hash := h0 append h1 append h2 append h3 append h4 append h5 append h6 append h7
        // The following nested for loops are not efficient, but I don't think it matters.
        for(   int l = 0; l <  8; l++) {
           for(int m = 0; m < 32; m++) {
               m_hash[l * 32  + m ] = m_h[l].get(m);
           }
        }
    }
    //////////////////////////////////////////////////////
    private static void reportLayers(ThirtyTwoBits[] arr){

        double counter  = 0;
        double sumOfAvg = 0;
        double maximum  = 0;

        for(    int i = 0; i < arr.length; i++){
            for(int j = 0; j < 32;         j++){

                Bit b = arr[i].get(j);

                sumOfAvg += b.getAverageLayer();

                maximum = Math.max( maximum, b.getMaxLayer());
                counter++;
            }
        }
        System.out.println("When working out the number of layers, had counter: " + Double.toString(counter));

        double average = sumOfAvg / Math.max(1, counter);

        System.out.println("Found average number of layers to be " + Double.toString(average));
        System.out.println("Found maximum number of layers to be " + Double.toString(maximum));

        System.out.println("Number of times AND called: " + Long.toString(Bit.getAndCounter()));
        System.out.println("Number of times XOR called: " + Long.toString(Bit.getXorCounter()));
        System.out.println("Number of times  OR called: " + Long.toString(Bit.getOrCounter ()));
        System.out.println("Number of times NOT called: " + Long.toString(Bit.getNotCounter()));
        System.out.println("Bit counter: "                + Long.toString(Bit.getBitCounter()));

    }
    //////////////////////////////////////////////////////
    private void generateHash(){
        initializeConstants();
        calcHashFromInput();
        outputResults();
    }
    ////////////////////////////////////////////////////////
    private void outputResults(){
        System.out.println("In binary, the output is: ");
        printBitArr(m_hash);
    }
    ////////////////////////////////////////////////////////
    private String getInputHexFromFile(String inputFilePath){
        System.out.println("About to read the contents of file: " + inputFilePath);

        try {
            File    myObj    = new File(inputFilePath);
            Scanner myReader = new Scanner(myObj);
            if(myReader.hasNextLine()) {
                String inputHex = myReader.nextLine();
                System.out.println(inputHex);
                return inputHex;
            } else{
                System.out.println("Didn't find any data in file.");
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while trying to read data from file.");
            e.printStackTrace();
        }
        return null;
    }
    ////////////////////////////////////////////////////////
    private void setInputBinary(String inputHex){
        int n = inputHex.length() * 4;
        m_inputBinary = new Bit[n];
        for(int i = 0; i< inputHex.length(); i++){
            int x = hexCharToInt(inputHex.charAt(i));
            //System.out.println(Integer.toString(x));
            int y = 1;
            for(int j = 0; j < 4; j++){
                int b = (x / y) - 2 * (x / (y*2));

                m_inputBinary[(i*4)+3-j] = new Bit (b == 1, 0,0);

                y *= 2;
            }
        }
    }
    //////////////////////////////////
    public static int hexCharToInt(char c){
        int ret;
        if( ('0' <= c) && (c <= '9'))
            ret = c - '0';
        else if (('A' <= c) && (c <= 'F'))
            ret = c - 55;
        else {
            System.out.println("Please use chars 0 to 9 and A to F. Have unrecognized char: " + c);
            ret = -1;
        }
        return ret;
    }
    /////////////////////////////////
    public static void printBitArr(Bit[] b){

        if(b == null)
            System.out.println("boolean array is null.");
        else {
            String str = new String("");

            for (int i = 0; i < b.length; i++) {
                if (b[i] == null)
                    str += "0";
                else
                 str += b[i].toString();
            }

            System.out.println(str);
        }
    }
    ///////////////////////////////////////////////////////
    private void initializeH(){
        m_h = new ThirtyTwoBits[8];

        int i = 0;
        m_h[i] = new ThirtyTwoBits("6A09E667");  i++;
        m_h[i] = new ThirtyTwoBits("BB67AE85");  i++;
        m_h[i] = new ThirtyTwoBits("3C6EF372");  i++;
        m_h[i] = new ThirtyTwoBits("A54FF53A");  i++;
        m_h[i] = new ThirtyTwoBits("510E527F");  i++;
        m_h[i] = new ThirtyTwoBits("9B05688C");  i++;
        m_h[i] = new ThirtyTwoBits("1F83D9AB");  i++;
        m_h[i] = new ThirtyTwoBits("5BE0CD19");
    }
    ///////////////////////////////////////////////////////
    private void initializeK(){
        m_k = new ThirtyTwoBits[64];

        int i = 0;
        m_k[i] = new ThirtyTwoBits("428A2F98"); i++;
        m_k[i] = new ThirtyTwoBits("71374491"); i++;
        m_k[i] = new ThirtyTwoBits("B5C0FBCF"); i++;
        m_k[i] = new ThirtyTwoBits("E9B5DBA5"); i++;
        m_k[i] = new ThirtyTwoBits("3956C25B"); i++;
        m_k[i] = new ThirtyTwoBits("59F111F1"); i++;
        m_k[i] = new ThirtyTwoBits("923F82A4"); i++;
        m_k[i] = new ThirtyTwoBits("AB1C5ED5"); i++;
        m_k[i] = new ThirtyTwoBits("D807AA98"); i++;
        m_k[i] = new ThirtyTwoBits("12835B01"); i++;
        m_k[i] = new ThirtyTwoBits("243185BE"); i++;
        m_k[i] = new ThirtyTwoBits("550C7DC3"); i++;
        m_k[i] = new ThirtyTwoBits("72BE5D74"); i++;
        m_k[i] = new ThirtyTwoBits("80DEB1FE"); i++;
        m_k[i] = new ThirtyTwoBits("9BDC06A7"); i++;
        m_k[i] = new ThirtyTwoBits("C19BF174"); i++;
        m_k[i] = new ThirtyTwoBits("E49B69C1"); i++;
        m_k[i] = new ThirtyTwoBits("EFBE4786"); i++;
        m_k[i] = new ThirtyTwoBits("0FC19DC6"); i++;
        m_k[i] = new ThirtyTwoBits("240CA1CC"); i++;
        m_k[i] = new ThirtyTwoBits("2DE92C6F"); i++;
        m_k[i] = new ThirtyTwoBits("4A7484AA"); i++;
        m_k[i] = new ThirtyTwoBits("5CB0A9DC"); i++;
        m_k[i] = new ThirtyTwoBits("76F988DA"); i++;
        m_k[i] = new ThirtyTwoBits("983E5152"); i++;
        m_k[i] = new ThirtyTwoBits("A831C66D"); i++;
        m_k[i] = new ThirtyTwoBits("B00327C8"); i++;
        m_k[i] = new ThirtyTwoBits("BF597FC7"); i++;
        m_k[i] = new ThirtyTwoBits("C6E00BF3"); i++;
        m_k[i] = new ThirtyTwoBits("D5A79147"); i++;
        m_k[i] = new ThirtyTwoBits("06CA6351"); i++;
        m_k[i] = new ThirtyTwoBits("14292967"); i++;
        m_k[i] = new ThirtyTwoBits("27B70A85"); i++;
        m_k[i] = new ThirtyTwoBits("2E1B2138"); i++;
        m_k[i] = new ThirtyTwoBits("4D2C6DFC"); i++;
        m_k[i] = new ThirtyTwoBits("53380D13"); i++;
        m_k[i] = new ThirtyTwoBits("650A7354"); i++;
        m_k[i] = new ThirtyTwoBits("766A0ABB"); i++;
        m_k[i] = new ThirtyTwoBits("81C2C92E"); i++;
        m_k[i] = new ThirtyTwoBits("92722C85"); i++;
        m_k[i] = new ThirtyTwoBits("A2BFE8A1"); i++;
        m_k[i] = new ThirtyTwoBits("A81A664B"); i++;
        m_k[i] = new ThirtyTwoBits("C24B8B70"); i++;
        m_k[i] = new ThirtyTwoBits("C76C51A3"); i++;
        m_k[i] = new ThirtyTwoBits("D192E819"); i++;
        m_k[i] = new ThirtyTwoBits("D6990624"); i++;
        m_k[i] = new ThirtyTwoBits("F40E3585"); i++;
        m_k[i] = new ThirtyTwoBits("106AA070"); i++;
        m_k[i] = new ThirtyTwoBits("19A4C116"); i++;
        m_k[i] = new ThirtyTwoBits("1E376C08"); i++;
        m_k[i] = new ThirtyTwoBits("2748774C"); i++;
        m_k[i] = new ThirtyTwoBits("34B0BCB5"); i++;
        m_k[i] = new ThirtyTwoBits("391C0CB3"); i++;
        m_k[i] = new ThirtyTwoBits("4ED8AA4A"); i++;
        m_k[i] = new ThirtyTwoBits("5B9CCA4F"); i++;
        m_k[i] = new ThirtyTwoBits("682E6FF3"); i++;
        m_k[i] = new ThirtyTwoBits("748F82EE"); i++;
        m_k[i] = new ThirtyTwoBits("78A5636F"); i++;
        m_k[i] = new ThirtyTwoBits("84C87814"); i++;
        m_k[i] = new ThirtyTwoBits("8CC70208"); i++;
        m_k[i] = new ThirtyTwoBits("90BEFFFA"); i++;
        m_k[i] = new ThirtyTwoBits("A4506CEB"); i++;
        m_k[i] = new ThirtyTwoBits("BEF9A3F7"); i++;
        m_k[i] = new ThirtyTwoBits("C67178F2");
     }
     ////////////////////////////////////////////////////////////
}
