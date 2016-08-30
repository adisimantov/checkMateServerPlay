package controllers;

import model.MySqlDriver;

public class SimilarityThread implements Runnable {

	@Override
	public void run() {
		try {
			MySqlDriver.calcSimilarities();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
