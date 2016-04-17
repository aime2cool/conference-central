package com.google.devrel.training.conference.domain;

import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;

public class Speaker extends Profile{

	public Speaker(String userId, String displayName, String mainEmail,
			TeeShirtSize teeShirtSize) {
		super(userId, displayName, mainEmail, teeShirtSize);
		// TODO Auto-generated constructor stub
	}
	
}
