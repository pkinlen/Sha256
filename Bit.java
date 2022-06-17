public class Bit {
    private boolean m_b;

    // The next to variables (members) are just used to keep track of the number of steps
    // required to get from the inputs to the current Bit.
    // They are not used in the calculation of the hash.
    private double m_averageLayer;
    private double m_maxLayer;

    // We keep track of the number of times each of the functions are called.
    // It is used to help us understand the algorithm.
    // The following static counters are not directly used in the calculation of the hash.
    private static long m_andCounter;
    private static long m_xorCounter;
    private static long m_orCounter;
    private static long m_notCounter;
    private static long m_bitCounter;

    //////////////////////////////////////////////
    /*
    Bit(boolean b){

        m_b = b;
        m_averageLayer = 0;
        m_maxLayer     = 0;
    }
     */
    //////////////////////////////////////////////
    Bit(boolean b, double averageLayer, double maxLayer){

        m_b            = b;

        m_averageLayer = averageLayer;
        m_maxLayer     = maxLayer;

        m_bitCounter++;

        /*
        if ( (m_bitCounter % 1000) == 0)
           System.out.println(  "Instantiating Bit with: b: " + Boolean.toString(b)
                              + ", averageLayer: "            + Double.toString (averageLayer)
                              + ", maxLayer"                  + Double.toString (maxLayer)     );

         */
    }
    //////////////////////////////////////////////
    Bit xor (Bit a){

        double averageLayer = getNextAverage(a.getAverageLayer(), m_averageLayer);
        double maxLayer     = Math.max(m_maxLayer,    a.getMaxLayer()    ) + 1.0;

        Bit ret = new Bit( m_b  != a.getBoolean(), averageLayer, maxLayer);

        m_xorCounter++;
        return ret;
    }
    //////////////////////////////////////////////
    Bit and( Bit a){
        double averageLayer = getNextAverage(a.getAverageLayer(), m_averageLayer);
        double maxLayer     = Math.max(m_maxLayer,    a.getMaxLayer()    ) + 1.0;

        Bit ret = new Bit( m_b && a.getBoolean(), averageLayer, maxLayer);

        m_andCounter++;
        return ret;
    }
    //////////////////////////////////////////////
    Bit or( Bit a){
        m_orCounter++;

        // The motivation for using the and(.) fundtion to calculate this or(.)
        // is just to show that the sha can be evaluated without an or(.)
        // The sha256 is calculated using, not, xor & and.

        // In the next line we use   A OR B = NOT( (NOT A) AND (NOT B))
        return (a.not().and(not())).not();

    }
    //////////////////////////////////////////////
    Bit orVersion2( Bit a){
        double averageLayer = getNextAverage(a.getAverageLayer(), m_averageLayer);
        double maxLayer     = Math.max(m_maxLayer,    a.getMaxLayer()    ) + 1.0;

        m_orCounter++;

        // In the next line we use   A OR B = NOT( (NOT A) AND (NOT B))
        return (a.not().and(not())).not();

        //return new Bit( m_b || a.getBoolean(), averageLayer, maxLayer);
    }
    //////////////////////////////////////////////
    boolean getBoolean(){
        return m_b;
    }
    //////////////////////////////////////////////
    Bit not(){
        Bit ret = new Bit( ! m_b, m_averageLayer, m_maxLayer);

        m_notCounter++;
        return ret;
    }
    //////////////////////////////////////////////
    public String toString(){
        return new String( m_b ? "1" : "0");
    }
    //////////////////////////////////////////////
    public double getAverageLayer(){
        return m_averageLayer;
    }
    //////////////////////////////////////////////
    public double getMaxLayer(){
        return m_maxLayer;
    }
    //////////////////////////////////////////////
    public static long getAndCounter(){
        return m_andCounter;
    }
    //////////////////////////////////////////////
    public static long getOrCounter() {
        return m_orCounter;
    }
    //////////////////////////////////////////////
    public static long getXorCounter(){
        return m_xorCounter;
    }
    ///////////////////////////////////////////////
    public static long getNotCounter(){
        return m_notCounter;
    }
    ///////////////////////////////////////////////
    public static long getBitCounter(){
        return m_bitCounter;
    }
    //////////////////////////////////////////////
    private static double getNextMax(double maxA, double maxB){
        return (Math.max(maxA, maxB) + 1.0);
    }
    //////////////////////////////////////////////
    private static double getNextAverage(double avgA, double avgB){
        if ( avgA == avgB)
            return avgA + 1.0;
        else if ( (avgA * avgB) == 0.0)
            return Math.max(avgA, avgB);
        else
            return 1.0 + 0.5 * ( avgA + avgB);
    }
    //////////////////////////////////////////////
}
