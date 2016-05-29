package model;

public class Type {
	Integer id;
	String name;
	
	public static Type other = new Type(-1,"other");
	
	public Type(Integer id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Type(){
		
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
