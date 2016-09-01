package model;

public class Type {
	Integer id;
	String name;
	Integer rate;

	public static Type other = new Type(-1, "other");

	public Type(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Type(Integer id, String name, Integer rate) {
		this.id = id;
		this.name = name;
		this.rate = rate;
	}

	public Type() {

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

	public Integer getRate() {
		return rate;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}
	
	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Type)) {
			return false;
		}

		return ((Type) obj).id == this.id;
	}
}
