package com.google.devrel.training.conference.form;

import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.devrel.training.conference.domain.Session.SessionType;


public class SessionForm {
	private String name;
	private String speaker;
    private Date date;
    private SessionType type;
    private String start_time;
    private String duration;
    private List<String> topics;
    public SessionForm(String name, String speaker, Date date, SessionType type, 
    		String start_time, String duration, List<String> topics) {
    	this.name = name;
    	this.speaker = speaker;
    	this.date = date == null? null : new Date(date.getTime());
    	this.type = type;
    	this.start_time = start_time;
    	this.duration = duration;
    	this.topics = topics == null ? null : ImmutableList.copyOf(topics);
    }
    public String getName() {
    	return name;
    }
    public String getSpeaker() {
    	return speaker;
    }
    public Date getDate() {
    	return date;
    }
    public SessionType getType() {
    	return type;
    }
	public String getStartTime() {
		return start_time;
	}
	public String getDuration() {
		return duration;
	}
	public List<String> getTopics() {
		return topics;
	}
	
}
