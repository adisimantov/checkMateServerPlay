package model;

public class User {
	private String userId;
	private String token;
	private Integer age;
	private char gender;
	
	public User() {
	}
	
	public User(String userId, String token) {
		this.userId = userId;
		this.token = token;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public char getGender() {
		return gender;
	}

	public void setGender(char gender) {
		this.gender = gender;
	}
	
	public int getGenderCode() {
		return (gender == 'f') ? 1 : 0;
	}
	
	public int getAgeNotNull() {
		return (age == null) ? 20 : age;
	}
}
