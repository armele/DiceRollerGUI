package com.deathfrog.npc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

import com.deathfrog.utils.GameException;

public class DiceDistro {

	@Test
	public void test() {
		
		try (Writer writer = new FileWriter("dicecombos.csv")) {
			
			int[] dice = {1,1,1,1,1,1,1};
			
				for (int a = 1; a < 7; a++) {
					dice[0] = a;
					for (int b = 1; b < 7; b++) {
						dice[1] = b;
						for (int c = 1; c < 7; c++) {
							dice[2] = c;
							for (int d = 1; d < 7; d++) {
								dice[3] = d;
								for (int e = 1; e < 7; e++) {
									dice[4] = e;
									for (int f = 1; f < 7; f++) {
										dice[5] = f;
										//System.out.println(dice[0]+","+dice[1]+","+dice[2]+","+dice[3]+","+dice[4]+","+dice[5]);
										writer.write(dice[0]+","+dice[1]+","+dice[2]+","+dice[3]+","+dice[4]+","+dice[5]+"\r\n");
									}
								}
							}
						}
					}
				}
		
		} catch (IOException ie) {
			// TODO: Display error messages to the user when appropriate.
			System.out.println(GameException.fullExceptionInfo(ie));
		}		
	}
	
}
