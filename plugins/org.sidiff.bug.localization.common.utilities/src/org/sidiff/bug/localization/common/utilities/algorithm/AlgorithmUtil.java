package org.sidiff.bug.localization.common.utilities.algorithm;

import java.util.Random;

public class AlgorithmUtil {

	public static int[] sampleRandomNumbersWithoutRepetitionAndReplacements(int start, int end, int count) {
		// NOTE: Choose n-times random numbers (without repetition) if count is larger then end.
		Random random = new Random();

		int[] result = new int[count];
		int picked = 0;
		int range = end - start;
		
		int replacements = (int) Math.ceil((double) count / (double) end);
		int[] counts = new int[replacements];
		
		for (int j = 0; j < replacements - 1; j++) {
			counts[j] = range;
		}
		counts[counts.length - 1] =  count - ((replacements - 1) * range);
		
		for (int i = start; i < end && count > 0; i++) {
			for (int j = 0; j < replacements; j++) {
				double probability = random.nextDouble();
				
				if (probability < ((double) counts[j]) / (double) (range - i)) {
					counts[j]--;
					result[picked++] = i;
				}
			}
		}
		return result;
	}
}
