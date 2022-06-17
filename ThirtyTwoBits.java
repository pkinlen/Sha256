public class ThirtyTwoBits {

    private Bit[] m_array;
    private int   m_leftOffset;

    ///////////////////////////////////////
    ThirtyTwoBits(String hexString){
        m_array = new Bit[32];

        for(int i = 0; i< hexString.length(); i++){
            int x = Sha256.hexCharToInt(hexString.charAt(i));
            //System.out.println("Initializing 32 bit array: " + Integer.toString(x));
            int y = 1;
            for(int j = 0; j < 4; j++){
                // If there was a need to speed this up, we could do one division in the line below
                // and then keep the result for the next iteration.
                int b = (x / y) - 2 * (x / (y*2));

                m_array[(i*4)+3-j] = new Bit(b == 1, 0,0);

                //System.out.println(    "j: " + Integer.toString(j) + ", b: " + Integer.toString(b)
                //                   + ", x: " + Integer.toString(x) + ", y: " + Integer.toString(y));

                y = y * 2;
            }
        }
    }
    ///////////////////////////////////////

    ThirtyTwoBits() {

        m_array = new Bit[32];
        // The default is to initialize to false.

        for(int i = 0; i < 32; i++){
            m_array[i] = new Bit(false, 0,0 );
        }

    }
    ///////////////////////////////////////
    ThirtyTwoBits(Bit[] arr){
        m_array = arr;

        // alternatively could do a deep copy:
        m_array = new Bit[32];
        for(int i = 0; i < 32; i++){
            m_array[i] = arr[i];
        }
    }

    ///////////////////////////////////////
    public Bit get(int i){
        int j = (i + m_leftOffset) % 32;
        return m_array[j];
    }
    ///////////////////////////////////////
    public void set(int i, Bit b) {
        int j = (i + m_leftOffset) % 32;
        m_array[j] = b;
    }
    ///////////////////////////////////////
    public ThirtyTwoBits shiftRight(int r){
        ThirtyTwoBits ttb = new ThirtyTwoBits();

        for(int i = r; i < 32; i++){
            ttb.set(i, get(i-r));
        }
        return ttb;
    }
    ///////////////////////////////////////
    public ThirtyTwoBits rotateRight(int r){
           return (new ThirtyTwoBits(m_array, r - m_leftOffset));
    }
    ///////////////////////////////////////
    ThirtyTwoBits(Bit[] arr, int rightOffset){
        m_array      = arr;  // a shallow copy
        m_leftOffset = 32 - rightOffset;
        // m_offset is the left offset, hence here we use:  32 - rightOffset
    }
    ///////////////////////////////////////
    ThirtyTwoBits xor(ThirtyTwoBits ttb){
        ThirtyTwoBits ret = new ThirtyTwoBits();

        for( int i = 0; i < 32; i++)
            ret.set(i, get(i).xor(ttb.get(i)));

        return ret;
    }
    ///////////////////////////////////////
    ThirtyTwoBits and(ThirtyTwoBits ttb){
        ThirtyTwoBits ret = new ThirtyTwoBits();

        for( int i = 0; i < 32; i++)
            ret.set(i, get(i).and(ttb.get(i)));

        return ret;
    }
    ///////////////////////////////////////
    ThirtyTwoBits not(){
        ThirtyTwoBits ret = new ThirtyTwoBits();

        for( int i = 0; i < 32; i++)
            ret.set(i,get(i).not());

        return ret;
    }
    ///////////////////////////////////////
    ThirtyTwoBits integerAddition(ThirtyTwoBits ttb){

        ThirtyTwoBits ret   = new ThirtyTwoBits();
        Bit           carry = new Bit(false, get(31).getAverageLayer(), get(31).getMaxLayer());
        // We are treating our bits as a big-endian integer. Most significant bits first.
        for(int i = 31; i >= 0; i--){

            Bit b = carry.xor(get(i));

            ret.set(i, (b.xor( ttb.get(i))));

            // we carry if at least two out of three of following are true { carry, get(i), ttb.get(i) }
            // Perhaps the efficiency (or elegance) of the following line could be improved.
            carry =    (carry. and( get(i))    )
                    .or(carry. and( ttb.get(i)))
                    .or(get(i).and( ttb.get(i)));
        }
        return ret;
    }
    //////////////////////////////////////
    public void printBinary(String msg){
        for(int i = 0; i < 32; i++){

            msg += get(i).toString();
        }
        System.out.println(msg);
    }
    //////////////////////////////////////
}
