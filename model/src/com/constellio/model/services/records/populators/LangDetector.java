package com.constellio.model.services.records.populators;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Majid on 2016-04-05.
 */
//FIXME: check for thread safe.
public class LangDetector {
	private static LangDetector langDetector = null;
	private LangDetector(){

	}

	private void init(String profileDirectory) throws LangDetectException {
		DetectorFactory.loadProfile(profileDirectory);
	}

	public synchronized  String detect(String text) throws LangDetectException {
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.detect();
	}

	public synchronized ArrayList<Language> detectLangs(String text) throws LangDetectException {
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.getProbabilities();
	}

	//FIXME: put the profile directory in the resource folder
	public static synchronized LangDetector getInstance() {
		if (langDetector == null){
			langDetector = new LangDetector();
			try {
				langDetector.init("language-profiles/");
			} catch (LangDetectException e) {
				throw new RuntimeException(e);
			}
		}
		return langDetector;
	}
}
