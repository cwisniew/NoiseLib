package net.rptools.noiselib;

import java.util.Arrays;
import java.util.Random;

class Utils {

  static int[] shuffleArray(int[] array, long seed) {
    int[] newArray = Arrays.copyOf(array, array.length);
    Random random = new Random(seed); // Use seed as we want repeatability

    for (int i = 0; i < newArray.length; i++) {
      int randPos = random.nextInt();
      int temp = newArray[i];
      newArray[i] = newArray[randPos];
      newArray[randPos] = temp;
    }

    return newArray;
  }


  static int[] shuffleSequentialArray(int size, long seed) {
    int[] array = new int[size];

    for (int i = 0; i < size; i++) {
      array[i] = i;
    }

    return shuffleArray(array, seed);
  }

}
