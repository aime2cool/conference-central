package com.google.devrel.training.conference.domain;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

import com.google.common.collect.ImmutableList;
import com.google.devrel.training.conference.form.ConferenceForm;
import com.google.devrel.training.conference.form.SessionForm;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.condition.IfNotDefault;

@Entity
@Cache
public class Session {
	private static final String DEFAULT_CITY = "Default City";
	@Index String name;
	SessionType type;
	@Index
	String speaker;
	@Parent
	private Key<Conference> conferenceKey;
	@Id
	long id;
	@Index
	long conferenceId;
	
	@Index
	private List<String> topics;
	@Index
	private Date date;
	private String duration;
	@Index
	private String start_time;

	public Session(long id, long conferenceId, SessionForm sessionForm) {
		this.id = id;
		this.conferenceId = conferenceId;
		this.conferenceKey = Key.create(Conference.class, conferenceId);
		updateWithSessionForm(sessionForm);
	}

	public Date getDate() {
		return date == null ? null : new Date(date.getTime());
	}
	
	public String getDuration() {
		return duration;
	}

	public Date getStartTime() throws ParseException {
		DateFormat sdf = new SimpleDateFormat("hh:mm");
		Date tmp = sdf.parse(start_time);
		return tmp;
	}

	public String getSpeaker() {
		return speaker;
	}

	public SessionType getType() {
		return type;
	}

	public List<String> getTopics() {
        return topics == null ? null : ImmutableList.copyOf(topics);
    }

	public Key<Conference> getConferenceKey() {
		return conferenceKey;
	}

	/**
	 * Updates the Session with Session Form. This method is used upon object
	 * creation as well as updating existing Session.
	 * 
	 * @param sessionForm
	 *            contains form data sent from the client.
	 */
	public void updateWithSessionForm(SessionForm sessionForm) {
		type = sessionForm.getType();
		speaker = sessionForm.getSpeaker();
		

		Date date = sessionForm.getDate();
		this.date = date == null ? null : new Date(
				date.getTime());
		
	}

	public static enum SessionType {
		Workshop, Lecture, Keynote
	}
}
