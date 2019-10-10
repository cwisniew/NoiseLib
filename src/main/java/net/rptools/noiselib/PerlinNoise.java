package net.rptools.noiselib;

public class PerlinNoise {

  private static final int TABLE_SIZE = 256;                // Must be a power of 2 for this to work
  private static final int TABLE_SIZE_MASK = TABLE_SIZE - 1;

  private final int[] permutationTable = new int[TABLE_SIZE * 2];



  public PerlinNoise(long seed) {
    int[] array = Utils.shuffleSequentialArray(TABLE_SIZE, seed);
    /*
     * Set up double sized permutation array look up. The second half is just copy of the first
     * half, but it saves us from having to do
     */
    for (int i = 0; i < TABLE_SIZE; i++) {
      permutationTable[i] = array[i];
      permutationTable[TABLE_SIZE + i] = array[i];
    }

  }

  public PerlinNoise() {
    this(System.currentTimeMillis());
  }


  public double noise(double x, double y) {
    return noise(x, y, 0.01);
  }

  public double noise(double x, double y, double z) {

    /*
     * Take the integer part of the co-ordinate and mask it so it fits into our table
     */

    int xi = (int) x & TABLE_SIZE_MASK;
    int yi = (int) y & TABLE_SIZE_MASK;
    int zi = (int) z & TABLE_SIZE_MASK;

    // Next we want the remainder non integer part of the co-ordinate
    double xd = x - (int) x;
    double yd = y - (int) y;
    double zd = z - (int) z;


    double u = fade(xd);
    double v = fade(yd);
    double w = fade(zd);

    // Get the value at all vertices of the unit cube
    int xyz      = hashNoise(xi, yi, zi);
    int xy1z     = hashNoise(xi, yi + 1, zi);
    int xyz1     = hashNoise(xi, yi, zi + 1);
    int x1yz     = hashNoise(xi + 1, yi, zi);
    int x1y1z    = hashNoise(xi + 1, yi + 1, zi);
    int x1yz1    = hashNoise(xi + 1, yi, zi + 1);
    int xy1z1    = hashNoise(xi, yi + 1, zi + 1);
    int x1y1z1   = hashNoise(xi + 1, yi + 1, zi + 1);


    double x1 = linerInterpolate(
        dot(xyz, xd, yd, zd),
        dot(x1yz, xd - 1, yd, zd),
        u
    );

    double x2 = linerInterpolate(
        dot(xy1z, xd, yd - 1, zd),
        dot(x1y1z, xd -1, yd -1, zd),
        u
    );

    double y1 = linerInterpolate(x1, x2, v);

    x1 = linerInterpolate(
        dot(xyz1, xd, yd, zd - 1),
        dot(x1yz1, xd - 1, yd, zd - 1),
        u
    );

    x2 = linerInterpolate(
        dot(xy1z1, xd, yd - 1, yd - 1),
        dot(x1y1z1, xd - 1, yd - 1, zd - 1),
        u
    );

    double y2 = linerInterpolate(x1, x2, v);

    return (linerInterpolate(y1, y2, w) + 1) / 2;

  }


  /**
   * This returns a value between v1 and v2 at a distance d (0 to 1) using linear interpolation..
   * @param v1 The first value.
   * @param v2 The second value.
   * @param d The distance between the two values (v1 being 0, v2 being 1).
   *
   * @return The value between v1 and v2 at distance d.
   */
  private double linerInterpolate(double v1, double v2, double d) {
    return v1 + d * (v2 - v1);
  }

  /**
   * This function returns the dot product between the two vectors passed in.
   * @param vect1 The lower four bits of this value represents a vector from
   *               a predefined set of vectors.
   * @param x The x co-ordinate of the second vector.
   * @param y The y co-ordinate of the second vector.
   * @param z The z co-ordinate of the second vector.
   *
   *
   * The lower for bits represent the following vectors
   *  0   =  0000  = (1,1,0)
   *  1   =  0001  = (-1,1,0)
   *  2   =  0010  = (1,-1,0)
   *  3   =  0011  = (-1,-1,0)
   *  4   =  0100  = (1,0,1)
   *  5   =  0101  = (-1,0,1)
   *  6   =  0110  = (1,0,-1)
   *  7   =  0111  = (-1,0,-1)
   *  8   =  1000  = (0,1,1)
   *  9   =  1001  = (0,-1,1)
   *  10  =  1010  = (0,1,-1)
   *  11  =  1011  = (0,-1,-1)
   *  12  =  1100  = (1,1,0)
   *  13  =  1101  = (-1,1,0)
   *  14  =  1110  = (0, -1, 1)
   *  15  =  1111  = (0, -1, -1)
   *
   * @return The dot product of the two vectors.
   */
  private double dot(int vect1, double x, double y, double z) {
      /*
       * So this code looks noting like what its actually doing...
       * We are taking the lower 4 bits of hashed and treating them as a unit vector.
       *
       * We are then doing a dot product of this vector and the x,y,z vector
       *
       * Formula for dot product is a • b = ax * bx + ay * by + az * bz
       *
       * Since the left hand vector x,y,z values are either -1, 0, or 1 the result
       * can be expressed in addition or subtraction of the right hand vector.
       *
       * So this case statement uses the fact that we have an index into an imaginary
       * array for our vectors and returns the sum of the correct x,y,z from the second
       * vector without having to multiple it out in the code.
       */
      switch(vect1 & 0xF) {
        case 1:
          return x + y;
        case 2:
          return -x + y;
        case 3:
          return x - y;
        case 4:
          return x + z;
        case 5:
          return -x + z;
        case 6:
          return x - z;
        case 7:
          return -x - z;
        case 8:
          return y + z;
        case 9:
          return -y + z;
        case 10:
          return y - z;
        case 11:
          return -y - z;
        case 12:
          return y + x;
        case 13:
          return -x + y;
        case 14:
          return -y + z;
        case 15:
          return -y - z;
        default:
          return 0; // not going to happen
      }
  }


  /*
   * This method calculates a smoothed out value for the input.
   * This is done using the formula as defined by Ken Perlin.
   *
   * 6t^5 - 15t^4 + 10t^3
   */
  private double fade(double t) {
    return t * t * t * (t * (t * 6 - 15) + 10);    // 6t^5 - 15t^4 + 10t^3
  }

  /**
   * This method returns a value between 0 and TABLE_SIZE based the input values.
   *
   * @param x The x co-ordinate.
   * @param y The y co-ordinate.
   * @param z The z co-ordinate.
   *
   * @return The hash value based in the input values.
   */
  private int hashNoise(int x, int y, int z) {
    /*
     * We use the result of lookup into our table for x and add it to y to perform another look up
     * then add this value to z to perform the last lookup into our table. This is why the array
     * size was doubled as y and z are between 0 and TABLE_SIZE (inclusive) and the result from the
     * lookup is between 0 and TABLE_SIZE (inclusive) which means the next lookup will be between 0
     * and TABLE_SIZE * 2 (inclusive).
     */
    return permutationTable[z + permutationTable[y + permutationTable[x]]];
  }


}
