package model;

public class Interest {
	Integer id;
	String name;
	
	public Interest(Integer id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Interest(){

	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
