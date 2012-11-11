package com.example.facebookapi;

public class ChatFields {
	private String name;
	private String time;
	private String text;
	private boolean color;
	
	public void setName (String who)    {name = who;}
	public void setTime (String when)   {time = when;}
	public void setText (String what)   {text = what;}
	public void setColor(boolean color) {this.color = color;}
	
	public String  getName()   {return name;}
	public String  getTime()   {return time;}
	public String  getText()   {return text;}
	public boolean getColor()  {return color;}
}
