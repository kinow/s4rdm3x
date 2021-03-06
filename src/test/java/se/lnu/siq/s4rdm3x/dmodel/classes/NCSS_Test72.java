package se.lnu.siq.s4rdm3x.dmodel.classes;

public class NCSS_Test72 {

    public int a = 0;
    public int b = 1;

    static class BoundingBox {
        public int left;
        public int right;
        public int top;
        public int bottom;
    }

    public int left;
    public int right;
    public int top;
    public int bottom;

    /**
     * See example PWS in Zuse page 153.
     */
    public void testPWS(int a, int b)
    {
        if ( a < b )
        {
            System.out.println( "3" );
        }
        else
        {
            System.out.println( "2" );
        }
        System.out.println( "4" );

        if ( a > b)
        {
            System.out.println( "7" );
        }
        else
        {
            System.out.println( "6" );
        }
        System.out.println( "8" );

        if ( a == b )
        {
            System.out.println( "11" );
        }
        else
        {
            System.out.println( "10" );
        }
        System.out.println( "12" );
    }

    /**
     * See example PWU10 (plus additional while loop with a break) in Zuse page 153.
     */
    public void testPWU10()
    {
        if ( a < b ) // 1
        {
            System.out.println( "7" );
        }
        else
        {
            while( true )
            {
                if ( a > b ) // 2
                {
                    continue;
                }
                else
                {
                    if ( a == b ) // 3
                    {
                        System.out.println( "5" );
                    }
                    else
                    {
                        System.out.println( "4" );
                    }
                    System.out.println( "6" );
                }
            }
        }
        System.out.println( "8" );
        System.out.println( "9" );
        System.out.println( "10" );
    }

    public boolean intersect(BoundingBox b)
    {
        return ((this.left<=b.right) && (this.right>=b.left)
                && (this.bottom<=b.top) && (this.top>=b.bottom));
    }

    public boolean verboseIntersect(BoundingBox b)
    {
        boolean result = left <= b.right;
        if ( result )
        {
            result = right >= b.left;
            if ( result )
            {
                result = bottom <= b.top;
                if ( result )
                {
                    result = top >= b.bottom;
                }
            }
        }

        return result;
    }

    public void testQuestionMark()
    {
        int smaller = (a<b)?a : b;
    }
}
